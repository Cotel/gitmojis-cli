package domain

import arrow.core.Option
import arrow.effects.IO

interface GitmojiRepository {
  fun all(): IO<ErrorOr<Sequence<Gitmoji>>>
  fun searchByName(searchWords: List<String>): IO<ErrorOr<Sequence<Gitmoji>>>
  fun findByName(name: String): IO<ErrorOr<Option<Gitmoji>>>
}
