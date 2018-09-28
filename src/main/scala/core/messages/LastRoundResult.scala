package core.messages

import java.awt.Point

import akka.actor.ActorRef
import core.model.ShotResult

import scala.util.Try


/**
  * Sent to a player to notify him of the result of a shot
  *
  * @param sender The sender of the message
  * @param result The shot result or Failure if the shot was not valid
  */
class LastRoundResult(val sender: ActorRef, val result: Try[(Point, ShotResult.Value)])
