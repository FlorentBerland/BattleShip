package core.messages

import akka.actor.ActorRef
import core.model.battle.FleetGrid

/**
  * This message should be sent to the engine to start the game with both players configurations
  *
  * @param firstPlayer The player who will start and his fleet
  * @param otherPlayer The second player and his fleet
  */
class StartGame(val firstPlayer: (ActorRef, FleetGrid), val otherPlayer: (ActorRef, FleetGrid))
