import java.awt.Dimension

import akka.actor.{ActorSystem, Props}
import core.GameOpponentManager
import core.messages.InitGame
import core.model.GenericShip
import players._
import util.DefaultGameConfig

object HumanVsChosenOpponent extends App {

  val dimensions = new Dimension(2, 2) //DefaultGameConfig.dimensions
  val ships = Set(new GenericShip("tiny", 1))//DefaultGameConfig.ships
  val replays = DefaultGameConfig.replays

  val system = ActorSystem("BattleShip")
  val manager = system.actorOf(Props(classOf[GameOpponentManager], dimensions, ships, replays), "ConfigManager")
  manager ! new InitGame(Props[HumanPlayer])

}
