package players

import java.awt.{Dimension, Point}

import akka.actor.{Actor, ActorRef}
import core.messages._

import scala.util.{Failure, Random}


/**
  * Basic AI, should randomly shot at the opponent's fleet
  */
class WeakAIPlayer extends Actor {

  override def receive: Receive = {
    case msg: CreateFleet => onCreateFleet(msg)
    case msg: NotifyCanPlay => onNotifyCanPlay(msg)
    case msg: LastRoundResult => onLastRoundResult(msg)

    case _ =>
  }

  private val _random = new Random()
  private var _dim: Dimension = _

  private def onCreateFleet(msg: CreateFleet): Unit = {
    _dim = msg.dimension
    // TODO: Implement the fleet building or delegate it to another class/actor
  }

  private def onNotifyCanPlay(msg: NotifyCanPlay): Unit = {
    play(msg.sender)
  }

  private def onLastRoundResult(msg: LastRoundResult): Unit = {
    msg.result match {
      case Failure(_) => play(msg.sender)
      case _ =>
    }
  }

  private def play(sender: ActorRef): Unit = {
    sender ! new Play(self, new Point(_random.nextInt(_dim.width + 1), _random.nextInt(_dim.height + 1)))
  }

}

object WeakAIPlayer {
  val NAME: String = "Weak AI"
}
