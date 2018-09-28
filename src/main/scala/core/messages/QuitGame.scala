package core.messages

import akka.actor.ActorRef

/**
  * Sent by a player who quit the game to warn the receiver of the message
  *
  * @param sender The actor (player) who quit the game
  */
class QuitGame(val sender: ActorRef)
