sealed class CLICommand {
  object ListGitmojis : CLICommand()
  object ShowHelp : CLICommand()
  object UnknownCommand : CLICommand()
}
