package gitmojis.app

import arrow.typeclasses.Show
import gitmojis.model.Gitmoji

fun Gitmoji.Companion.show() = object : Show<Gitmoji> {
  override fun Gitmoji.show(): String =
    "%1s - %-30s | %s".format(emoji, code, description)
}
