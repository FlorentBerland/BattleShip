package core.model

import java.awt.{Dimension, Point}

/**
  * This class represent the grid to play. It only contains the information
  * the player should know about his opponent's grid
  *
  * @param dim The dimensions of the grid
  * @param shotsPerformed All the shots by the grid owner
  */
class ShotGrid(val dim: Dimension, val shotsPerformed: Set[(Point, ShotResult.Result)]){


  /**
    * Transform this grind into a heuristic fleet grid
    * (the representation of the opponent's fleet)
    */
  def toHeuristicFleetGrid: FleetGrid = {
    FleetGrid(dim, shotsPerformed.map(shot => {
      if(shot._2 >= ShotResult.HIT)
        Some(Ship(Set((shot._1, false))))
      else None
    }).map(_.get), shotsPerformed.map(_._1))
  }

}

object ShotGrid {
  def apply(dim: Dimension, shotsPerformed: Set[(Point, ShotResult.Result)]): ShotGrid =
    new ShotGrid(dim, shotsPerformed)
}