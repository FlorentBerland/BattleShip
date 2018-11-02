package core.messages

import core.model.FleetGrid

/**
  * Sent by a player to notify that he has created his fleet
  *
  * @param playerId The player's id
  * @param fleet The fleet created
  */
class FleetCreated(val playerId: String, val fleet: FleetGrid)
