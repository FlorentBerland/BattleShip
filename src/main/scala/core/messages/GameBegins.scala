package core.messages

import core.model.{FleetGrid, ShotGrid}


/**
  * Sent to a player to notify him the game is starting
  *
  * @param fleet The player's fleet
  * @param shotGrid The opponent's grid to display to the player
  */
class GameBegins(val fleet: FleetGrid, val shotGrid: ShotGrid)
