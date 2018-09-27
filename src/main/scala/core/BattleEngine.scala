package core

import java.awt.Point

import akka.actor.{Actor, ActorRef}
import core.messages._
import core.model.battle.{FleetGrid, ShotResult}

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
    _battleState = BattleState(msg.firstPlayer, msg.otherPlayer)
  }

  private def onPlay(msg: Play): Unit = {
    val coords: Point = msg.shotCoordinates
    if(_battleState.isNextPlayer(msg.sender)){
      val result: Try[(FleetGrid, ShotResult.Result)] = _battleState.targetedTurn._2.shot(coords)
      // The sender receives the result (whether succeeded or failed)
      msg.sender ! new LastRoundResult(result.map(r => (coords, r._2)))
      result.map(r => {
        _battleState.targetedTurn._1 ! new NotifyHasBeenShot(coords)

        if(_battleState.targetedTurn._2.isDestroyed){
          _battleState.nextTurn._1 ! new GameEnd(self, GameOver.VICTORY, r._1)
          _battleState.targetedTurn._1 ! new GameEnd(self, GameOver.DEFEAT, _battleState.nextTurn._2)
          // TODO: Notify the parent of the end of the game to terminate the actor system
        } else {
          // Update the game state and give the turn to the next player
          _battleState = BattleState((_battleState.targetedTurn._1, r._1), _battleState.nextTurn)
          _battleState.nextTurn._1 ! new NotifyCanPlay(self)
        }
      })
    } else {
      msg.sender ! new LastRoundResult(Failure(new IllegalStateException("This is not your turn!")))
    }
  }
}

object BattleEngine {
  def apply: BattleEngine = new BattleEngine
}