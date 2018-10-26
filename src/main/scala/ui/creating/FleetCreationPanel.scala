package ui.creating

import java.awt.event.{MouseEvent, MouseListener, MouseMotionListener}
import java.awt.{Color, Dimension, Graphics, Point}

import core.model.{FleetGrid, GenericShip, Ship}
import ui.DisplayFleetPanel


/**
  * Manage the ships positioning during the fleet creation
  *
  * @param initFleet The fleet to display
  * @param dimensions The size of the grid
  * @param mTop The top margin
  * @param mBottom The bottom margin
  * @param mLeft The left margin
  * @param mRight The right margin
  */
class FleetCreationPanel(initFleet: FleetGrid,
                         dimensions: Dimension,
                         mTop: Int,
                         mBottom: Int,
                         mLeft: Int,
                         mRight: Int
                       ) extends DisplayFleetPanel(initFleet, dimensions, mTop, mBottom, mLeft, mRight)
                          with MouseListener with MouseMotionListener
{

  private var _shipDragged: Option[GenericShip] = None
  private var _isHorizontal: Option[Boolean] = None // The direction of the ship dragged
  private var _cbPlaceShip: Option[Option[Ship] => Unit] = None // What to do on a left click (place the ship)

  init()

  private def init(): Unit = {
    this.addMouseMotionListener(this)
    this.addMouseListener(this)
  }

  /**
    * Ask the panel to drag the given ship (it will be displayed on the fleet editor)
    *
    * @param genShip The ship to place
    * @param isHorizontal The default orientation for the ship
    * @param cb The controller to invoke when the position is confirmed (by a left click)
    */
  def dragShip(genShip: GenericShip, isHorizontal: Boolean = true, cb: Option[Ship] => Unit): Unit = {
    _shipDragged = Some(genShip)
    _isHorizontal = Some(isHorizontal)
    _cbPlaceShip = Some(cb)
  }


  override def paint(g: Graphics): Unit = {
    super.paint(g)

    squareCursor(this.getMousePosition).foreach(fillSquare(g, _, Color.gray))

    computeShip(_shipDragged, _isHorizontal, squareCursor(this.getMousePosition)).
      foreach(_.squares.foreach(sq => fillSquare(getGraphics, new Point(sq._1.x, sq._1.y), Color.blue)))

    g.setColor(Color.white)
    g.fillRect(dim.width + marginLeft + 1, 0, 200, dim.height + marginTop + 200)
    g.fillRect(0, dim.height + marginTop + 1, dim.width + marginLeft + 200, 200)
  }

  /**
    * Create a ship depending on the _shipDragged properties and the _isHorizontal property
    *
    * @return A ship ready to be added to the grid
    */
  private def computeShip(genShip: Option[GenericShip], isHorizontal: Option[Boolean], cursorPos: Option[Point]): Option[Ship] = {
    genShip.flatMap(ship => cursorPos.map(pos => {
      if(isHorizontal.getOrElse(true))
        Ship((pos.x until pos.x + ship.size).map(i => (new Point(i, pos.y), true)).toSet)
      else
        Ship((pos.y until pos.y + ship.size).map(i => (new Point(pos.x, i), true)).toSet)
    }))
  }


  // Mouse events part

  override def mouseMoved(e: MouseEvent): Unit = {
    paint(this.getGraphics)
  }

  override def mouseClicked(e: MouseEvent): Unit = {
    e.getButton match {
      case 1 => // Left click
        _cbPlaceShip.foreach(_(computeShip(_shipDragged, _isHorizontal, squareCursor(getMousePosition))))
        _shipDragged = None
        _isHorizontal = None
        _cbPlaceShip = None
        paint(this.getGraphics)
      case 3 => // Right click
        _isHorizontal = _isHorizontal.map(!_)
        paint(this.getGraphics)
      case _ =>
    }
  }


  // Useless events
  override def mouseDragged(e: MouseEvent): Unit = {}
  override def mouseEntered(e: MouseEvent): Unit = {}
  override def mouseExited(e: MouseEvent): Unit = {}
  override def mousePressed(e: MouseEvent): Unit = {}
  override def mouseReleased(e: MouseEvent): Unit = {}
}
