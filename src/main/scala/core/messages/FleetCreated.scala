package core.messages

import akka.actor.ActorRef
import core.model.FleetGrid

/**
  * Sent by a player to notify that he has created his fleet
  *
  * @param sender The player to inform of the next action
  * @param fleet The fleet created
  */
class FleetCreated(val sender: ActorRef, val fleet: FleetGrid)
