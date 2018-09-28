package core.model

import java.awt.{Dimension, Point}

/**
  * This class represent the grid to play
  *
  * @param dim The dimensions of the grid
  * @param shotsPerformed All the shots by the grid owner
  */
class ShotGrid(val dim: Dimension, val shotsPerformed: Set[(Point, ShotResult.Result)])

object ShotGrid {
  def apply(dim: Dimension, shotsPerformed: Set[(Point, ShotResult.Result)]): ShotGrid =
    new ShotGrid(dim, shotsPerformed)
}