import jline.TerminalFactory
import org.fusesource.jansi.Ansi.ansi
import org.fusesource.jansi.AnsiConsole
import persistence.GitmojiFileDatasource
import persistence.GitmojiInMemoryDatasource
import persistence.GitmojiTwoTierRepository
import presentation.CLICommand
import presentation.CLICommand.Companion.parseCLICommand
import presentation.commitWizard
import presentation.allGitmojis
import presentation.hookWizard
import presentation.showHelpText
import presentation.searchGitmojis

fun main(args: Array<String>) {
  val inMemoryDatasource = GitmojiInMemoryDatasource()
  val fileDatasource = GitmojiFileDatasource()
  val gitmojiRepository = GitmojiTwoTierRepository(fileDatasource, inMemoryDatasource)

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
