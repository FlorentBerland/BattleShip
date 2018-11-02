import akka.actor.{ActorSystem, Props}
import core.GameReplaysManager
import core.messages._
import players._
import util.DefaultGameConfig

object StrongAIVsWeakAI extends App {

  val dimensions = DefaultGameConfig.dimensions
  val ships = DefaultGameConfig.ships
  val replays = DefaultGameConfig.replays


  val system = ActorSystem("BattleShip")
  val manager = system.actorOf(Props[GameReplaysManager], "ConfigManager")
  val strongAi = system.actorOf(Props[StrongAIPlayer], "Strong_AI")
  val weakAi = system.actorOf(Props[WeakAIPlayer], "Weak_AI")
  manager ! new UseGameConfig(null, weakAi, strongAi, dimensions, ships, replays)

}
