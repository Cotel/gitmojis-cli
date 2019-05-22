package gitmojis.app

import arrow.core.Option
import arrow.core.extensions.option.applicative.applicative
import arrow.core.fix
import arrow.core.toOption
import arrow.data.EitherT
import arrow.data.EitherTPartialOf
import arrow.data.Kleisli
import arrow.data.Nel
import arrow.data.ReaderT
import arrow.data.extensions.eithert.fx.fx
import arrow.data.extensions.eithert.monad.monad
import arrow.data.extensions.kleisli.fx.fx
import arrow.data.extensions.kleisli.monad.monad
import arrow.data.extensions.kleisli.monadThrow.monadThrow
import arrow.effects.ForIO
import arrow.effects.IO
import arrow.effects.extensions.io.applicative.applicative
import arrow.effects.extensions.io.fx.fx
import arrow.effects.extensions.io.monad.monad
import arrow.effects.extensions.io.monadError.monadError
import arrow.effects.extensions.io.monadThrow.monadThrow
import de.codeshelf.consoleui.prompt.ConsolePrompt
import de.codeshelf.consoleui.prompt.InputResult
import gitmojis.app.interpreters.repository.GitmojiTwoTierRepository
import gitmojis.model.GitmojiDomainOp
import gitmojis.model.GitmojiDomainOpMonad
import gitmojis.model.GitmojiErrors
import gitmojis.repository.GitmojiRepository
import gitmojis.service.GitmojiService
import jline.console.completer.StringsCompleter

fun commonWizard() = GitmojiDomainOpMonad.fx {
  val prompt = ConsolePrompt()
  val promptBuilder = prompt.promptBuilder

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
    "${gitmoji.code} $message"
  }.fix()

  fullCommit
}
