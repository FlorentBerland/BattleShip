package core.messages

import akka.actor.ActorRef
import core.GameOver
import core.model.battle.FleetGrid


/**
  * Notify a player of the end of a game
  *
  * @param sender The sender of the message
  * @param end The end of the game
  * @param opponentFleet The final fleet of the opponent
  */
class GameEnd(val sender: ActorRef, val end: GameOver.End, val opponentFleet: FleetGrid)