import arrow.data.fix
import arrow.data.value
import arrow.effects.fix
import arrow.typeclasses.Show
import domain.Gitmoji
import domain.GitmojiRepository
import domain.services.GitmojiOperationMonad
import domain.services.GitmojiService.listAllGitmojis
import domain.services.GitmojiService.searchGitmojis
import persistence.GitmojiFileDatasource
import persistence.GitmojiInMemoryDatasource
import persistence.GitmojiTwoTierRepository

fun main(vararg args: String) {
  val inMemoryDatasource = GitmojiInMemoryDatasource()
  val fileDatasource = GitmojiFileDatasource()
  val gitmojiRepository = GitmojiTwoTierRepository(fileDatasource, inMemoryDatasource)

  when (val cliCommand = parseCLICommand(args)) {
    CLICommand.ListGitmojis -> printAllGitmojis(gitmojiRepository)
    CLICommand.ShowHelp -> printHelpText()
    is CLICommand.SearchGitmojis -> printSearchGitmojis(cliCommand.searchWords, gitmojiRepository)
    CLICommand.UnknownCommand -> {
      println("Unrecognized command")
      printHelpText()
    }
  }
}

fun parseCLICommand(args: Array<out String>): CLICommand =
  when (args.first()) {
    "-l", "--list" -> CLICommand.ListGitmojis
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
      { it.all.forEach(::println) },
      { it.forEach(::println) }
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
      { it.all.forEach(::println) },
      { it.forEach(::println) }
    )
}

fun printHelpText() {
  println("Gitmojis CLI: A tool for writing commits with fashion ✨")
  println()
  println(" Available commands:")
  println("   -l, --list              List all gitmojis")
  println("   -s, --search (names)    Search gitmojis by name")
  println("   -h, --help              Show this text")
}

val GitmojiShowConsoleInstance = object : Show<Gitmoji> {
  override fun Gitmoji.show(): String =
    "%1s - %-30s | %s".format(emoji, code, description)
}
