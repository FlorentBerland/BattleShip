package core.messages

import akka.actor.ActorRef
import core.GameEnd
import core.model.FleetGrid


/**
  * Notify a player of the end of a game
  *
  * @param sender The sender of the message
  * @param end The end of the game
  * @param opponentFleet The final fleet of the opponent
  */
class GameOver(val sender: ActorRef, val end: GameEnd.End, val opponentFleet: FleetGrid)