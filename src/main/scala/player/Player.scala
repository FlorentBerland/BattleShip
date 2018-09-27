package player

import akka.actor.Actor
import core.messages._

trait Player extends Actor{

  override def receive: Receive = {
    case msg: CreateFleet => onCreateFleet(msg)
    case msg: NotifyCanPlay => onNotifyCanPlay(msg)
    case msg: LastRoundResult => onLastRoundResult(msg)
    case msg: NotifyHasBeenShot => onNotifyHasBeenShot(msg)
    case msg: GameEnd => onGameEnd(msg)

    case _ =>
  }

  /**
    * The player should create his fleet and send it back
    */
  def onCreateFleet(msg: CreateFleet): Unit


  /**
    * This method is invoked to notify the player that it is his turn to play.
    * For an AI implementation, it should trigger the AI turn
    */
  def onNotifyCanPlay(msg: NotifyCanPlay): Unit


  /**
    * Notify the player of the result of his last round
    */
  def onLastRoundResult(msg: LastRoundResult): Unit


  /**
    * Notify the player of the shot of the other player
    */
  def onNotifyHasBeenShot(msg: NotifyHasBeenShot): Unit


  /**
    * Notify the player that the game is over
    */
  def onGameEnd(msg: GameEnd): Unit

}
