package core

import java.awt.Dimension

import akka.actor.{Actor, InvalidActorNameException, Props}
import core.messages._
import core.model.GenericShip
import players.{HumanPlayer, MediumAIPlayer, StrongAIPlayer, WeakAIPlayer}


/**
  * Manages the initial configuration, meaning the type of opponent
  */
class GameOpponentManager(dimensions: Dimension, ships: Set[GenericShip], replays: Int) extends Actor {

  override def receive: Receive = {
    case msg: InitGame => onInitGame(msg)
    case msg: OpponentChosen => onUseGameConfig(msg)
    case msg: EndOfMatch => onEndOfMatch(msg)
    case msg: QuitGame => onQuitGame(msg)

    case msg: AnyRef => println(msg.getClass.getName)
    case _ =>
  }

  private def onInitGame(msg: InitGame): Unit = {
    context.actorOf(msg.firstPlayerProps, msg.firstPlayerProps.actorClass().getSimpleName) ! new ChooseOpponent(self)
  }

  private def onUseGameConfig(msg: OpponentChosen): Unit = {

    val player2Props = msg.opponent match {
      case "HumanPlayer" => Props[HumanPlayer]
      case "WeakAIPlayer" => Props[WeakAIPlayer]
      case "MediumAIPlayer" => Props[MediumAIPlayer]
      case "StrongAIPlayer" => Props[StrongAIPlayer]
      case _ => Props[WeakAIPlayer]
    }
    val player2 = try {
      context.actorOf(player2Props, msg.opponent)
    } catch {
      case _: InvalidActorNameException => context.actorOf(player2Props, msg.opponent + "2")
    }

    val fleetManager = context.actorOf(Props[GameReplaysManager], "GameReplaysManager")
    fleetManager ! new UseGameConfig(self, msg.player, player2, dimensions, ships, replays)
  }


  private def onEndOfMatch(msg: EndOfMatch): Unit = {
    context stop self
    context.system.terminate
  }


  private def onQuitGame(msg: QuitGame): Unit = {
    context stop self
    context.system.terminate
  }

}
