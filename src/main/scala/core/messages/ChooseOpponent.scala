package core.messages

import akka.actor.ActorRef

/**
  * Sent to the first player of a game to let him choose how would the game be
  *
  * @param nextActor The actor to give the game when chosen
  */
class ChooseOpponent(val nextActor: ActorRef)
