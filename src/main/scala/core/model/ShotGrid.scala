package core.model

import java.awt.{Dimension, Point}

/**
  * This class represent the grid to play. It only contains the information
  * the player should know about his opponent's grid
  *
  * @param dim The dimensions of the grid
  * @param shipsFound The ships, fully or partially revealed during the game
  * @param shotsPerformed The shots performed by the player
  */
class ShotGrid(val dim: Dimension, val shipsFound: Set[Ship], val shotsPerformed: Set[Point]){


  /**
    * Transform this grid into a FleetGrid for the display
    */
  def toFleetGrid: FleetGrid = FleetGrid(dim, shipsFound, shotsPerformed)

}

object ShotGrid {
  def apply(dim: Dimension, shipsFound: Set[Ship], shotsPerformed: Set[Point]): ShotGrid =
    new ShotGrid(dim, shipsFound, shotsPerformed)
}
