package ui.initializing

import java.awt.BorderLayout

import akka.actor.ActorRef
import core.messages.{QuitGame, UseGameConfig}
import javax.swing.{BoxLayout, JLabel, JPanel}
import ui.SwingUI

class ChooseOpponentComponent(val player: ActorRef, val nextActor: ActorRef) extends JPanel {

  init()

  private def init(): Unit = {
    this.setLayout(new BorderLayout())
    this.add(new JLabel("Choose your opponent"), BorderLayout.NORTH)
    this.add(new JPanel(){
      this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS))
      this.add(SwingUI.button("Human player",
        () => nextActor ! new UseGameConfig(player, "HumanPlayer")))
      this.add(SwingUI.button("Weak AI",
        () => nextActor ! new UseGameConfig(player, "WeakAIPlayer")))
      this.add(SwingUI.button("Medium AI",
        () => nextActor ! new UseGameConfig(player, "MediumAIPlayer")))
      this.add(SwingUI.button("Strong AI",
        () => nextActor ! new UseGameConfig(player, "StrongAIPlayer")))
    }, BorderLayout.CENTER)
    this.add(SwingUI.button("QuitGame", () => nextActor ! new QuitGame(player)), BorderLayout.SOUTH)
  }

}
