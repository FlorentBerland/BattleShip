package core

import akka.actor.{Actor, Props}
import core.messages._
import players.{HumanPlayer, MediumAIPlayer, StrongAIPlayer, WeakAIPlayer}
import util.DefaultGameConfig

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.Duration

/**
  * Manages the initial configuration, meaning the type of opponent
  */
class GameConfigManager extends Actor {

  override def receive: Receive = {
    case msg: InitGame => onInitGame(msg)
    case msg: UseGameConfig => onUseGameConfig(msg)
    case msg: QuitGame => onQuitGame(msg)

    case msg: AnyRef => println(msg.getClass.getName)
    case _ =>
  }

  private def onInitGame(msg: InitGame): Unit = {
    context.actorOf(msg.firstPlayerProps, msg.firstPlayerProps.actorClass().getSimpleName) ! new ChooseGameConfig(self)
  }

  private def onUseGameConfig(msg: UseGameConfig): Unit = {
    val ships = DefaultGameConfig.ships
    val dimensions = DefaultGameConfig.dimensions

    val fleetManager = context.actorOf(Props[GameFleetManager], "FleetManager")
    fleetManager ! new CreateFleet(self, dimensions, ships)

    context.actorOf(msg.opponent match {
      case "HumanPlayer" => Props[HumanPlayer]
      case "WeakAIPlayer" => Props[WeakAIPlayer]
      case "MediumAIPlayer" => Props[MediumAIPlayer]
      case "StrongAIPlayer" => Props[StrongAIPlayer]
      case _ => Props[WeakAIPlayer]
    }, msg.opponent) ! new CreateFleet(fleetManager, dimensions, ships)
    msg.nextActor ! new CreateFleet(fleetManager, dimensions, ships)
  }


  private def onQuitGame(msg: QuitGame): Unit = {
    context stop msg.sender
    implicit val executionContext: ExecutionContext = context.system.dispatcher
    context.system.scheduler.scheduleOnce(Duration.Zero)(System.exit(0))
  }

}
