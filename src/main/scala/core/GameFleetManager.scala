package core

import akka.actor.{Actor, ActorRef, Props}
import core.messages._
import core.model.FleetGrid

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.Duration

class GameFleetManager extends Actor {

  override def receive: Receive = {
    case msg: FleetCreated => onFleetCreated(msg)
    case msg: QuitGame => onQuitGame(msg)

    case msg: AnyRef => println(msg.getClass.getName)
    case _ =>
  }

  private var _playersToFleet = Map.empty[ActorRef, FleetGrid]
  private var _hasToQuit = false

  private def onFleetCreated(msg: FleetCreated): Unit = {
    println("onFleetCreated")
    if(_hasToQuit) {
      println("has to quit")
      context stop msg.nextActor
      context stop self
      implicit val executionContext: ExecutionContext = context.system.dispatcher
      context.system.scheduler.scheduleOnce(Duration.Zero)(System.exit(0))
    } else {
      if (!_playersToFleet.contains(msg.nextActor))
        _playersToFleet = _playersToFleet + (msg.nextActor -> msg.fleet)
      if (_playersToFleet.size == 2) {
        val gameEngine = context.actorOf(Props[BattleEngine])
        gameEngine ! new StartGame(_playersToFleet.head, _playersToFleet.last)
        _playersToFleet.map(keyValue => keyValue._1 ! new ReadyToStartGame(gameEngine))
      }
    }
  }

  private def onQuitGame(msg: QuitGame): Unit = {
    println("onQuitGame")
    context stop msg.sender
    _hasToQuit = true
    implicit val executionContext: ExecutionContext = context.system.dispatcher
    context.system.scheduler.scheduleOnce(Duration.Zero)(System.exit(0))
  }

}
