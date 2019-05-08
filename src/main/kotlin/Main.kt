import arrow.data.fix
import arrow.data.value
import arrow.effects.fix
import domain.GitmojiRepository
import domain.services.GitmojiOperationMonad
import domain.services.GitmojiService.listAllGitmojis
import persistence.GitmojiFileDatasource
import persistence.GitmojiInMemoryDatasource
import persistence.GitmojiTwoTierRepository

fun main() {
  val inMemoryDatasource = GitmojiInMemoryDatasource()
  val fileDatasource = GitmojiFileDatasource()
  val gitmojiRepository = GitmojiTwoTierRepository(fileDatasource, inMemoryDatasource)

  printAllGitmojis(gitmojiRepository)
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
