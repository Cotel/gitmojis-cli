package presentation

import arrow.data.fix
import arrow.data.value
import arrow.effects.fix
import consoleRender
import domain.GitmojiRepository
import domain.services.GitmojiOperationMonad
import domain.services.GitmojiService

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
