package gitmojis.app

import arrow.data.run
import arrow.data.value
import arrow.effects.fix
import consoleRender
import consoleRenderList
import gitmojis.model.GitmojiErrors
import gitmojis.model.show
import gitmojis.repository.GitmojiRepository
import java.io.File

fun hookWizard(filePath: String, gitmojiRepository: GitmojiRepository) {
  commonWizard().run(gitmojiRepository)
    .value().fix()
    .unsafeRunSync()
    .fold(
      {
        consoleRenderList(it.all, GitmojiErrors.show())
        System.exit(1)
      },
      {
        it.fold(
          {
            consoleRender("An error ocured while cooking your commit message")
            System.exit(1)
          },
          {
            val file = File(filePath)
            file.writeText(it)
          }
        )
      }
    )
}
