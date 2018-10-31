package core.messages

import akka.actor.ActorRef

/**
  * Sent by the GameEngine to inform of the winner of a game
  *
  * @param winner The player who won the game
  */
class GameFinished(val winner: ActorRef)
