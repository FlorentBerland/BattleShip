package model.battle

import java.awt.Dimension

/**
  * This class represent the grid to play
  *
  * @param dim The dimensions of the grid
  * @param shotsPerformed All the shots by the grid owner
  */
@deprecated("This class is shit")
class ShotGrid(val dim: Dimension, val shotsPerformed: Set[(Dimension, ShotResult.Result)]) {

// TODO

}

object ShotGrid {
  def apply(dim: Dimension, shotsPerformed: Set[(Dimension, ShotResult.Result)]): ShotGrid =
    new ShotGrid(dim, shotsPerformed)
}