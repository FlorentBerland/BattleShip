package ui.playing

import java.awt.{BorderLayout, Color, Dimension, Point}

import akka.actor.ActorRef
import core.model.{FleetGrid, ShotGrid, ShotResult}
import javax.swing.{JLabel, JPanel}
import ui.DisplayFleetPanel

import scala.util.{Failure, Success, Try}

class GameComponent(val playerId: String, val nextActor: ActorRef, fleetGrid: FleetGrid, shotGrid: ShotGrid)
  extends JPanel {

  private val _playerFleetPanel = new DisplayFleetPanel(fleetGrid, new Dimension(300, 300), 10, 10, 10, 10)
  private val _opponentFleetPanel = new GameFleetPanel(shotGrid,
    new Dimension(300, 300), 10, 10, 10, 10,
    playerId, nextActor
  )
  private val _roundStateDisplay = new JLabel("Play !!!"){ this.setForeground(Color.white) }
  private val _roundStatePanel = new JPanel(){
    this.setBackground(new Color(100, 110, 200))
    this.add(_roundStateDisplay)
  }

  init()

  def notifiedToPlay(nextActor: ActorRef, shotGrid: ShotGrid, id: String): Unit = {
    _opponentFleetPanel.cbActor = nextActor
    _opponentFleetPanel.shotGrid = shotGrid
    _opponentFleetPanel.playerId = id
    _opponentFleetPanel.paint(_opponentFleetPanel.getGraphics)
  }

  def notifiedLastRoundResult(shotGrid: ShotGrid, result: Try[ShotResult.Value], id: String): Unit = {
    result match {
      case Success(data) => data match {
        case ShotResult.MISS =>
          _roundStateDisplay.setText("Miss")
          _roundStatePanel.setBackground(Color.gray)
        case ShotResult.HIT =>
          _roundStateDisplay.setText("Hit")
          _roundStatePanel.setBackground(Color.blue)
        case ShotResult.HIT_AND_SINK =>
          _roundStateDisplay.setText("Hit and sink !")
          _roundStatePanel.setBackground(Color.green)
      }
      case Failure(ex) => ex match {
        case _: IllegalArgumentException =>
          _roundStateDisplay.setText("You can't shot here")
          _roundStatePanel.setBackground(Color.orange)
        case _: IllegalStateException =>
          _roundStateDisplay.setText("This is not your turn !")
          _roundStatePanel.setBackground(Color.red)
      }
    }
    _opponentFleetPanel.shotGrid = shotGrid
    _opponentFleetPanel.playerId = id
    paint(this.getGraphics)
  }

  def notifiedHasBeenShot(fleetGrid: FleetGrid): Unit = {
    _playerFleetPanel.fleet = fleetGrid
    _playerFleetPanel.paint(_playerFleetPanel.getGraphics)
  }

  private def init(): Unit = {
    this.setLayout(new BorderLayout())

    // Header
    this.add(_roundStatePanel, BorderLayout.NORTH)

    // Fleet grid
    this.add(_playerFleetPanel, BorderLayout.WEST)

    // Play grid
    this.add(_opponentFleetPanel, BorderLayout.EAST)
  }

}
