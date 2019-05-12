package presentation

import arrow.data.fix
import arrow.data.value
import arrow.effects.fix
import consoleRender
import domain.GitmojiRepository
import domain.services.GitmojiOperationMonad

fun allGitmojis(gitmojiRepository: GitmojiRepository) {
  val gitmojis = GitmojiOperationMonad.binding {
    with(GitmojiShowConsoleInstance) {
      domain.services.GitmojiService.listAllGitmojis().bind().map { it.show() }
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
