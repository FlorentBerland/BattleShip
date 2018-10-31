package core

import akka.actor.{Actor, ActorRef}
import core.messages._

import scala.util.{Failure, Success}

/**
  * Manage the game and the rounds. This is the only class with an internal state and side effects
  * in its methods
  */
class BattleEngine extends Actor {

  override def receive: Receive = {
    case msg: StartGame => onStartGame(msg)
    case msg: Play => onPlay(msg)

    case _ =>
  }


  // Internal state:
  private var _battleState: BattleState = _
  private var _retriesManager: ActorRef = _


  private def onStartGame(msg: StartGame): Unit = {
    _retriesManager = msg.sender
    _battleState = BattleState(msg.firstPlayer, msg.otherPlayer)
    _battleState.nextTurn._1 ! new GameBegins(self, _battleState.nextTurn._2, _battleState.targetedTurn._2.toOpponentGrid)
    _battleState.targetedTurn._1 ! new GameBegins(self, _battleState.targetedTurn._2, _battleState.nextTurn._2.toOpponentGrid)
    _battleState.nextTurn._1 ! new NotifyCanPlay(self, _battleState.targetedTurn._2.toOpponentGrid)
  }

  private def onPlay(msg: Play): Unit = {
    val (newState, newFleet, shotResult) = _battleState.play(msg.sender, msg.shotCoordinates)
    shotResult match {
      case Success(_) =>
        newState.targetedTurn._1 ! new LastRoundResult(self, msg.shotCoordinates, shotResult, newFleet.toOpponentGrid)
        newState.nextTurn._1 ! new NotifyHasBeenShot(msg.shotCoordinates, newFleet)
        if(newState.nextTurn._2.isDestroyed){
          newState.targetedTurn._1 ! new GameOver(_retriesManager, GameEnd.VICTORY, newFleet)
          newState.nextTurn._1 ! new GameOver(_retriesManager, GameEnd.DEFEAT, newState.targetedTurn._2)
          _retriesManager ! new GameFinished(newState.targetedTurn._1)
        } else {
          newState.nextTurn._1 ! new NotifyCanPlay(self, newState.targetedTurn._2.toOpponentGrid)
        }
      case Failure(_) =>
        msg.sender ! new LastRoundResult(self, msg.shotCoordinates, shotResult, newFleet.toOpponentGrid)
    }
    _battleState = newState
  }
}