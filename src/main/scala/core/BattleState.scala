package core

import java.awt.Point

import akka.actor.ActorRef
import core.model.{FleetGrid, ShotResult}

import scala.util.{Failure, Success, Try}

/**
  * Store the state of the battle
  *
  * @param nextTurn The next player's id, his fleet and his uid
  * @param targetedTurn The opponent's id, his fleet and his uid, targeted by the next turn
  */
class BattleState(val nextTurn: (ActorRef, FleetGrid, String), val targetedTurn: (ActorRef, FleetGrid, String)){

  /**
    * Test whether the given player is the next to play
    *
    * @param playerId The player to test
    * @return True is it is the player's turn
    */
  def isNextPlayer(playerId: String): Boolean = nextTurn._3 == playerId


  /**
    * Plays the round and return the new battle state.
    *
    * @param playerId The shooter's id
    * @param coords The coordinates of the shot
    * @return The new state and the shot result. The state is unchanged if the shot cannot be performed.
    */
  def play(playerId: String, coords: Point): (BattleState, FleetGrid, Try[ShotResult.Result]) = {
    if(isNextPlayer(playerId)){
      val result = this.targetedTurn._2.shot(coords)
      result._2 match {
        case Success(_) => (BattleState((targetedTurn._1, result._1, targetedTurn._3), nextTurn), result._1, result._2)
        case Failure(_) => (this, result._1, result._2)
      }
    } else {
      (this, nextTurn._2, Failure(new IllegalStateException("This is not your turn!")))
    }
  }

}

object BattleState {
  def apply(nextTurn: (ActorRef, FleetGrid, String), targetedTurn: (ActorRef, FleetGrid, String)): BattleState =
    new BattleState(nextTurn, targetedTurn)
}
