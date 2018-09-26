package model

import model.battle.FleetGrid
import model.player.Player

/**
  * Store the state of the battle
  *
  * @param nextTurn The next player and its fleet
  * @param targetedTurn The opponent and its fleet, targeted by the next turn
  */
class BattleState(val nextTurn: (Player, FleetGrid), val targetedTurn: (Player, FleetGrid)){

  /**
    * Test whether the given player is the next to play
    *
    * @param player The player to test
    * @return True is it is the player's turn
    */
  def isNextPlayer(player: Player): Boolean = nextTurn._1 == player

}

object BattleState {
  def apply(nextTurn: (Player, FleetGrid), targetedTurn: (Player, FleetGrid)): BattleState =
    new BattleState(nextTurn, targetedTurn)
}
