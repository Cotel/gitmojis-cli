package persistence

import ErrorOr
import arrow.core.Option
import arrow.core.left
import arrow.core.right
import arrow.core.toOption
import arrow.data.NonEmptyList
import arrow.effects.IO
import domain.Gitmoji

class GitmojiInMemoryDatasource {
  private var memoryMap: Map<String, Gitmoji> = emptyMap()

  fun isMemoryEmpty(): Boolean = memoryMap.isEmpty()

  fun loadGitmojis(gitmojis: Sequence<Gitmoji>): IO<Unit> = IO {
    val gitmojisMap = gitmojis
      .asSequence()
      .map { gitmoji -> gitmoji.name to gitmoji }
      .toMap()

    memoryMap = gitmojisMap
  }

  fun all(): IO<ErrorOr<Sequence<Gitmoji>>> = IO {
    if (isMemoryEmpty()) NonEmptyList("Memory is empty").left()
    else memoryMap.values.asSequence().right()
  }

  fun searchByName(name: String): IO<Option<Gitmoji>> = IO {
    memoryMap[name].toOption()
  }
}
