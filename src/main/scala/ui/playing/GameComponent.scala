package ui.playing

import java.awt.{BorderLayout, Color, Dimension, Point}

import akka.actor.ActorRef
import core.model.{FleetGrid, ShotGrid, ShotResult}
import javax.swing.{JLabel, JPanel}
import ui.DisplayFleetPanel

import scala.util.{Failure, Success, Try}

class GameComponent(val player: ActorRef, val nextActor: ActorRef, fleetGrid: FleetGrid, shotGrid: ShotGrid)
  extends JPanel {

  private val _playerFleetPanel = new DisplayFleetPanel(fleetGrid, new Dimension(300, 300), 10, 10, 10, 10)
  private val _opponentFleetPanel = new GameFleetPanel(shotGrid,
    new Dimension(300, 300), 10, 10, 10, 10,
    player, nextActor
  )

  init()

  def notifiedToPlay(nextActor: ActorRef, shotGrid: ShotGrid): Unit = {
    _opponentFleetPanel.cbActor = nextActor
    _opponentFleetPanel.shotGrid = shotGrid
    _opponentFleetPanel.paint(_opponentFleetPanel.getGraphics)
  }

  def notifiedLastRoundResult(shotGrid: ShotGrid, result: Try[(Point, ShotResult.Value)]): Unit = {
    result match {
      case Success(data) =>
      case Failure(ex) =>
    }
    _opponentFleetPanel.shotGrid = shotGrid
  }

  def notifiedHasBeenShot(fleetGrid: FleetGrid): Unit = {
    _playerFleetPanel.fleet = fleetGrid
    _playerFleetPanel.paint(_playerFleetPanel.getGraphics)
  }

  private def init(): Unit = {
    this.setLayout(new BorderLayout())

    // Header
    this.add(new JPanel(){
      this.setBackground(new Color(100, 110, 200))
      this.add(new JLabel("Play !!!"){ this.setForeground(Color.white) })
    }, BorderLayout.NORTH)

    // Fleet grid
    this.add(_playerFleetPanel, BorderLayout.WEST)

    // Play grid
    this.add(_opponentFleetPanel, BorderLayout.EAST)
  }

}
