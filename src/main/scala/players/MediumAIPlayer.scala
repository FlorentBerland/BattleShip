package players

import java.awt.Point

import akka.actor.{Actor, ActorRef}
import core.messages._
import core.model.{FleetGrid, Ship, ShotGrid}
import util.{FleetHelper, Rand}

import scala.util.Failure


/**
  * AI that should shot randomly at the opponent, unless it finds a ship. In this case, it will try to
  * sink it and return to a random fire. This AI chooses a weak AI as opponent if asked to.
  */
class MediumAIPlayer extends Actor {

  override def receive: Receive = {
    case msg: ChooseGameConfig => onChooseGameConfig(msg)
    case msg: CreateFleet => onCreateFleet(msg)
    case msg: NotifyCanPlay => onNotifyCanPlay(msg)
    case msg: LastRoundResult => onLastRoundResult(msg)
    case msg: GameOver => onGameOver(msg)

    case _ =>
  }

  private def onChooseGameConfig(msg: ChooseGameConfig): Unit = {
    msg.nextActor ! new UseGameConfig(self, "WeakAIPlayer")
  }

  private def onCreateFleet(msg: CreateFleet): Unit = {
    msg.nextActor ! new FleetCreated(self, FleetGrid(msg.dimension, msg.ships))
  }

  private def onNotifyCanPlay(msg: NotifyCanPlay): Unit = {
    play(msg.nextActor, msg.shotGrid)
  }

  private def onLastRoundResult(msg: LastRoundResult): Unit = {
    msg.result match {
      case Failure(ex) => ex match {
        case _: IllegalArgumentException => play(msg.sender, msg.shotGrid)
        case _ =>
      }
      case _ =>
    }
  }

  private def onGameOver(msg: GameOver): Unit = {
    msg.sender ! new Replay(self, true)
  }


  private def play(sender: ActorRef, shotGrid: ShotGrid): Unit = {
    val flatFleet = FleetHelper.flatten(shotGrid)

    // Get the longest sequences of horizontally and vertically aligned alive ship squares
    val hAliveSeq = FleetHelper.longestHorizontalSequence[Option[Ship]](flatFleet, !_.getOrElse(Ship()).isDestroyed)
    val vAliveSeq = FleetHelper.longestVerticalSequence[Option[Ship]](flatFleet, !_.getOrElse(Ship()).isDestroyed)

    // Get the longest sequence of each and theirs coordinates
    val (hMax, hX, hY) = FleetHelper.maxValue(hAliveSeq)
    val (vMax, vX, vY) = FleetHelper.maxValue(vAliveSeq)

    val coordinates: Point =
      if(hMax == vMax && hMax == 0){
        new Point(Rand.r.nextInt(shotGrid.dim.width), Rand.r.nextInt(shotGrid.dim.height))
      } else if(hMax >= vMax){
        tryToShootAtHorizontalAlignment(hMax, hX, hY, shotGrid).getOrElse(
          tryToShootAtVerticalAlignment(vMax, vX, vY, shotGrid).getOrElse(
            new Point(Rand.r.nextInt(shotGrid.dim.width), Rand.r.nextInt(shotGrid.dim.height))
          )
        )
      } else {
        tryToShootAtVerticalAlignment(vMax, vX, vY, shotGrid).getOrElse(
          tryToShootAtHorizontalAlignment(hMax, hX, hY, shotGrid).getOrElse(
            new Point(Rand.r.nextInt(shotGrid.dim.width), Rand.r.nextInt(shotGrid.dim.height))
          )
        )
      }

    sender ! new Play(self, coordinates)
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

}
