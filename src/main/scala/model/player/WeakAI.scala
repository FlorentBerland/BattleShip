package model.player
import java.awt.Dimension

import model.BattleEngine
import model.battle.ShotResult

import scala.util.{Failure, Random, Try}

/**
  * Basic AI, should randomly shot at the opponent's fleet
  *
  * @param dim The dimensions of the grid
  * @param engine The battle engine to use for the game
  */
class WeakAI(val dim: Dimension, val engine: BattleEngine) extends Player {

  private val _random = new Random()

  override def lastRoundResult(result: Try[ShotResult.Value]): Unit = result match {
    case Failure(exception) => if(exception.isInstanceOf[IllegalArgumentException]) play()
    case _ =>
  }

  override def notifyHasBeenShot(coordinates: Dimension): Unit = {}

  override def notifyCanPlay(): Unit = play()

  private def play(): Unit =
    engine.play(this, new Dimension(_random.nextInt(dim.width) + 1, _random.nextInt(dim.height) + 1))

}

object WeakAI {
  val NAME: String = "Weak AI"
}
