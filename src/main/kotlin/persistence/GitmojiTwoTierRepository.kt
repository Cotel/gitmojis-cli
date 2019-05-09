package persistence

import ErrorOr
import arrow.core.Option
import arrow.core.Some
import arrow.core.left
import arrow.core.right
import arrow.data.Nel
import arrow.data.extensions.list.traverse.sequence
import arrow.data.extensions.sequence.foldable.isEmpty
import arrow.data.fix
import arrow.effects.IO
import arrow.effects.extensions.io.applicative.applicative
import arrow.effects.extensions.io.fx.fx
import arrow.effects.extensions.io.monad.effectM
import arrow.effects.fix
import domain.Gitmoji
import domain.GitmojiRepository

class GitmojiTwoTierRepository(
  private val fileDatasource: GitmojiFileDatasource,
  private val inMemoryDatasource: GitmojiInMemoryDatasource
) : GitmojiRepository {
  override fun all(): IO<ErrorOr<Sequence<Gitmoji>>> =
    inMemoryDatasource.all()
      .flatMap { memoryGitmojisOrError ->
        memoryGitmojisOrError.fold(
          { memoryErrors ->
            fileDatasource
              .openGitmojiJsonFile()
              .flatMap { fileOrError ->
                fileOrError.fold(
                  { fileDatasource.downloadGitmojiJsonFile() },
                  { IO { it.right() } }
                )
              }
              .flatMap { fileDatasource.parseGitmojiJsonFile() }
              .effectM { IO { it.map { inMemoryDatasource.loadGitmojis(it) } } }
              .flatMap { fileGitmojisOrError ->
                fileGitmojisOrError.fold(
                  { fileErrors -> IO { (memoryErrors + fileErrors).left() } },
                  { IO { it.right() } }
                )
              }
          },
          { IO { it.right() } }
        )
      }

  override fun searchByName(searchWords: List<String>): IO<ErrorOr<Sequence<Gitmoji>>> = fx {
    if (inMemoryDatasource.isMemoryEmpty()) populateData().bind()

    val searchResult = searchWords
      .map { inMemoryDatasource.searchByName(it) }
      .sequence(IO.applicative()).map { it.fix() }.fix()
      .map { searchResults -> searchResults.asSequence().filter { it.isDefined() }.map { (it as Some).t } }
      .bind()

    if (searchResult.isEmpty()) Nel("Search results are empty").left()
    else searchResult.right()
  }

  override fun findByName(name: String): IO<ErrorOr<Option<Gitmoji>>> = fx {
    if (inMemoryDatasource.isMemoryEmpty()) populateData().bind()

    val gitmoji = inMemoryDatasource.searchByName(name).bind()

    gitmoji.right()
  }

  private fun populateData(): IO<Unit> = fx {
    val fileOrError = fileDatasource.openGitmojiJsonFile().bind()
    fileOrError.fold(
      { fileDatasource.downloadGitmojiJsonFile() },
      { IO { it.right() } }
    ).bind()
    val parseOrError = fileDatasource.parseGitmojiJsonFile().bind()
    parseOrError.fold(
      { Nel("Error parsing json file").left() },
      { inMemoryDatasource.loadGitmojis(it).bind() }
    )
    Unit
  }
}
