package presentation

import arrow.core.Option
import arrow.core.Try
import arrow.core.extensions.option.applicative.applicative
import arrow.core.fix
import arrow.core.toOption
import arrow.data.fix
import arrow.data.value
import arrow.effects.fix
import consoleRender
import de.codeshelf.consoleui.prompt.ConsolePrompt
import de.codeshelf.consoleui.prompt.InputResult
import domain.GitmojiRepository
import domain.services.GitmojiOperationMonad
import domain.services.GitmojiService
import jline.console.completer.StringsCompleter
import java.io.BufferedReader
import java.io.InputStreamReader

fun commitWizard(gitmojiRepository: GitmojiRepository) {
  val prompt = ConsolePrompt()
  val promptBuilder = prompt.promptBuilder

  val gitmojis = GitmojiOperationMonad.binding {
    val allGitmojis = GitmojiService.listAllGitmojis().bind()
    val allNames = allGitmojis.map { it.name }

    promptBuilder.createInputPrompt()
      .name("gitmoji")
      .message("Choose a gitmoji:")
      .addCompleter(StringsCompleter(allNames.toMutableList()))
      .addPrompt()

    promptBuilder.createInputPrompt()
      .name("message")
      .message("Enter the commit message:")
      .addPrompt()

    val result = prompt.prompt(promptBuilder.build()).mapValues { (_, v) -> (v as InputResult).input }
    val chosenGitmoji = result["gitmoji"].toOption()
      .map(String::trim)
      .flatMap { name -> GitmojiService.findGitmojiByName(name).bind() }
    val commitMessage = result["message"].toOption()

    val fullCommit = Option.applicative().map(chosenGitmoji, commitMessage) { (gitmoji, message) ->
      "${gitmoji.emoji} $message"
    }.fix()

    fullCommit
  }.fix()

  gitmojis.run(gitmojiRepository)
    .value().fix()
    .unsafeRunSync()
    .fold(
      { it.all.forEach(::consoleRender) },
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
