package core.messages

import akka.actor.ActorRef
import core.model.FleetGrid

/**
  * Sent by a player to notify that he has created his fleet
  *
  * @param nextActor The player to inform of the next action
  * @param fleet The fleet created
  */
class FleetCreated(val nextActor: ActorRef, val fleet: FleetGrid)
