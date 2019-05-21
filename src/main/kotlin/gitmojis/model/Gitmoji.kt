package gitmojis.model

typealias Emoji = String

data class Gitmoji(
  val emoji: Emoji,
  val entity: String,
  val code: String,
  val description: String,
  val name: String
)

data class Gitmojis(val gitmojis: List<Gitmoji>)
