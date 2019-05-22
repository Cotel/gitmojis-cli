package gitmojis.app

import arrow.data.value
import arrow.effects.fix
import consoleRender
import consoleRenderList
import gitmojis.model.Gitmoji
import gitmojis.model.GitmojiErrors
import gitmojis.model.show
import gitmojis.repository.GitmojiRepository
import gitmojis.service.GitmojiService

fun allGitmojis(gitmojiRepository: GitmojiRepository) =
  GitmojiService.listAllGitmojis()
    .run(gitmojiRepository)
    .value().fix()
    .unsafeRunSync()
    .fold(
      { consoleRenderList(it.all, GitmojiErrors.show()) },
      { consoleRenderList(it.toList(), Gitmoji.show()) }
    )
