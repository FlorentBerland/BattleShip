package model.player

import java.awt.Point

import model.battle.ShotResult

import scala.util.Try

trait Player {

  /**
    * This method is invoked to notify the player that it is his turn to play.
    * For an AI implementation, it should trigger the AI turn
    */
  def notifyCanPlay(): Unit


  /**
    * Notify the player of the result of its last round
    *
    * @param result The result of the shot performed
    */
  def lastRoundResult(result: Try[ShotResult.Value]): Unit


  /**
    * Notify the player of the shot of the other player
    *
    * @param coordinates The coordinates of the shot
    */
  def notifyHasBeenShot(coordinates: Point): Unit

}
