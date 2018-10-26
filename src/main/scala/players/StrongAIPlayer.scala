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
    case msg: ChooseGameConfig => onChooseGameConfig(msg)
    case msg: CreateFleet => onCreateFleet(msg)
    case msg: NotifyCanPlay => onNotifyCanPlay(msg)
    case msg: LastRoundResult => onLastRoundResult(msg)

    case _ =>
  }

  // AI state:
  var fleetComposition: Set[GenericShip] = _

  private def onChooseGameConfig(msg: ChooseGameConfig): Unit = {
    msg.nextActor ! new UseGameConfig(self, "MediumAIPlayer")
  }

  private def onCreateFleet(msg: CreateFleet): Unit = {
    msg.nextActor ! new FleetCreated(self, FleetGrid(msg.dimension, msg.ships))
    fleetComposition = msg.ships
  }

  private def onNotifyCanPlay(msg: NotifyCanPlay): Unit = {
    play(msg.nextActor, msg.shotGrid)
  }

  private def onLastRoundResult(msg: LastRoundResult): Unit = {
    msg.result match {
      case Failure(_) => play(msg.sender, msg.shotGrid)
      case _ =>
    }
  }

  private def play(sender: ActorRef, shotGrid: ShotGrid): Unit = {
    // Compute the greatest empty sequences of squares not shot and remove the
    // sequences that cannot contain the alive ships
    val aliveShips = FleetHelper.notSunkShips(fleetComposition, shotGrid)
    val shotMap = FleetHelper.flattenShotMap(shotGrid)
    val verticalSequences = FleetHelper.longestVerticalSequence[Boolean](shotMap, a => !a)
    val horizontalSequences = FleetHelper.longestHorizontalSequence[Boolean](shotMap, a => !a)
    val distancesToNearestShip = FleetHelper.distanceToNearestObstacle[Option[Ship]](FleetHelper.flatten(shotGrid), _.nonEmpty)
    val distancesToNearestShot = FleetHelper.distanceToNearestObstacle[Boolean](FleetHelper.flattenShotMap(shotGrid), p => p)

    val smallestShipSize = aliveShips.minBy(_.size).size

    // Result example: (smallest ship size = 3, [[ 4, 3, 2, 1, 0, 0, 3, 2, 1 ]]) -> [[ 4, 3, 0, 0, 0, 0, 3, 0, 0 ]]
    val filteredVerticalSequences = verticalSequences.map(_.map(x => if(x < smallestShipSize) 0 else x))
    val filteredHorizontalSequences = horizontalSequences.transpose.map(_.map(x => if(x < smallestShipSize) 0 else x)).transpose

    val weights = weightSquaresToAim(filteredHorizontalSequences, filteredVerticalSequences, distancesToNearestShip, distancesToNearestShot)
    val maxWeight = weights.map(_.max).max
    val maxWeightOccurrencesCoordinates: List[Point] = weights.indices.flatMap(i => weights(i).indices.map(j => {
      if(weights(i)(j) == maxWeight) Some(new Point(i, j)) else None
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

    println("Horizontal sequences:")
    FleetHelper.printArray(filteredHorizontalSequences)
    println("Vertical sequences:")
    FleetHelper.printArray(filteredVerticalSequences)
    println("Distances to ships:")
    FleetHelper.printArray(distancesToNearestShip)
    println("Distances to shots:")
    FleetHelper.printArray(distancesToNearestShot)
    println("Weights:")
    FleetHelper.printArray(weights)
    println("Point selected: " + selectedPoint)

    sender ! new Play(self, new Point(shotCoordinates._1, shotCoordinates._2))

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
      else vSeq(i)(j)*1 + hSeq(i)(j)*1 + distancesToShips(i)(j)*1 + distancesToShots(i)(j)*1
    }).toArray).toArray
  }


}
