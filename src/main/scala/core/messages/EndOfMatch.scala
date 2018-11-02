package core.messages

import akka.actor.ActorRef

/**
  * Sent back by the GameReplaysManager with the results of a match
  *
  * @param sender The sender of the message
  * @param player1 The first player
  * @param score1 First player's score
  * @param player2 The second player
  * @param score2 Second player's score
  */
class EndOfMatch(val sender: ActorRef, val player1: ActorRef, val score1: Int, val player2: ActorRef, val score2: Int)
