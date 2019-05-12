import arrow.core.Option
import arrow.core.Try
import arrow.core.extensions.option.applicative.applicative
import arrow.core.fix
import arrow.core.identity
import arrow.core.toOption
import arrow.data.fix
import arrow.data.value
import arrow.effects.fix
import arrow.typeclasses.Show
import de.codeshelf.consoleui.prompt.ConsolePrompt
import de.codeshelf.consoleui.prompt.InputResult
import domain.Gitmoji
import domain.GitmojiRepository
import domain.services.GitmojiOperationMonad
import domain.services.GitmojiService.findGitmojiByName
import domain.services.GitmojiService.listAllGitmojis
import domain.services.GitmojiService.searchGitmojis
import jline.TerminalFactory
import jline.console.completer.StringsCompleter
import org.fusesource.jansi.Ansi.ansi
import org.fusesource.jansi.AnsiConsole
import persistence.GitmojiFileDatasource
import persistence.GitmojiInMemoryDatasource
import persistence.GitmojiTwoTierRepository
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import kotlin.concurrent.thread

fun main(args: Array<String>) {
  val inMemoryDatasource = GitmojiInMemoryDatasource()
  val fileDatasource = GitmojiFileDatasource()
  val gitmojiRepository = GitmojiTwoTierRepository(fileDatasource, inMemoryDatasource)

  AnsiConsole.systemInstall()

  when (val cliCommand = parseCLICommand(args)) {
    CLICommand.ListGitmojis -> printAllGitmojis(gitmojiRepository)
    CLICommand.ShowHelp -> printHelpText()
    CLICommand.CommitWizard -> commitWizard(gitmojiRepository)
    is CLICommand.SearchGitmojis -> printSearchGitmojis(cliCommand.searchWords, gitmojiRepository)
    CLICommand.UnknownCommand -> {
      consoleRender("Unrecognized command")
      printHelpText()
    }
  }

  AnsiConsole.systemUninstall()
  TerminalFactory.get().restore()
}

fun parseCLICommand(args: Array<out String>): CLICommand =
  if (args.isEmpty()) CLICommand.UnknownCommand
  else when (args.first()) {
    "-l", "--list" -> CLICommand.ListGitmojis
    "-c", "--commit" -> CLICommand.CommitWizard
    "-s", "--search" -> CLICommand.SearchGitmojis(args.drop(1))
    "-h", "--help" -> CLICommand.ShowHelp
    else -> CLICommand.UnknownCommand
  }

fun printAllGitmojis(gitmojiRepository: GitmojiRepository) {
  val gitmojis = GitmojiOperationMonad.binding {
    with(GitmojiShowConsoleInstance) {
      listAllGitmojis().bind().map { it.show() }
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

fun printSearchGitmojis(searchWords: List<String>, gitmojiRepository: GitmojiRepository) {
  val gitmojis = GitmojiOperationMonad.binding {
    with(GitmojiShowConsoleInstance) {
      searchGitmojis(searchWords).bind().map { it.show() }
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

fun commitWizard(gitmojiRepository: GitmojiRepository) {
  val prompt = ConsolePrompt()
  val promptBuilder = prompt.promptBuilder

  val gitmojis = GitmojiOperationMonad.binding {
    val allGitmojis = listAllGitmojis().bind()
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
      .flatMap { name -> findGitmojiByName(name).bind() }
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

fun printHelpText() {
  consoleRender("Gitmojis CLI: A tool for writing commits with fashion ✨")
  consoleRenderBlankLine()
  consoleRender(" Available commands:")
  consoleRender("   -l, --list              List all gitmojis")
  consoleRender("   -c, --commit            Make a new commit with the wizard")
  consoleRender("   -s, --search (names)    Search gitmojis by name")
  consoleRender("   -h, --help              Show this text")
}

fun consoleRender(str: String) = println(ansi().render(str))
fun consoleRenderBlankLine() = println()

val GitmojiShowConsoleInstance = object : Show<Gitmoji> {
  override fun Gitmoji.show(): String =
    "%1s - %-30s | %s".format(emoji, code, description)
}
