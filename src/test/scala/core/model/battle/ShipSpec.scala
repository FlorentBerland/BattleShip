package core.model.battle

import java.awt.Point

import core.model.Ship
import org.scalatest.{FlatSpec, Matchers}

class ShipSpec extends FlatSpec with Matchers {

  private val shipSize = 5
  private val ship = getShipExample(shipSize)

  "A ship" should "have its number of alive squares equal to the squares being worth true" in {
    ship.aliveSquares should be (shipSize)
  }

  it should "not be destroyed if the size is greater than 0" in {
    ship.isDestroyed should not be false
  }

  it should "not be affected if a shot misses" in {
    ship.aliveSquares should be (ship.shot(new Point(shipSize + 1, 1)).aliveSquares)
  }

  it should "lose an alive square if a shot succeeds" in {
    ship.aliveSquares should not be ship.shot(ship.squares.head._1).aliveSquares
  }

  it should "not lose the square if it has already been shot" in {
    ship.shot(ship.squares.head._1).aliveSquares should be(
      ship.shot(ship.squares.head._1).shot(ship.squares.head._1).aliveSquares)
  }

  it should "be destroyed if all the squares are shot" in {
    ship.squares.foldLeft(ship)((ship, square) => ship shot square._1).isDestroyed should be (true)
  }


  private def getShipExample(size: Int): Ship =
    Ship((1 to size).map(i => (new Point(i, 1), true)).toSet)

}
