package core.messages

/**
  * Sent by a player who quit the game to warn the receiver of the message
  *
  * @param playerId The id of the sender of the message
  */
class QuitGame(val playerId: String)
