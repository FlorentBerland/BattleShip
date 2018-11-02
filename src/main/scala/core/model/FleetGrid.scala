package core.model

import java.awt.{Dimension, Point}

import util.{FleetHelper, Rand}

import scala.annotation.tailrec
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
    *         Failure with IllegalArgumentException if the shot cannot be performed
    */
  def shot(coordinates: Point): (FleetGrid, Try[ShotResult.Result]) = {
    if(!canShot(coordinates))
      (this, Failure(new IllegalArgumentException("Cannot shoot at coordinates " + coordinates)))
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
      (FleetGrid(dim, resultByShip.map(_._1), shotsReceived + coordinates), Success(resultByShip.map(_._2).max))
    }
  }


  /**
    * Test whether the shot is correct (meaning it is in the grid and the square has not already been shot)
    *
    * @param coordinates The coordinates of the shot
    * @return True if the shot can be performed
    */
  def canShot(coordinates: Point): Boolean =
    coordinates.x >= 0 && coordinates.y >= 0 &&
    coordinates.x < dim.width && coordinates.y < dim.height &&
    shotsReceived.forall(square => !square.equals(coordinates))


  /**
    * Keep only the information the opponent should know about the fleet
    */
  def toOpponentGrid: ShotGrid = {
    ShotGrid(dim, ships.flatMap(ship => {
      if(ship.isDestroyed){
        // If the ship is destroyed, all its squares are revealed
        Set(ship)
      } else {
        // Otherwise, each square hit become a single ship (to hide the whole position info)
        ship.squares.flatMap(square => if(!square._2) Some[Ship](DamagedOpponentShip(Set(square))) else None)
      }
    }), shotsReceived)
  }


  /**
    * Indicate if all the ships are destroyed, meaning the battle is lost for the
    * FleetGrid owner
    *
    * @return True if all the ships are destroyed, false otherwise
    */
  def isDestroyed: Boolean = ships.forall(_.isDestroyed)


  /**
    * Tests whether the ships are correctly placed on the grid
    *
    * @return True if the fleet abide the game ship placing rules
    */
  def isValid: Boolean = {
    // Every ship has to be in the grid:
    if(!ships.forall(_.squares.forall(s => s._1.x >= 0 && s._1.y >= 0 && s._1.x < dim.width && s._1.y < dim.height)))
      return false

    // The ships has to be aligned horizontally or vertically and in one part:
    if(!ships.forall(ship => {
      // Push all the squares of the ship in two sets, one for the x and one for the y
      val coordsSets = ship.squares.foldLeft((Set.empty[Int], Set.empty[Int]))((tuple, square) => {
        (tuple._1 + square._1.x, tuple._2 + square._1.y)
      })
      // If the ship is aligned, one set must be size 1 and the other one must be the size of the ship
      (coordsSets._1.size == 1 && coordsSets._2.size == ship.squares.size) ||
        (coordsSets._1.size == ship.squares.size && coordsSets._2.size == 1)
    }))
      return false

    // The ships should not overlap each other:
    val flatFleet = FleetHelper.flatten(this)
    if(!ships.forall(ship => ship.squares.forall(square => flatFleet(square._1.x)(square._1.y).contains(ship))))
      return false

    true
  }


  /**
    * Tests whether the fleet abides the initial ship composition and dimensions
    *
    * @param expectedShips The generic ships to be placed to compare with the fleet's ships
    * @param expectedDim The dimensions to compare with the fleet dimensions
    * @return True if the dimensions are equal and the ships and the expected composition match, false otherwise
    */
  def abidesComposition(expectedShips: Set[GenericShip], expectedDim: Dimension): Boolean = {

    @tailrec
    def abidesShipComposition(expectedShips: Set[GenericShip], ships: Set[Ship]): Boolean = {
      if(ships.isEmpty) expectedShips.isEmpty
      else expectedShips.find(_.size == ships.head.squares.size) match {
        case Some(genericShip) => abidesShipComposition(expectedShips - genericShip, ships.tail)
        case None => false
      }
    }

    expectedDim.equals(this.dim) && abidesShipComposition(expectedShips, this.ships)
  }


  /**
    * Add a ship to the fleet
    *
    * @param ship The ship to add
    * @return A fleet with the new ship
    */
  def +(ship: Ship): FleetGrid = FleetGrid(dim, ships + ship, shotsReceived)

}

object FleetGrid {

  def apply(dim: Dimension, ships: Set[Ship], shotsReceived: Set[Point]) = new FleetGrid(dim, ships, shotsReceived)

  /**
    * Randomly generates a fleet based on the given configuration
    *
    * @param dim The dimensions of the grid
    * @param expectedShips The fleet composition
    * @return A new fleet
    */
  def apply(dim: Dimension, expectedShips: Set[GenericShip]): FleetGrid = {

    @tailrec
    def addShip(fleet: FleetGrid, expectedShips: Set[GenericShip]): FleetGrid = {
      if(expectedShips.isEmpty) fleet
      else {
        val flatFleet = FleetHelper.flatten(fleet)

        // Get a matrix of taxicab distances to the nearest ship:
        val distancesToShips = FleetHelper.distanceToNearestObstacle[Option[Ship]](flatFleet, _.nonEmpty)
        // Then get the greatest distance:
        val maxValue = FleetHelper.maxValue(distancesToShips)._1
        /* Find a threshold to accept squares that do not maximize the greatest distance (this will create a placing
          more random)
          The random factor can be set between 0 and 1. 0 means a full random generation and 1 means a generation that
          maximizes the distance between ships. Greater than 1 may result in an infinite retry loop
          (the ship cannot be placed)
         */
        val randomFactor = .75
        val threshold = (maxValue - expectedShips.head.size/2) * randomFactor

        // Get all the coordinates that have a distance to the nearest ship greater than the threshold:
        val coordinatesList: List[Point] = distancesToShips.indices.flatMap(i => distancesToShips(i).indices.map(j =>
          if(distancesToShips(i)(j) >= threshold) Some(new Point(i, j)) else None
        )).flatten.toList

        // Choose a random point among the selected ones:
        val selectedPoint = coordinatesList(Rand.r.nextInt(coordinatesList.size))

        // Choose an orientation for the ship and center it on the selected point:
        val shipToAdd: Ship =
          if(Rand.r.nextInt(2) == 0){
            // Horizontal
            val firstPoint = new Point(selectedPoint.x - expectedShips.head.size/2, selectedPoint.y)
            Ship((0 until expectedShips.head.size).map(i => (new Point(firstPoint.x + i, firstPoint.y), true)).toSet)
          } else {
            // Vertical
            val firstPoint = new Point(selectedPoint.x, selectedPoint.y - expectedShips.head.size/2)
            Ship((0 until expectedShips.head.size).map(i => (new Point(firstPoint.x, firstPoint.y + i), true)).toSet)
          }

        // Sometimes the chosen location may result in an inconsistent fleet, e.g. the corner of the grid
        if((fleet + shipToAdd).isValid)
          addShip(fleet + shipToAdd, expectedShips.tail)
        else
          addShip(fleet, expectedShips)

      }
    }

    addShip(FleetGrid(dim, Set.empty, Set.empty), expectedShips)
  }
}

