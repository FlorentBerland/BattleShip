package core.messages


/**
  * Sent by the GameEngine to inform of the winner of a game
  *
  * @param winnerId The id of the player who won the game
  */
class GameFinished(val winnerId: String)
