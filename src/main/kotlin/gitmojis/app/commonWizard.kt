package gitmojis.app

import arrow.core.extensions.option.applicative.applicative
import arrow.core.fix
import arrow.core.toOption
import de.codeshelf.consoleui.prompt.InputResult
import gitmojis.service.GitmojiOperationMonad
import gitmojis.service.GitmojiService

fun commonWizard() = GitmojiOperationMonad.binding {
  val prompt = de.codeshelf.consoleui.prompt.ConsolePrompt()
  val promptBuilder = prompt.promptBuilder

  val allGitmojis = GitmojiService.listAllGitmojis().bind()
  val allNames = allGitmojis.map { it.name }

  promptBuilder.createInputPrompt()
    .name("gitmoji")
    .message("Choose a gitmoji:")
    .addCompleter(jline.console.completer.StringsCompleter(allNames.toMutableList()))
    .addPrompt()

  promptBuilder.createInputPrompt()
    .name("message")
    .message("Enter the commit message:")
    .addPrompt()

  val result = prompt.prompt(promptBuilder.build()).mapValues { (_, v) -> (v as InputResult).input }
  val chosenGitmoji = result["gitmoji"].toOption()
    .map(kotlin.String::trim)
    .flatMap { name -> GitmojiService.findGitmojiByName(name).bind() }
  val commitMessage = result["message"].toOption()

  val fullCommit = arrow.core.Option.applicative().map(chosenGitmoji, commitMessage) { (gitmoji, message) ->
    "${gitmoji.code} $message"
  }.fix()

  fullCommit
}
