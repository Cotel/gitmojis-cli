package gitmojis.repository

import arrow.core.Option
import arrow.effects.IO
import base.ErrorOr
import gitmojis.model.Gitmoji

interface GitmojiRepository {
  fun all(): IO<ErrorOr<Sequence<Gitmoji>>>
  fun searchByName(searchWords: List<String>): IO<ErrorOr<Sequence<Gitmoji>>>
  fun findByName(name: String): IO<ErrorOr<Option<Gitmoji>>>
}
