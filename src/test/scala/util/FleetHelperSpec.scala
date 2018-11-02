package util

import java.awt.{Dimension, Point}

import core.model.{FleetGrid, GenericShip, Ship, ShotGrid}
import org.scalatest.FunSuite

class FleetHelperSpec extends FunSuite {

  /**
    * The fleet example should look like this:
    *
    *   |. . .|
    *   |X X .|
    *   |. . O|
    *
    *  . = water, X = ship 1, O = ship 2
    */

  val fleet = FleetGrid(
    new Dimension(3, 3),
    Set(Ship(Set((new Point(0, 1), false), (new Point(1, 1), false))),
      Ship(Set((new Point(2, 2), true)))),
    Set(new Point(0, 1), new Point(1, 2), new Point(1, 1)) // Ship 1 is sunk and a shot is missed
  )

  val fleetComposition: Set[GenericShip] = Set(new GenericShip("", 2), new GenericShip("", 1))

  test("A fleet flattened should be a matrix of Some(ship) in the ship squares and None for the empty squares") {
    assert(FleetHelper.flatten(fleet)(0)(0).isEmpty)
    assert(FleetHelper.flatten(fleet)(1)(0).isEmpty)
    assert(FleetHelper.flatten(fleet)(2)(0).isEmpty)
    assert(FleetHelper.flatten(fleet)(0)(1).nonEmpty)
    assert(FleetHelper.flatten(fleet)(1)(1).nonEmpty)
    assert(FleetHelper.flatten(fleet)(2)(1).isEmpty)
    assert(FleetHelper.flatten(fleet)(0)(2).isEmpty)
    assert(FleetHelper.flatten(fleet)(1)(2).isEmpty)
    assert(FleetHelper.flatten(fleet)(2)(2).nonEmpty)
  }

  test("A matrix based on a shot grid flattened should have as many shots as the fleet grid") {
    assert(FleetHelper.flattenShotMap(fleet.toOpponentGrid).flatten.count(p => p) == fleet.shotsReceived.size)
  }

  test("All destroyed ships must not be in the alive ships set") {
    assert(FleetHelper.notSunkShips(fleetComposition, fleet.toOpponentGrid).size == 1)
  }

  assert(FleetHelper.longestVerticalSequence[Option[Ship]](FleetHelper.flatten(fleet), _.isEmpty).flatten
    sameElements Array(1, 0, 1, 1, 0, 1, 2, 1, 0))

  assert(FleetHelper.longestHorizontalSequence[Boolean](FleetHelper.flattenShotMap(fleet.toOpponentGrid), !_).flatten
    sameElements Array(3, 0, 1, 2, 0, 0, 1, 1, 1))

  assert(FleetHelper.distanceToNearestObstacle[Option[Ship]](FleetHelper.flatten(fleet), _.nonEmpty).flatten
    sameElements Array(1, 0, 1, 1, 0, 1, 2, 1, 0))

  assert(FleetHelper.maxValue(Array(Array(1,3,2), Array(4,1,3))) == (4, 1, 0))

}
