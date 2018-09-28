package util

import java.awt.Dimension

import core.model.GenericShip

object DefaultGameConfig {
  def dimensions = new Dimension(10, 10)
  def ships = Set(
    (new GenericShip("Carrier", 5), 1),
    (new GenericShip("Battleship", 4), 2),
    (new GenericShip("Cruiser", 3), 3),
    (new GenericShip("Submarine", 3), 4),
    (new GenericShip("Destroyer", 2), 5)
  )
}
