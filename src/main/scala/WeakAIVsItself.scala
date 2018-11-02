import akka.actor.{Actor, ActorSystem, Props}
import core.GameReplaysManager
import core.messages._
import players._
import util.DefaultGameConfig

object WeakAIVsItself extends App {

  ActorSystem("BattleShip").actorOf(Props[WeakAloneController])

}

class WeakAloneController extends Actor {

  override def receive: Receive = {
    case msg: EndOfMatch =>
      context stop msg.sender
      context stop self
      context.system.terminate
  }


  private val dimensions = DefaultGameConfig.dimensions
  private val ships = DefaultGameConfig.ships
  private val replays = DefaultGameConfig.replays

  override def preStart(): Unit = {
    super.preStart
    val manager = context.actorOf(Props[GameReplaysManager], "ConfigManager")
    val weakAi = context.actorOf(Props[WeakAIPlayer], "Weak_AI")
    manager ! new UseGameConfig(self, weakAi, weakAi, dimensions, ships, replays)
  }

}