package core.messages

import java.awt.Point

import akka.actor.ActorRef
import core.model.{ShotGrid, ShotResult}

import scala.util.Try


/**
  * Sent to a player to notify him of the result of a shot
  *
  * @param sender The sender of the message
  * @param result The shot result or Failure if the shot was not valid
  * @param shotGrid The fleet after the last round
  * @param playerId The id of the player
  */
class LastRoundResult(val sender: ActorRef, val coords: Point,
                      val result: Try[ShotResult.Value], val shotGrid: ShotGrid,
                      val playerId: String)
