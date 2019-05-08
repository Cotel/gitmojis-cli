sealed class CLICommand {
  object ListGitmojis : CLICommand()
  object ShowHelp : CLICommand()
  data class SearchGitmojis(val searchWords: List<String>) : CLICommand()
  object UnknownCommand : CLICommand()
}
