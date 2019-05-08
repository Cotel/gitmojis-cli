import arrow.data.fix
import arrow.data.value
import arrow.effects.fix
import domain.GitmojiRepository
import domain.services.GitmojiOperationMonad
import domain.services.GitmojiService.listAllGitmojis
import persistence.GitmojiFileDatasource
import persistence.GitmojiInMemoryDatasource
import persistence.GitmojiTwoTierRepository

fun main(vararg args: String) {
  val inMemoryDatasource = GitmojiInMemoryDatasource()
  val fileDatasource = GitmojiFileDatasource()
  val gitmojiRepository = GitmojiTwoTierRepository(fileDatasource, inMemoryDatasource)

  when (parseCLICommand(args)) {
    CLICommand.ListGitmojis -> printAllGitmojis(gitmojiRepository)
    CLICommand.ShowHelp -> printHelpText()
    CLICommand.UnknownCommand -> {
      println("Unrecognized command")
      printHelpText()
    }
  }
}

fun parseCLICommand(args: Array<out String>): CLICommand {
  val textCommand = args.first()

  return when (textCommand) {
    "-l", "--list" -> CLICommand.ListGitmojis
    "-h", "--help" -> CLICommand.ShowHelp
    else -> CLICommand.UnknownCommand
  }
}

fun printAllGitmojis(gitmojiRepository: GitmojiRepository) {
  val gitmojis = GitmojiOperationMonad.binding {
    listAllGitmojis().bind()
  }.fix()

  gitmojis.run(gitmojiRepository)
    .value().fix()
    .unsafeRunSync()
    .fold(
      { it.all.forEach(::println) },
      { it.forEach(::println) }
    )
}

fun printHelpText() {
  println("Gitmojis CLI: A tool for writing commits with fashion ✨")
  println()
  println(" Available commands:")
  println("   -l, --list    List all gitmojis")
  println("   -h, --help    Show this text")
}
