import jline.TerminalFactory
import org.fusesource.jansi.Ansi.ansi
import org.fusesource.jansi.AnsiConsole
import gitmojis.app.interpreters.repository.GitmojiFileDatasource
import gitmojis.app.interpreters.repository.GitmojiInMemoryDatasource
import gitmojis.app.interpreters.repository.GitmojiTwoTierRepository
import gitmojis.app.CLICommand
import gitmojis.app.CLICommand.Companion.parseCLICommand
import gitmojis.app.commitWizard
import gitmojis.app.allGitmojis
import gitmojis.app.hookWizard
import gitmojis.app.showHelpText
import gitmojis.app.searchGitmojis

fun main(args: Array<String>) {
  val inMemoryDatasource = GitmojiInMemoryDatasource()
  val fileDatasource = GitmojiFileDatasource()
  val gitmojiRepository =
    GitmojiTwoTierRepository(fileDatasource, inMemoryDatasource)

  AnsiConsole.systemInstall()

  when (val cliCommand = parseCLICommand(args)) {
    CLICommand.ListGitmojis -> allGitmojis(gitmojiRepository)
    CLICommand.ShowHelp -> showHelpText()
    CLICommand.CommitWizard -> commitWizard(gitmojiRepository)
    is CLICommand.HookWizard -> hookWizard(cliCommand.filePath, gitmojiRepository)
    is CLICommand.SearchGitmojis -> searchGitmojis(cliCommand.searchWords, gitmojiRepository)
    is CLICommand.UnknownCommand -> {
      consoleRender("Unrecognized command: ${cliCommand.command}")
      showHelpText()
    }
  }

  AnsiConsole.systemUninstall()
  TerminalFactory.get().restore()
}

fun consoleRender(str: String) = println(ansi().render(str))
fun consoleRenderBlankLine() = println()
