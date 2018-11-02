package core.messages

import akka.actor.ActorRef
import core.model.FleetGrid

/**
  * This message should be sent to the engine to start the game with both players configurations
  *
  * @param sender The sender of the message
  * @param firstPlayer The player who will start and his fleet
  * @param otherPlayer The second player and his fleet
  */
class StartGame(val sender: ActorRef, val firstPlayer: (ActorRef, FleetGrid, String),
                val otherPlayer: (ActorRef, FleetGrid, String))
