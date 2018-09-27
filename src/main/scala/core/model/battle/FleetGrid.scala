package core.model.battle

import java.awt.{Dimension, Point}

import scala.util.{Failure, Success, Try}

/**
  * The current fleet of a player
  *
  * @param dim The size of the grid
  * @param ships The ships of the player
  * @param shotsReceived A set of shot coordinates performed on this grid
  */
class FleetGrid(val dim: Dimension, val ships: Set[Ship], val shotsReceived: Set[Point]) {

  /**
    * Return a fleet grid with the shot applied on the current instance
    *
    * @param coordinates The coordinates of the shot
    * @return Success with a fleet with the shot applied. Unmodified if the shot miss. Return
    *         Failure if the shot cannot be performed
    */
  def shot(coordinates: Point): Try[(FleetGrid, ShotResult.Result)] = {
    if(!canShot(coordinates))
      Failure(new IllegalArgumentException("Cannot shoot at coordinates " + coordinates))
    else {
      val resultByShip: Set[(Ship, ShotResult.Value)] = ships.map(ship => {
        val shotShip = ship shot coordinates
        (shotShip,
          if (ship.aliveSquares != shotShip.aliveSquares) {
            if (shotShip.isDestroyed)
              ShotResult.HIT_AND_SINK
            else
              ShotResult.HIT
          } else ShotResult.MISS)
      })
      Success((FleetGrid(dim, resultByShip.map(_._1), shotsReceived + coordinates), resultByShip.map(_._2).max))
    }
  }


  /**
    * Test whether the shot is correct (meaning it is in the grid and the square has not already been shot)
    *
    * @param coordinates The coordinates of the shot
    * @return True if the shot can be performed
    */
  def canShot(coordinates: Point): Boolean =
    coordinates.x > 0 && coordinates.y > 0 &&
    coordinates.x <= dim.width && coordinates.y <= dim.height &&
    shotsReceived.forall(square => square.x != coordinates.x || square.y != coordinates.y)


  /**
    * Indicate if all the ships are destroyed, meaning the battle is lost for the
    * FleetGrid owner
    *
    * @return True if all the ships are destroyed, false otherwise
    */
  def isDestroyed: Boolean = ships.forall(_.isDestroyed)

}

object FleetGrid {
  def apply(dim: Dimension, ships: Set[Ship], shotsReceived: Set[Point]) = new FleetGrid(dim, ships, shotsReceived)
}