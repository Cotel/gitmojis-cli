package presentation

sealed class CLICommand {
  object ListGitmojis : CLICommand()
  object ShowHelp : CLICommand()
  data class SearchGitmojis(val searchWords: List<String>) : CLICommand()
  object UnknownCommand : CLICommand()
  object CommitWizard : CLICommand()

  companion object {
    fun parseCLICommand(args: Array<out String>): CLICommand =
      if (args.isEmpty()) UnknownCommand
      else when (args.first()) {
        "-l", "--list" -> ListGitmojis
        "-c", "--commit" -> CommitWizard
        "-s", "--search" -> SearchGitmojis(args.drop(1))
        "-h", "--help" -> ShowHelp
        else -> UnknownCommand
      }
  }
}
