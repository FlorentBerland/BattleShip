package core.messages

import java.awt.Point

import core.model.battle.ShotResult

import scala.util.Try


/**
  * Sent to a player to notify him of the result of a shot
  *
  * @param result The shot result or Failure if the shot was not valid
  */
class LastRoundResult(result: Try[(Point, ShotResult.Value)])
