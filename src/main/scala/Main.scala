import java.awt.Point

import core.model.{FleetGrid, Ship}
import util.{DefaultGameConfig, FleetHelper}

object Main extends App {

  //var fleet = FleetGrid(DefaultGameConfig.dimensions, Set(Ship(Set((new Point(5,5), true), (new Point(5,6), true)))), Set.empty)

  var fleet = FleetGrid(DefaultGameConfig.dimensions, DefaultGameConfig.ships)
  printArray(FleetHelper.distanceToNearestObtacle[Option[Ship]](FleetHelper.flatten(fleet), _.nonEmpty))

  def printArray[T](array: Array[Array[T]]): Unit = {
    array.transpose.toList.foreach(row => { row.foreach(a => print(a + "\t")); println() })
  }

}
