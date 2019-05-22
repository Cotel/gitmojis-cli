package gitmojis.app.interpreters.repository

import arrow.core.Option
import arrow.core.Some
import arrow.core.left
import arrow.core.right
import arrow.data.Nel
import arrow.data.extensions.list.traverse.sequence
import arrow.data.fix
import arrow.effects.IO
import arrow.effects.extensions.io.applicative.applicative
import arrow.effects.extensions.io.fx.fx
import arrow.effects.extensions.io.monad.effectM
import arrow.effects.fix
import arrow.effects.handleErrorWith
import base.BIO
import base.ErrorOr
import gitmojis.model.Gitmoji
import gitmojis.repository.GitmojiRepository

interface GitmojiTwoTierRepository : GitmojiRepository {

  val fileDatasource: GitmojiFileDatasource
  val inMemoryDatasource: GitmojiInMemoryDatasource

  override fun all(): BIO<Sequence<Gitmoji>> =
    inMemoryDatasource.all()
      .handleErrorWith {
        fileDatasource.openGitmojiJsonFile()
          .handleErrorWith { fileDatasource.downloadGitmojiJsonFile() }
          .flatMap { fileDatasource.parseGitmojiJsonFile() }
          .effectM { IO { it.map { inMemoryDatasource.loadGitmojis(it) } } }
      }

  override fun searchByName(searchWords: List<String>): BIO<Sequence<Gitmoji>> = fx {
    if (inMemoryDatasource.isMemoryEmpty()) populateData().bind()

    val searchResult = searchWords
      .map { inMemoryDatasource.searchByName(it) }
      .sequence(IO.applicative()).map { it.fix() }.fix()
      .map { searchResults -> searchResults.asSequence().filter { it.isDefined() }.map { (it as Some).t } }
      .bind()

    searchResult.right()
  }

  override fun findOneByName(name: String): IO<ErrorOr<Option<Gitmoji>>> = fx {
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

  companion object : GitmojiTwoTierRepository {
    override val fileDatasource: GitmojiFileDatasource =
      GitmojiFileDatasource()

    override val inMemoryDatasource: GitmojiInMemoryDatasource =
      GitmojiInMemoryDatasource()
  }
}
