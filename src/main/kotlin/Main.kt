import arrow.typeclasses.Show
import gitmojis.app.CLICommand
import gitmojis.app.CLICommand.Companion.parseCLICommand
import gitmojis.app.allGitmojis
import gitmojis.app.commitWizard
import gitmojis.app.hookWizard
import gitmojis.app.interpreters.repository.GitmojiTwoTierRepository
import gitmojis.app.searchGitmojis
import gitmojis.app.showHelpText
import jline.TerminalFactory
import org.fusesource.jansi.Ansi.ansi
import org.fusesource.jansi.AnsiConsole

fun main(args: Array<String>) {
  AnsiConsole.systemInstall()

  when (val cliCommand = parseCLICommand(args)) {
    CLICommand.ListGitmojis -> allGitmojis(GitmojiTwoTierRepository)
    CLICommand.ShowHelp -> showHelpText()
    CLICommand.CommitWizard -> commitWizard(GitmojiTwoTierRepository)
    is CLICommand.HookWizard -> hookWizard(cliCommand.filePath, GitmojiTwoTierRepository)
    is CLICommand.SearchGitmojis -> searchGitmojis(cliCommand.searchWords, GitmojiTwoTierRepository)
    is CLICommand.UnknownCommand -> {
      consoleRender("Unrecognized command: ${cliCommand.command}")
      showHelpText()
    }
  }

  AnsiConsole.systemUninstall()
  TerminalFactory.get().restore()
}

fun consoleRenderBlankLine() = println()
fun consoleRender(str: String) = println(ansi().render(str))
fun <A> consoleRenderList(list: List<A>, S: Show<A>) = list
  .map { S.run { it.show() } }
  .forEach(::consoleRender)
