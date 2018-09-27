package core

import akka.actor.ActorRef
import core.model.battle.FleetGrid

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

}

object BattleState {
  def apply(nextTurn: (ActorRef, FleetGrid), targetedTurn: (ActorRef, FleetGrid)): BattleState =
    new BattleState(nextTurn, targetedTurn)
}
