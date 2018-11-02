package players

import java.awt.Point

import akka.actor.{Actor, ActorRef}
import core.messages._
import core.model.{FleetGrid, ShotGrid}
import util.Rand

import scala.util.Failure


/**
  * Basic AI, should randomly shot at the opponent's fleet. This AI is shy an forfeits the fight
  * if asked to choose an opponent
  */
class WeakAIPlayer extends Actor {

  override def receive: Receive = {
    case msg: ChooseOpponent => onChooseGameConfig(msg)
    case msg: CreateFleet => onCreateFleet(msg)
    case msg: NotifyCanPlay => onNotifyCanPlay(msg)
    case msg: LastRoundResult => onLastRoundResult(msg)
    case msg: GameOver => onGameOver(msg)

    case _ =>
  }

  private def onChooseGameConfig(msg: ChooseOpponent): Unit = {
    msg.nextActor ! new QuitGame("")
  }

  private def onCreateFleet(msg: CreateFleet): Unit = {
    msg.nextActor ! new FleetCreated(msg.playerId, FleetGrid(msg.dimension, msg.ships))
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

  private def play(sender: ActorRef, shotGrid: ShotGrid, id: String): Unit = {
    sender ! new Play(id, new Point(Rand.r.nextInt(shotGrid.dim.width), Rand.r.nextInt(shotGrid.dim.height)))
  }

  private def onGameOver(msg: GameOver): Unit = {
    msg.sender ! new Replay(msg.playerId, true)
  }

}