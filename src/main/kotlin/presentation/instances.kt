package presentation

import arrow.typeclasses.Show
import domain.Gitmoji

val GitmojiShowConsoleInstance = object : Show<Gitmoji> {
  override fun Gitmoji.show(): String =
    "%1s - %-30s | %s".format(emoji, code, description)
}
