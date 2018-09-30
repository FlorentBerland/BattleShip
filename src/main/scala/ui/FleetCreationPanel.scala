package ui

import java.awt.{Color, Dimension, Graphics, Point}
import java.awt.event.{MouseEvent, MouseListener, MouseMotionListener}

import core.model.{FleetGrid, GenericShip, Ship}
import javax.swing.JPanel

/**
  * The controller for the grid display on the screen.
  *
  * @param fleet The fleet to display. It is variable for needs of performance (instead of recreating
  *              a controller on each update)
  */
class FleetCreationPanel(var fleet: FleetGrid) extends JPanel with MouseMotionListener with MouseListener {

  private val _containerSize = new Dimension(420, 420)
  private val _size = new Dimension(400, 400)
  private val _squareSize = new Dimension(_size.width/fleet.dim.width, _size.height/fleet.dim.height)

  // Editing properties
  private var _shipDragged: Option[GenericShip] = None
  private var _isHorizontal: Option[Boolean] = None // The direction of the ship dragged
  private var _cbPlaceShip: Option[Option[Ship] => Unit] = None // WHat to do on a left click (place the ship)

  init()

  private def init(): Unit = {
    this.addMouseMotionListener(this)
    this.addMouseListener(this)
    this.setPreferredSize(_containerSize)
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
    g.setColor(Color.gray)
    (0 until fleet.dim.width).map(i => {
      (0 until fleet.dim.height ).map(j => {
        g.drawRect(i*_squareSize.width, j*_squareSize.height, _squareSize.width, _squareSize.height)
      })
    })

    fleet.ships.map(_.squares.map(square => fillSquare(g, new Point(square._1.x - 1, square._1.y - 1), Color.darkGray)))

    squareCursor(this.getMousePosition).map(fillSquare(g, _, Color.gray))

    computeShip(_shipDragged, _isHorizontal, squareCursor(this.getMousePosition)).
      map(_.squares.map(sq => fillSquare(getGraphics, new Point(sq._1.x - 1, sq._1.y - 1), Color.blue)))

    g.setColor(Color.darkGray)
    g.drawString("Right click to flip a ship", 10, 415)
  }

  protected def fillSquare(g: Graphics, square: Point, color: Color): Unit = {
    g.setColor(color)
    g.fillRect(square.x*_squareSize.width, square.y*_squareSize.height, _squareSize.width, _squareSize.height)
  }

  /**
    * Return the square overlapped by the cursor
    *
    * @param mouseLocation The location relative to the panel
    * @return The square overlapped
    */
  private def squareCursor(mouseLocation: Point): Option[Point] = {
    if(mouseLocation == null)
      None
    else {
      val square = new Point(mouseLocation.x / _squareSize.width, mouseLocation.y / _squareSize.height)
      if (square.x < 0 || square.y < 0 || square.x >= fleet.dim.width || square.y >= fleet.dim.height)
        None
      else
        Some(square)
    }
  }

  /**
    * Create a ship depending on the _shipDragged properties and the _isHorizontal property
    *
    * @return A ship ready to be added to the grid
    */
  private def computeShip(genShip: Option[GenericShip], isHorizontal: Option[Boolean], cursorPos: Option[Point]): Option[Ship] = {
    genShip.flatMap(ship => cursorPos.map(pos => {
      if(isHorizontal.getOrElse(true))
        Ship((pos.x until pos.x + ship.size).map(i => (new Point(i + 1, pos.y + 1), true)).toSet)
      else
        Ship((pos.y until pos.y + ship.size).map(i => (new Point(pos.x + 1, i + 1), true)).toSet)
    }))
  }

  override def mouseMoved(e: MouseEvent): Unit = {
    paint(this.getGraphics)
  }

  override def mouseClicked(e: MouseEvent): Unit = {
    e.getButton match {
      case 1 => // Left click
        _cbPlaceShip.map(_(computeShip(_shipDragged, _isHorizontal, squareCursor(getMousePosition))))
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
