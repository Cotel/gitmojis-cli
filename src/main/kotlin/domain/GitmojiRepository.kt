package domain

import ErrorOr
import arrow.effects.IO

interface GitmojiRepository {
  fun all(): IO<ErrorOr<Sequence<Gitmoji>>>
  fun searchByName(searchWords: List<String>): IO<ErrorOr<Sequence<Gitmoji>>>
}
