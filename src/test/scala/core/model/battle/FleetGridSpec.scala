package core.model.battle

import java.awt.{Dimension, Point}

import org.scalatest.{FlatSpec, Matchers}

import scala.util.Success

class FleetGridSpec extends FlatSpec with Matchers {

  val fleet: FleetGrid = getFleetExample

  "A fleet" should "not be destroyed if it contains alive ships" in {
    fleet.isDestroyed should be (false)
  }

  it should "be a failure if the shot is out of bounds or redone on a previously shot square" in {
    fleet.shot(new Point(0, 3)).isFailure should be (true)
    fleet.shot(new Point(7, 11)).isFailure should be (true)
    fleet.shot(new Point(1, 1)).map(_._1.shot(new Point(1, 1)).get._1).isFailure should be (true)
  }

  it should "send 'miss' if a shot misses" in {
    fleet.shot(new Point(10, 10)).map(_._2) should be (Success(ShotResult.MISS))
  }

  it should "send 'hit' if a shot hit a ship" in {
    fleet.shot(new Point(1, 1)).map(_._2) should be (Success(ShotResult.HIT))
  }

  it should "send 'hit and sink' if a shot sink a ship" in {
    fleet.ships.head.squares.init.foldLeft(fleet)((fleet, square) => {
      fleet.shot(square._1).map(_._1).getOrElse(fleet)
    }).shot(fleet.ships.head.squares.last._1).map(_._2) should be (Success(ShotResult.HIT_AND_SINK))
  }

  it should "be destroyed if all the ship squares get hit" in {
    fleet.ships.foldLeft(fleet)((fleet, ship) => {
      ship.squares.foldLeft(fleet)((fleet, square) => {
        fleet.shot(square._1).map(_._1).getOrElse(fleet)
      })
    }).isDestroyed should be (true)
  }

  private def getFleetExample: FleetGrid = {
    val ships = Set[Ship](
      Ship((1 to 3).map(i => (new Point(i, 1), true)).toSet),
      Ship((1 to 5).map(i => (new Point(6, i), true)).toSet),
      Ship((1 to 3).map(i => (new Point(i, 3), true)).toSet)
    )
    FleetGrid(new Dimension(10, 10), ships, Set.empty)
  }

}
