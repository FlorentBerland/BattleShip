package ui.ending

import java.awt.{BorderLayout, Dimension, FlowLayout, Font}

import akka.actor.ActorRef
import core.GameEnd
import core.messages.Replay
import core.model.FleetGrid
import javax.swing.{JLabel, JPanel}
import ui.{DisplayFleetPanel, SwingUI}

class GameOverComponent(val player: ActorRef, val nextActor: ActorRef,
                        val end: GameEnd.End, val opponentFleet: FleetGrid
                       ) extends JPanel {

  init()

  private def init(): Unit = {
    this.setLayout(new BorderLayout())
    this.add(new JPanel(){
      val label = new JLabel()
      end match {
        case GameEnd.VICTORY =>
          label.setText("Victory !")
        case GameEnd.DEFEAT =>
          label.setText("Defeat :(")
      }
      label.setFont(new Font("Helvetica", Font.BOLD, 36))
      this.add(label)
    }, BorderLayout.NORTH)
    this.add(new DisplayFleetPanel(
      opponentFleet, new Dimension(300, 300), 10, 10, 10 ,10
    ), BorderLayout.CENTER)
    this.add(new JPanel(){
      this.setLayout(new FlowLayout())
      this.add(SwingUI.button("Replay", () => nextActor ! new Replay(player, true)))
      this.add(SwingUI.button("Quit game", () => nextActor ! new Replay(player, false)))
    }, BorderLayout.SOUTH)
  }

}
