import java.awt.Dimension

import model.BattleState
import model.battle.{FleetGrid, ShotResult}
import model.player.Player

import scala.util.{Failure, Success}

/**
  * Manage the game and the rounds. This is the only class with an internal state and side effects
  * in its methods
  *
  * @param player1 The player who will shoot first
  * @param player2 The second player
  * @param fleetGrid1 The first player's fleet
  * @param fleetGrid2 The second player's fleet
  */
class BattleEngine(val player1: Player, val player2: Player, val fleetGrid1: FleetGrid, val fleetGrid2: FleetGrid) {

  private var _battleState: BattleState = BattleState((player1, fleetGrid1), (player2, fleetGrid2))


  /**
    * Play a round and modify the internal engine state if the shot succeeded
    *
    * @param player The shooter
    * @param shot The coordinates of the shot
    */
  def play(player: Player, shot: Dimension): Unit = { // FIXME : Refactor the code
    if(_battleState.isNextPlayer(player)){
      if(_battleState.targetedTurn._2.canShot(shot)){
        val (shotFleet: FleetGrid, shotResult: ShotResult.Value) = _battleState.targetedTurn._2.shot(shot)
        _battleState.nextTurn._1.lastRoundResult(Success(shotResult))
        _battleState.targetedTurn._1.notifyHasBeenShot(shot)

        // Update the game state and give the turn to the next player
        _battleState = BattleState((_battleState.targetedTurn._1, shotFleet), _battleState.nextTurn)
        _battleState.nextTurn._1.notifyCanPlay()

      } else {
        _battleState.nextTurn._1.lastRoundResult(
          Failure(new IllegalArgumentException("Invalid coordinates: " + shot.toString)))
      }
    } else {
      _battleState.nextTurn._1.lastRoundResult(Failure(new IllegalStateException("This is not your turn!")))
    }
  }

}

object BattleEngine {
  def apply(player1: Player, player2: Player, fleetGrid1: FleetGrid, fleetGrid2: FleetGrid): BattleEngine =
    new BattleEngine(player1, player2, fleetGrid1, fleetGrid2)
}