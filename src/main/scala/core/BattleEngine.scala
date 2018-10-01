package core

import java.awt.Point

import akka.actor.{Actor, ActorRef}
import core.messages._
import core.model.{FleetGrid, ShotResult}

import scala.util.{Failure, Try}

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


  private var _battleState: BattleState = _
  private var _parent: ActorRef = _


  private def onStartGame(msg: StartGame): Unit = {
    println("onStartGame")
    _battleState = BattleState(msg.firstPlayer, msg.otherPlayer)
    _battleState.nextTurn._1 ! new GameBegins(_battleState.nextTurn._2, _battleState.targetedTurn._2.toShotGrid)
    _battleState.targetedTurn._1 ! new GameBegins(_battleState.targetedTurn._2, _battleState.nextTurn._2.toShotGrid)
    //_battleState.nextTurn._1 ! new NotifyCanPlay(self, _battleState.targetedTurn._2.toShotGrid)
  }

  private def onPlay(msg: Play): Unit = { // TODO: Refactor again
    println("onPlay")
    val coords: Point = msg.shotCoordinates
    if(_battleState.isNextPlayer(msg.sender)){
      val result: (FleetGrid, Try[ShotResult.Result]) = _battleState.targetedTurn._2.shot(coords)
      // The nextActor receives the result (whether succeeded or failed)
      msg.sender ! new LastRoundResult(self, result._2.map(r => (coords, r)), result._1.toShotGrid)
      result._2.map(r => {
        _battleState.targetedTurn._1 ! new NotifyHasBeenShot(coords, result._1)

        if(_battleState.targetedTurn._2.isDestroyed){
          _battleState.nextTurn._1 ! new GameOver(self, GameEnd.VICTORY, result._1)
          _battleState.targetedTurn._1 ! new GameOver(self, GameEnd.DEFEAT, _battleState.nextTurn._2)
          // TODO: Notify the parent of the end of the game to terminate the actor system
        } else {
          // Update the game state and give the turn to the next player
          _battleState = BattleState((_battleState.targetedTurn._1, result._1), _battleState.nextTurn)
          _battleState.nextTurn._1 ! new NotifyCanPlay(self, _battleState.targetedTurn._2.toShotGrid)
        }
      })
    } else {
      msg.sender ! new LastRoundResult(
        self,
        Failure(new IllegalStateException("This is not your turn!")),
        _battleState.targetedTurn._2.toShotGrid)
    }
  }
}