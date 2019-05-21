package gitmojis.app

import arrow.data.fix
import arrow.data.value
import arrow.effects.fix
import consoleRender
import gitmojis.repository.GitmojiRepository
import gitmojis.service.GitmojiOperationMonad
import gitmojis.service.GitmojiService

fun searchGitmojis(searchWords: List<String>, gitmojiRepository: GitmojiRepository) {
  val gitmojis = GitmojiOperationMonad.binding {
    with(GitmojiShowConsoleInstance) {
      GitmojiService.searchGitmojis(searchWords).bind().map { it.show() }
    }
  }.fix()

  gitmojis.run(gitmojiRepository)
    .value().fix()
    .unsafeRunSync()
    .fold(
      { it.all.forEach(::consoleRender) },
      { it.forEach(::consoleRender) }
    )
}
