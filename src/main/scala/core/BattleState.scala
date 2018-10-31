package core

import java.awt.Point

import akka.actor.ActorRef
import core.model.{FleetGrid, ShotResult}

import scala.util.{Failure, Success, Try}

/**
  * Store the state of the battle
  *
  * @param nextTurn The next player and its fleet
  * @param targetedTurn The opponent and its fleet, targeted by the next turn
  */
class BattleState(val nextTurn: (ActorRef, FleetGrid), val targetedTurn: (ActorRef, FleetGrid)){

  /**
    * Test whether the given player is the next to play
    *
    * @param player The player to test
    * @return True is it is the player's turn
    */
  def isNextPlayer(player: ActorRef): Boolean = nextTurn._1 == player


  /**
    * Plays the round and return the new battle state.
    *
    * @param player The shooter
    * @param coords The coordinates of the shot
    * @return The new state and the shot result. The state is unchanged if the shot cannot be performed.
    */
  def play(player: ActorRef, coords: Point): (BattleState, FleetGrid, Try[ShotResult.Result]) = {
    if(isNextPlayer(player)){
      val result = this.targetedTurn._2.shot(coords)
      result._2 match {
        case Success(_) => (BattleState((targetedTurn._1, result._1), nextTurn), result._1, result._2)
        case Failure(_) => (this, result._1, result._2)
      }
    } else {
      (this, nextTurn._2, Failure(new IllegalStateException("This is not your turn!")))
    }
  }

}

object BattleState {
  def apply(nextTurn: (ActorRef, FleetGrid), targetedTurn: (ActorRef, FleetGrid)): BattleState =
    new BattleState(nextTurn, targetedTurn)
}
