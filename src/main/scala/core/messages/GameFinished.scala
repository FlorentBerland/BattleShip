package core.messages

import akka.actor.ActorRef

/**
  * Sent by the GameEngine to inform of the winner of a game
  *
  * @param sender The sender of the message
  * @param winner The player who won the game
  */
class GameFinished(val sender: ActorRef, val winner: ActorRef)
