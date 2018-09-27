package player

import core.messages._


/**
  * Basic AI, should randomly shot at the opponent's fleet
  */
class WeakAI extends Player {

  override def onCreateFleet(msg: CreateFleet): Unit = ???

  override def onNotifyCanPlay(msg: NotifyCanPlay): Unit = ???

  override def onLastRoundResult(msg: LastRoundResult): Unit = ???

  override def onNotifyHasBeenShot(msg: NotifyHasBeenShot): Unit = ???

  override def onGameEnd(msg: GameEnd): Unit = ???

}

object WeakAI {
  val NAME: String = "Weak AI"
}
