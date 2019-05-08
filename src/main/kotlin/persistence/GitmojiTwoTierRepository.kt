package persistence

import ErrorOr
import arrow.core.left
import arrow.core.right
import arrow.effects.IO
import arrow.effects.extensions.io.monad.effectM
import arrow.effects.handleErrorWith
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

}
