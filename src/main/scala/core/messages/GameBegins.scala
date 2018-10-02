package core.messages

import akka.actor.ActorRef
import core.model.{FleetGrid, ShotGrid}


/**
  * Sent to a player to notify him the game is starting
  *
  * @param nextActor The actor to call back on player's turn
  * @param fleet The player's fleet
  * @param shotGrid The opponent's grid to display to the player
  */
class GameBegins(val nextActor: ActorRef, val fleet: FleetGrid, val shotGrid: ShotGrid)
