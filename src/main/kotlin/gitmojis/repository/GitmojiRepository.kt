package gitmojis.repository

import arrow.core.Option
import arrow.effects.IO
import base.BIO
import base.ErrorOr
import gitmojis.model.Gitmoji

interface GitmojiRepository {
  fun all(): BIO<Sequence<Gitmoji>>
  fun searchByName(searchWords: List<String>): BIO<Sequence<Gitmoji>>
  fun findOneByName(name: String): BIO<Option<Gitmoji>>
}
