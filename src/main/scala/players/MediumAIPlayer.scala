package players

import java.awt.Point

import akka.actor.{Actor, ActorRef}
import core.messages._
import core.model.{FleetGrid, ShotGrid}
import util.Rand

import scala.util.Failure


/**
  * AI that should shot randomly at the opponent, unless it finds a ship. In this case, it will try to
  * sink it and return to a random fire. This AI chooses a weak AI as opponent if asked to.
  */
class MediumAIPlayer extends Actor {

  override def receive: Receive = {
    case msg: ChooseGameConfig => onChooseGameConfig(msg)
    case msg: CreateFleet => onCreateFleet(msg)
    case msg: GameBegins => onGameBegins(msg)
    case msg: NotifyCanPlay => onNotifyCanPlay(msg)
    case msg: LastRoundResult => onLastRoundResult(msg)

    case _ =>
  }

  private def onChooseGameConfig(msg: ChooseGameConfig): Unit = {
    msg.nextActor ! new UseGameConfig(self, "WeakAIPlayer")
  }

  private def onCreateFleet(msg: CreateFleet): Unit = {
    msg.nextActor ! new FleetCreated(self, FleetGrid(msg.dimension, msg.ships))
  }

  private def onGameBegins(msg: GameBegins): Unit = {
    // TODO: Reset the AI state
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
    // FIXME : aim smartly
    sender ! new Play(self, new Point(Rand.r.nextInt(shotGrid.dim.width + 1), Rand.r.nextInt(shotGrid.dim.height + 1)))
  }

}
