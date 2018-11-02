package core

import java.util.UUID

import akka.actor.{Actor, ActorRef, Props}
import core.messages.{UseGameConfig, _}
import core.model.FleetGrid


class GameReplaysManager extends Actor {

  override def receive: Receive = {
    case msg: UseGameConfig => onCreateGameConfig(msg)
    case msg: FleetCreated => onFleetCreated(msg)
    case msg: GameFinished => onGameFinished(msg)
    case msg: Replay => onReplay(msg)
    case msg: QuitGame => onQuitGame(msg)

    case _ =>
  }

  override def postStop(): Unit = {
    context stop _gameEngine
    super.postStop()
  }


  private val _gameEngine: ActorRef = context.actorOf(Props[BattleEngine], "BattleEngine")

  ///// Manager state

  // Values: player1, player2, last starting player
  private var games = Map.empty[UseGameConfig,(Player, Player, ActorRef)]
  private var idPlayers = Map.empty[String, ActorRef]



  private def onCreateGameConfig(msg: UseGameConfig): Unit = {
    val idPlayer1 = UUID.randomUUID().toString
    val idPlayer2 = UUID.randomUUID().toString
    idPlayers = idPlayers + (idPlayer1 -> msg.player1, idPlayer2 -> msg.player2)
    games = games + (msg -> (Player().playerSet(msg.player1).uidSet(idPlayer1),
      Player().playerSet(msg.player2).uidSet(idPlayer2), msg.player2))

    msg.player1 ! new CreateFleet(self, msg.dimensions, msg.ships, idPlayer1)
    msg.player2 ! new CreateFleet(self, msg.dimensions, msg.ships, idPlayer2)
  }


  private def onFleetCreated(msg: FleetCreated): Unit = {
    val game = findGameByPlayerUid(msg.playerId)
    if(game.isEmpty) return // The actor was not a subscribed player

    // This should shorten the code after...
    val key = game.get._1
    val values = game.get._2

    if(msg.fleet.abidesComposition(key.ships, key.dimensions) && msg.fleet.isValid){
      // Set the fleet to the player
      if(values._1.uid == msg.playerId) {
        games = games + (key -> (values._1.fleetChanged(msg.fleet), values._2, values._3))
      }
      else if(values._2.uid == msg.playerId)
        games = games + (key -> (values._1, values._2.fleetChanged(msg.fleet), values._3))
    }

    val nv = games(key) // The New set of Values, to shorten the code

    if(nv._1.isReady && nv._2.isReady){
      if(nv._2 == nv._3){
        _gameEngine ! new StartGame(self, nv._1.getPlayerGrid, nv._2.getPlayerGrid)
        games = games + (key -> (nv._1, nv._2, nv._1.player))
      } else {
        _gameEngine ! new StartGame(self, nv._2.getPlayerGrid, nv._1.getPlayerGrid)
        games = games + (key -> (nv._1, nv._2, nv._2.player))
      }
    }
  }


  private def onGameFinished(msg: GameFinished): Unit = {
    if(sender != _gameEngine) return
    val game = findGameByPlayerUid(msg.winnerId)
    if(game.isEmpty) return // Should not happen

    val key = game.get._1
    val values = game.get._2

    // Reset the fleets and increment the scores
    if(values._1.uid == msg.winnerId)
      games = games + (key -> (values._1.fleetReset.scoreIncremented, values._2.fleetReset, values._3))
    else
      games = games + (key -> (values._1.fleetReset, values._2.fleetReset.scoreIncremented, values._3))

    val nv = games(key) // The New set of Values, to shorten the code

    // Print the scores:
    println(Console.BLUE + "\n---------- Round " +
      (nv._1.score + nv._2.score) +
      " ----------" + Console.RESET)
    println("Player 1 (" + values._1.player.path.name + "):\t" +
      nv._1.score + (if(nv._1.uid == msg.winnerId) "\t(winner)" else ""))
    println("Player 2 (" + values._2.player.path.name + "):\t" +
      nv._2.score + (if(nv._2.uid == msg.winnerId) "\t(winner)" else ""))

    // Max number of rounds reached
    if(nv._1.score + nv._2.score == key.replays)
      finishMatch((key, nv))
  }


  private def onReplay(msg: Replay): Unit = {
    val game = findGameByPlayerUid(msg.playerId)
    if(game.isEmpty) return

    val key = game.get._1
    val values = game.get._2

    if(values._1.uid == msg.playerId)
      games = games + (key -> (values._1.replayChanged(msg.replay), values._2, values._3))
    else if(values._2.uid == msg.playerId)
      games = games + (key -> (values._1, values._2.replayChanged(msg.replay), values._3))

    val nv = games(key) // The New set of Values, to shorten the code

    if(nv._1.wantsToReplay && nv._2.wantsToReplay){
      nv._1.player ! new CreateFleet(self, key.dimensions, key.ships, nv._1.uid)
      nv._2.player ! new CreateFleet(self, key.dimensions, key.ships, nv._2.uid)
      games = games + (key -> (nv._1.replayReset, nv._2.replayReset, nv._3))
    } else if(nv._1.wantsToQuit || nv._2.wantsToQuit){
      finishMatch((key, nv))
    }
  }

  private def onQuitGame(msg: QuitGame): Unit = findGameByPlayerUid(msg.playerId).foreach(g => finishMatch(g))


  /**
    * Retrieves the game that is played by the given player actor
    *
    * @param uid The uid of the player to search
    * @return The game key-values pair found if it exists
   */
  private def findGameByPlayerUid(uid: String): Option[(UseGameConfig, (Player, Player, ActorRef))] =
    games.find(g => g._2._1.uid == uid || g._2._2.uid == uid)


  /**
    * Terminates a match, and remove its game
    *
    * @param game The game game to terminate
    */
  private def finishMatch(game: (UseGameConfig,(Player, Player, ActorRef))): Unit = {
    games = games - game._1
    if(game._1.sender == null) {
      def stillPlaying(player: ActorRef): Boolean = idPlayers.exists(_._2 == player)
      if(!stillPlaying(game._2._1.player)) context stop game._2._1.player
      if(!stillPlaying(game._2._2.player)) context stop game._2._2.player
    } else {
      game._1.sender ! new EndOfMatch(self, game._2._1.player, game._2._1.score, game._2._2.player, game._2._2.score)
    }
  }


  private class Player(val player: ActorRef, val fleet: FleetGrid, val score: Int, val _wantsToReplay: Option[Boolean], val uid: String){

    def getPlayerGrid: (ActorRef, FleetGrid, String) = (player, fleet, uid)
    def playerSet(actor: ActorRef): Player = Player(actor, fleet, score, _wantsToReplay, uid)
    def uidSet(id: String): Player = Player(player, fleet, score, _wantsToReplay, id)
    def fleetChanged(fleetGrid: FleetGrid): Player = Player(player, fleetGrid, score, _wantsToReplay, uid)
    def scoreIncremented: Player = Player(player, fleet, score + 1, _wantsToReplay, uid)
    def replayChanged(replay: Boolean): Player = Player(player, fleet, score, Some(replay), uid)
    def replayReset: Player = Player(player, fleet, score, None, uid)
    def fleetReset: Player = Player(player, null, score, _wantsToReplay, uid)
    def isReady: Boolean = player != null && fleet != null
    def ==(actor: ActorRef): Boolean = player == actor
    def wantsToReplay: Boolean = _wantsToReplay.contains(true)
    def wantsToQuit: Boolean = _wantsToReplay.contains(false)

  }
  private object Player{
    def apply(player: ActorRef, fleet: FleetGrid, score: Int, _wantsToReplay: Option[Boolean], uid: String): Player = new Player(player, fleet, score, _wantsToReplay, uid)
    def apply(): Player = Player(null, null, 0, None, "")
  }

}
