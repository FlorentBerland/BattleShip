package model.battle

import java.awt.Dimension

/**
  * The current fleet of a player
  *
  * @param dim The size of the grid
  * @param ships The ships of the player
  * @param shotsReceived A set of shot coordinates performed on this grid
  */
class FleetGrid(val dim: Dimension, val ships: Set[Ship], val shotsReceived: Set[Dimension]) {

  /**
    * Return a fleet grid with the shot applied on the current instance
    *
    * @param coordinates The coordinates of the shot
    * @return A grid with the shot applied. Unmodified if the shot miss
    */
  def shot(coordinates: Dimension): (FleetGrid, ShotResult.Result) = {
    val resultByShip: Set[(Ship, ShotResult.Value)] = ships.map(ship => {
      val shotShip = ship shot coordinates
      (shotShip,
        if(ship.aliveSquares != shotShip.aliveSquares){
          if(shotShip.isDestroyed)
            ShotResult.HIT_AND_SINK
          else
            ShotResult.HIT
        } else ShotResult.MISS)
    })
    (FleetGrid(dim, resultByShip.map(_._1), shotsReceived + coordinates), resultByShip.map(_._2).max)
  }


  /**
    * Test whether the shot is correct (meaning it is in the grid and the square has not already been shot)
    *
    * @param coordinates The coordinates of the shot
    * @return True if the shot can be performed
    */
  def canShot(coordinates: Dimension): Boolean =
    coordinates.width > 0 && coordinates.height > 0 &&
    coordinates.width <= dim.width && coordinates.height <= dim.height &&
    shotsReceived.forall(square => square.width != coordinates.width || square.height != coordinates.height)


  /**
    * Indicate if all the ships are destroyed, meaning the battle is lost for the
    * FleetGrid owner
    *
    * @return True if all the ships are destroyed, false otherwise
    */
  def isDestroyed: Boolean = ships.forall(_.isDestroyed)

}

object FleetGrid {
  def apply(dim: Dimension, ships: Set[Ship], shotsReceived: Set[Dimension]) = new FleetGrid(dim, ships, shotsReceived)
}