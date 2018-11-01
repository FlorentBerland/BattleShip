import akka.actor.{ActorSystem, Props}
import core.GameConfigManager
import core.messages.InitGame
import players._

object MediumAIVsWeakAI extends App {

  val system = ActorSystem("BattleShip")
  val manager = system.actorOf(Props[GameConfigManager], "ConfigManager")
  manager ! new InitGame(Props[MediumAIPlayer])

}
