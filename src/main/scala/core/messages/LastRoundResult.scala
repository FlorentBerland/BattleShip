package core.messages

import java.awt.Point

import akka.actor.ActorRef
import core.model.{ShotGrid, ShotResult}

import scala.util.Try


/**
  * Sent to a player to notify him of the result of a shot
  *
  * @param sender The nextActor of the message
  * @param result The shot result or Failure if the shot was not valid
  * @param shotGrid The fleet after the last round
  */
class LastRoundResult(val sender: ActorRef, val result: Try[(Point, ShotResult.Value)], val shotGrid: ShotGrid)
