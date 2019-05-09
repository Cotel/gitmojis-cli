package domain

import ErrorOr
import arrow.Kind
import arrow.core.Option
import arrow.data.EitherTPartialOf
import arrow.data.Nel
import arrow.effects.ForIO
import arrow.effects.IO

interface GitmojiRepository {
  fun all(): IO<ErrorOr<Sequence<Gitmoji>>>
  fun searchByName(searchWords: List<String>): IO<ErrorOr<Sequence<Gitmoji>>>
  fun findByName(name: String): IO<ErrorOr<Option<Gitmoji>>>
}
