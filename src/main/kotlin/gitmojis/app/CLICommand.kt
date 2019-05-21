package gitmojis.app

sealed class CLICommand {
  object ListGitmojis : CLICommand()
  object ShowHelp : CLICommand()
  data class SearchGitmojis(val searchWords: List<String>) : CLICommand()
  data class UnknownCommand(val command: String) : CLICommand()
  object CommitWizard : CLICommand()
  data class  HookWizard(val filePath: String) : CLICommand()

  companion object {
    fun parseCLICommand(args: Array<out String>): CLICommand =
      if (args.isEmpty()) UnknownCommand("empty")
      else when (val command = args.first()) {
        "-l", "--list" -> ListGitmojis
        "-c", "--commit" -> CommitWizard
        "-k", "--hook" -> HookWizard(args[1])
        "-s", "--search" -> SearchGitmojis(args.drop(1))
        "-h", "--help" -> ShowHelp
        else -> UnknownCommand(command)
      }
  }
}
