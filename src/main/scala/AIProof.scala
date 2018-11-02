import akka.actor._
import core.GameReplaysManager
import core.messages.{EndOfMatch, UseGameConfig}
import players._
import util.DefaultGameConfig
import java.io._

object AIProof extends App {

  val system = ActorSystem("BattleShipProof")
  val launcher = system.actorOf(Props[ProofLauncher])

}

class ProofLauncher extends Actor {

  override def receive: Receive = {
    case msg: EndOfMatch => onEndOfMatch(msg)

    case _ =>
  }

  // Game config to use
  private val dimensions = DefaultGameConfig.dimensions
  private val ships = DefaultGameConfig.ships
  private val replays = DefaultGameConfig.replays

  // Actors
  private val gameManager = context.actorOf(Props[GameReplaysManager], "GameReplaysManager")
  private val weakAI = context.actorOf(Props[WeakAIPlayer], "Level_Beginner")
  private val mediumAI = context.actorOf(Props[MediumAIPlayer], "Level_Medium")
  private val strongAI = context.actorOf(Props[StrongAIPlayer], "Level_Hard")

  // Matches
  private val matches = Set[(ActorRef, ActorRef)]((weakAI, mediumAI),(weakAI, strongAI),(mediumAI, strongAI))
  private var matchResults = Set.empty[(ActorRef, Int, ActorRef, Int)]

  // Output
  private val file = new File("ai_proof.csv")
  private val bw = new BufferedWriter(new FileWriter(file))


  override def preStart(): Unit = {
    super.preStart()
    bw.write("AI Name; score; AI Name2; score2\n")
    matches.foreach(m => gameManager ! new UseGameConfig(self, m._1, m._2, dimensions, ships, replays))
  }


  private def onEndOfMatch(msg: EndOfMatch): Unit = {
    matchResults = matchResults + ((msg.player1, msg.score1, msg.player2, msg.score2))
    bw.write(msg.player1.path.name.replace("_", " ") + ";" + msg.score1 + ";"
      + msg.player2.path.name.replace("_", " ") + ";" + msg.score2 + "\n")

    if(matchResults.size == matches.size){
      bw.close()

      context stop weakAI
      context stop mediumAI
      context stop strongAI
      context stop gameManager
      context.system.terminate
    }
  }
}