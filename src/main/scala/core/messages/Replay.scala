package core.messages

import akka.actor.ActorRef


/**
  * Sent by the players at the end of a game to manage the replays
  *
  * @param sender The sender of the message (a player is expected)
  * @param replay True if the player wants to play again
  */
class Replay(val sender: ActorRef, val replay: Boolean)