package core.messages

import akka.actor.ActorRef


/**
  * Sent by a player to choose its opponent
  *
  * @param player The sender of the message (should be a player)
  * @param opponent The class name of the opponent chosen
  */
class OpponentChosen(val player: ActorRef, val opponent: String)
