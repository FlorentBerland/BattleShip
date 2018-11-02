package players

import java.awt.Point

import akka.actor.{Actor, ActorRef}
import core.messages._
import core.model._
import util.{FleetHelper, Rand}

import scala.util.Failure


/**
  * AI that should shot smartly at the opponent. It will choose first the squares that has the
  * greatest probability of containing a ship. Chooses a medium AI as opponent if asked to.
  */
class StrongAIPlayer extends Actor {

  override def receive: Receive = {
    case msg: ChooseOpponent => onChooseGameConfig(msg)
    case msg: CreateFleet => onCreateFleet(msg)
    case msg: NotifyCanPlay => onNotifyCanPlay(msg)
    case msg: LastRoundResult => onLastRoundResult(msg)
    case msg: GameOver => onGameOver(msg)

    case _ =>
  }

  ///// AI state:

  // The initial fleet composition by player id. Used to compute what opponent ships are still alive and then
  // eliminate impossible ship locations to aim
  var fleetsComposition = Map.empty[String, Set[GenericShip]]


  private def onChooseGameConfig(msg: ChooseOpponent): Unit = {
    msg.nextActor ! new OpponentChosen(self, "MediumAIPlayer")
  }

  private def onCreateFleet(msg: CreateFleet): Unit = {
    msg.nextActor ! new FleetCreated(msg.playerId, FleetGrid(msg.dimension, msg.ships))
    fleetsComposition = fleetsComposition + (msg.playerId -> msg.ships)
  }

  private def onNotifyCanPlay(msg: NotifyCanPlay): Unit = {
    play(msg.nextActor, msg.shotGrid, msg.playerId)
  }

  private def onLastRoundResult(msg: LastRoundResult): Unit = {
    msg.result match {
      case Failure(ex) => ex match {
        case _: IllegalArgumentException => play(msg.sender, msg.shotGrid, msg.playerId)
        case _ =>
      }
      case _ =>
    }
  }

  private def onGameOver(msg: GameOver): Unit = {
    fleetsComposition = fleetsComposition - msg.playerId
    msg.sender ! new Replay(msg.playerId, true)
  }


  /**
    * This is almost the same code as the Medium AI but instead of randomly aim when no ship has been found,
    * the coordinates of the next shot are computed using heuristics on the ships locations
    */
  private def play(sender: ActorRef, shotGrid: ShotGrid, id: String): Unit = {
    val flatFleet = FleetHelper.flatten(shotGrid)

    // Get the longest sequences of horizontally and vertically aligned alive ship squares
    val hAliveSeq = FleetHelper.longestHorizontalSequence[Option[Ship]](flatFleet, !_.getOrElse(Ship()).isDestroyed)
    val vAliveSeq = FleetHelper.longestVerticalSequence[Option[Ship]](flatFleet, !_.getOrElse(Ship()).isDestroyed)

    // Get the longest sequence of each and theirs coordinates
    val (hMax, hX, hY) = FleetHelper.maxValue(hAliveSeq)
    val (vMax, vX, vY) = FleetHelper.maxValue(vAliveSeq)

    val coordinates: Point =
      if(hMax == vMax && hMax == 0){
        aimWithNormalShipDistributionHeuristic(shotGrid, fleetsComposition.get(id))
      } else if(hMax >= vMax){
        tryToShootAtHorizontalAlignment(hMax, hX, hY, shotGrid).getOrElse(
          tryToShootAtVerticalAlignment(vMax, vX, vY, shotGrid).getOrElse(
            aimWithNormalShipDistributionHeuristic(shotGrid, fleetsComposition.get(id))
          )
        )
      } else {
        tryToShootAtVerticalAlignment(vMax, vX, vY, shotGrid).getOrElse(
          tryToShootAtHorizontalAlignment(hMax, hX, hY, shotGrid).getOrElse(
            aimWithNormalShipDistributionHeuristic(shotGrid, fleetsComposition.get(id))
          )
        )
      }

    sender ! new Play(id, coordinates)
  }


  /**
    * Return coordinates of a shot to perform with a given horizontal hit squares alignment
    *
    * @param length The length of the alignment
    * @param x The coordinates of the starting point
    * @param y The coordinates of the starting point
    * @param shotGrid The opponent's grid
    * @return The square to aim, or None if this is not possible (outside the grid or shot already performed)
    */
  def tryToShootAtHorizontalAlignment(length: Int, x: Int, y: Int, shotGrid: ShotGrid): Option[Point] = {
    val shotMap = FleetHelper.flattenShotMap(shotGrid)
    if (x > 0 && !shotMap(x - 1)(y)) {
      // Shoot at the left
      Some(new Point(x - 1, y))
    } else if (x + length < shotGrid.dim.width && !shotMap(x + length)(y)) {
      // Shoot at the right
      Some(new Point(x + length, y))
    } else {
      // Impossible to shoot at the alignment, there were two vertical ships side by side
      None
    }
  }

  /**
    * Return coordinates of a shot to perform with a given vertical hit squares alignment
    *
    * @param length The length of the alignment
    * @param x The coordinates of the starting point
    * @param y The coordinates of the starting point
    * @param shotGrid The opponent's grid
    * @return The square to aim, or None if this is not possible (outside the grid or shot already performed)
    */
  def tryToShootAtVerticalAlignment(length: Int, x: Int, y: Int, shotGrid: ShotGrid): Option[Point] = {
    val shotMap = FleetHelper.flattenShotMap(shotGrid)
    if(y > 0 && !shotMap(x)(y-1)){
      // Shoot at the top
      Some(new Point(x, y - 1))
    } else if(y + length < shotGrid.dim.height && !shotMap(x)(y + length)){
      // Shoot at the bottom
      Some(new Point(x, y + length))
    } else {
      // Impossible to shoot at the alignment, there were two horizontal ships side by side
      None
    }
  }


  /**
    * Find a square to shot taking account of the distances to the sunk ships. In case of a pseudo-random
    * distribution or a hand-made distribution, ships are rarely placed side by side (or only in a specific
    * hand-made distribution strategy). It also takes account of the distances between the shots performed
    * to get a better grid coverage. Finally, it will aim only at the sequences of squares that are large enough
    * to contain an alive ship.
    * For needs of simplicity, partially damaged ships are not considered to eliminate improbable
    * empty squares sequences by length.
    *
    * @param shotGrid The opponent's grid
    * @param fleetComposition The fleet composition to retrieve the sunk ship sizes (None should not happen)
    */
  private def aimWithNormalShipDistributionHeuristic(shotGrid: ShotGrid, fleetComposition: Option[Set[GenericShip]]): Point = {
    // Compute the greatest empty sequences of squares not shot and remove the
    // sequences that cannot contain the alive ships
    val aliveShips = fleetComposition.map(f => FleetHelper.notSunkShips(f, shotGrid))
    val shotMap = FleetHelper.flattenShotMap(shotGrid)
    val verticalSequences = FleetHelper.longestVerticalSequence[Boolean](shotMap, a => !a)
    val horizontalSequences = FleetHelper.longestHorizontalSequence[Boolean](shotMap, a => !a)
    val distancesToNearestShip = FleetHelper.distanceToNearestObstacle[Option[Ship]](FleetHelper.flatten(shotGrid), _.nonEmpty)
    val distancesToNearestShot = FleetHelper.distanceToNearestObstacle[Boolean](shotMap, p => p)

    val smallestShipSize = aliveShips.map(_.minBy(_.size).size).getOrElse(0)

    // Result example: (smallest ship size = 3, [[ 4, 3, 2, 1, 0, 0, 3, 2, 1 ]]) -> [[ 4, 3, 0, 0, 0, 0, 3, 0, 0 ]]
    val filteredVerticalSequences = verticalSequences.map(_.map(x => if(x < smallestShipSize) 0 else x))
    val filteredHorizontalSequences = horizontalSequences.transpose.map(_.map(x => if(x < smallestShipSize) 0 else x)).transpose

    val weights = weightSquaresToAim(filteredHorizontalSequences, filteredVerticalSequences, distancesToNearestShip, distancesToNearestShot)
    val maxWeight = weights.map(_.max).max
    val toleratedWeight = maxWeight * .75 // If the coordinates are chosen using the maxWeight equality,
    // patterns appear in the shot coverage strategy. The threshold allows more flexibility so shots are more random
    // and it is harder to abuse the game with the ships placing strategy.
    val maxWeightOccurrencesCoordinates: List[Point] = weights.indices.flatMap(i => weights(i).indices.map(j => {
      if(weights(i)(j) >= toleratedWeight) Some(new Point(i, j)) else None
    })).flatten.toList

    // Randomly choose a point among those which equal the maxWeight
    val selectedPoint = maxWeightOccurrencesCoordinates(Rand.r.nextInt(maxWeightOccurrencesCoordinates.size))
    // Then shoot at the middle of the vertical or horizontal sequence starting at this point
    // This is a divide and conquer method: shoot in the middle of the unknown squares sequences so for the next shoots
    // incompatible empty sequence lengths are quickly eliminated (incompatible with the smallest alive ship size)
    val shotCoordinates =
      if(horizontalSequences(selectedPoint.x)(selectedPoint.y) > verticalSequences(selectedPoint.x)(selectedPoint.y)){
        (selectedPoint.x + horizontalSequences(selectedPoint.x)(selectedPoint.y) / 2, selectedPoint.y)
      } else if(horizontalSequences(selectedPoint.x)(selectedPoint.y) < verticalSequences(selectedPoint.x)(selectedPoint.y)) {
        (selectedPoint.x, selectedPoint.y + verticalSequences(selectedPoint.x)(selectedPoint.y) / 2)
      } else {
        // Sequences has the same length, randomly choose between horizontal or vertical ones
        Rand.r.nextInt(2) match {
          case 0 => // Arbitrary horizontal
            (selectedPoint.x + horizontalSequences(selectedPoint.x)(selectedPoint.y) / 2, selectedPoint.y)
          case 1 => // Arbitrary vertical
            (selectedPoint.x, selectedPoint.y + verticalSequences(selectedPoint.x)(selectedPoint.y) / 2)
        }
      }

    new Point(shotCoordinates._1, shotCoordinates._2)
  }


  /**
    * Returns a matrix of weights bases on a list of matrix weights
    *
    * @param hSeq The matrix of horizontal sequences
    * @param vSeq The matrix of vertical sequences
    * @param distancesToShips The matrix of distances to the nearest ship
    * @param distancesToShots The matrix of distances to the nearest shot
    * @return A matrix of integers representing the weights
    */
  private def weightSquaresToAim(hSeq: Array[Array[Int]],
                                 vSeq: Array[Array[Int]],
                                 distancesToShips: Array[Array[Int]],
                                 distancesToShots: Array[Array[Int]]): Array[Array[Int]] = {
    distancesToShips.indices.map(i => distancesToShips.indices.map(j => {
      if(vSeq(i)(j) == 0 && hSeq(i)(j) == 0) 0
      else /*vSeq(i)(j)*1 + hSeq(i)(j)*1 +*/ distancesToShips(i)(j)*1 + distancesToShots(i)(j)*1
    }).toArray).toArray
  }


}
