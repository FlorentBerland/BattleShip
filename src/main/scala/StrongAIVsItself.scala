import akka.actor.{Actor, ActorSystem, Props}
import core.GameReplaysManager
import core.messages._
import players._
import util.DefaultGameConfig

object StrongAIVsItself extends App {

  ActorSystem("BattleShip").actorOf(Props[StrongAloneController])

}

class StrongAloneController extends Actor {

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
    val strongAi = context.actorOf(Props[StrongAIPlayer], "Strong_AI")
    manager ! new UseGameConfig(self, strongAi, strongAi, dimensions, ships, replays)
  }

}