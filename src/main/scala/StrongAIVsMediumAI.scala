import akka.actor.{ActorSystem, Props}
import core.GameConfigManager
import core.messages.InitGame
import players._
import util.DefaultGameConfig

object StrongAIVsMediumAI extends App {

  val dimensions = DefaultGameConfig.dimensions
  val ships = DefaultGameConfig.ships
  val replays = DefaultGameConfig.replays


  val system = ActorSystem("BattleShip")
  val manager = system.actorOf(Props(classOf[GameConfigManager], dimensions, ships, replays), "ConfigManager")
  manager ! new InitGame(Props[StrongAIPlayer])

}
