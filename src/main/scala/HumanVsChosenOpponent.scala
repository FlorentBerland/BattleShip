import java.awt.Dimension

import akka.actor.{ActorSystem, Props}
import core.GameOpponentManager
import core.messages.InitGame
import players._
import util.DefaultGameConfig

object HumanVsChosenOpponent extends App {

  val dimensions = DefaultGameConfig.dimensions
  val ships = DefaultGameConfig.ships
  val replays = DefaultGameConfig.replays

  val system = ActorSystem("BattleShip")
  val manager = system.actorOf(Props(classOf[GameOpponentManager], dimensions, ships, replays), "ConfigManager")
  manager ! new InitGame(Props[HumanPlayer])

}
