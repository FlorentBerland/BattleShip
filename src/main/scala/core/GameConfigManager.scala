package core

import akka.actor.{Actor, Props}
import core.messages._
import players.{HumanPlayer, WeakAIPlayer}
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
    println("onInitGame")
    context.actorOf(msg.firstPlayerProps) ! new ChooseGameConfig(self)
  }

  private def onUseGameConfig(msg: UseGameConfig): Unit = {
    println("onUseGameConfig")
    val fleetManager = context.actorOf(Props[GameFleetManager], "FleetManager")
    context.actorOf(msg.opponent match {
      case "HumanPlayer" => Props[HumanPlayer]
      case "WeakAIPlayer" => Props[WeakAIPlayer]
      case _ => Props[WeakAIPlayer]
    }) ! new CreateFleet(fleetManager, DefaultGameConfig.dimensions, DefaultGameConfig.ships)
    msg.nextActor ! new CreateFleet(fleetManager, DefaultGameConfig.dimensions, DefaultGameConfig.ships)
    //context stop self
  }


  private def onQuitGame(msg: QuitGame): Unit = {
    println("onQuitGame")
    context stop msg.sender
    implicit val executionContext: ExecutionContext = context.system.dispatcher
    context.system.scheduler.scheduleOnce(Duration.Zero)(System.exit(0))
  }

}
