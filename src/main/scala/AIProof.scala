import akka.actor.{Actor, ActorRef, ActorSystem, Props}
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

  private val dimensions = DefaultGameConfig.dimensions
  private val ships = DefaultGameConfig.ships
  private val replays = DefaultGameConfig.replays

  private val _gameManager = context.actorOf(Props[GameReplaysManager], "GameReplaysManager")
  private val weakAI = context.actorOf(Props[WeakAIPlayer], "Level_Beginner")
  private val mediumAI = context.actorOf(Props[MediumAIPlayer], "Level_Medium")
  private val strongAI = context.actorOf(Props[StrongAIPlayer], "Level_Hard")

  private var matches = Set[(ActorRef, ActorRef)]((weakAI, mediumAI),(weakAI, strongAI), (mediumAI, strongAI))
  private var matchResults = Set.empty[(ActorRef, Int, ActorRef, Int)]


  override def preStart(): Unit = {
    super.preStart()


    _gameManager ! new UseGameConfig(self, matches.head._1, matches.head._2, dimensions, ships, replays)
    matches = matches.tail
  }


  private def onEndOfMatch(msg: EndOfMatch): Unit = {
    matchResults = matchResults + ((msg.player1, msg.score1, msg.player2, msg.score2))
    if(matches.nonEmpty){
      _gameManager ! new UseGameConfig(self, matches.head._1, matches.head._2, dimensions, ships, replays)
      matches = matches.tail
    } else {
      // Write the scores
      val file = new File("ai_proof.csv")
      val bw = new BufferedWriter(new FileWriter(file))
      bw.write("AI Name; score; AI Name2; score2\n")
      matchResults.foreach(row => {
        bw.write(row._1.path.name.replace("_"," ") + ";" + row._2 + ";"
          + row._3.path.name.replace("_", " ") + ";" + row._4 + "\n")
      })
      bw.close()

      context stop weakAI
      context stop mediumAI
      context stop strongAI
      context stop _gameManager
      context.system.terminate()
    }
  }
}