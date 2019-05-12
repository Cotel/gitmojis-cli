package presentation

import arrow.data.run
import arrow.data.value
import arrow.effects.fix
import consoleRender
import domain.GitmojiRepository
import java.io.File

fun hookWizard(filePath: String, gitmojiRepository: GitmojiRepository) {
  commonWizard().run(gitmojiRepository)
    .value().fix()
    .unsafeRunSync()
    .fold(
      {
        it.all.forEach(::consoleRender)
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
