package gitmojis.app

import arrow.core.Try
import arrow.data.run
import arrow.data.value
import arrow.effects.fix
import consoleRender
import consoleRenderList
import gitmojis.model.GitmojiErrors
import gitmojis.model.show
import gitmojis.repository.GitmojiRepository
import java.io.BufferedReader
import java.io.InputStreamReader

fun commitWizard(gitmojiRepository: GitmojiRepository) {
  commonWizard().run(gitmojiRepository)
    .value().fix()
    .unsafeRunSync()
    .fold(
      { consoleRenderList(it.all, GitmojiErrors.show()) },
      {
        it.fold(
          { consoleRender("An error ocured while cooking your commit message") },
          { commitMsg ->
            Try {
              val rt = Runtime.getRuntime()
              val process = rt.exec("git commit -m \"$commitMsg\"")

              val input = BufferedReader(InputStreamReader(process.inputStream))
              var line: String?

              line = input.readLine()
              while (line != null) {
                consoleRender(line)
                line = input.readLine()
              }

              process.waitFor()
            }
          }
        )
      }
    )
}
