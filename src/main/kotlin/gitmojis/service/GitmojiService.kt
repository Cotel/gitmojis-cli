package gitmojis.service

import arrow.core.Option
import arrow.core.left
import arrow.data.EitherT
import arrow.data.Kleisli
import arrow.data.Nel
import arrow.effects.ForIO
import arrow.effects.IO
import base.ErrorOr
import gitmojis.model.Gitmoji
import gitmojis.model.GitmojiDomainOp
import gitmojis.model.SearchInputIsEmptyError
import gitmojis.repository.GitmojiRepository

interface GitmojiService {
  fun listAllGitmojis(): GitmojiDomainOp<Sequence<Gitmoji>> = Kleisli { repository -> EitherT(repository.all()) }

  fun searchGitmojis(searchWords: List<String>): GitmojiDomainOp<Sequence<Gitmoji>> = Kleisli { repository ->
    if (searchWords.isEmpty()) EitherT(IO { Nel(SearchInputIsEmptyError).left() })
    else EitherT(repository.searchByName(searchWords))
  }

  fun findGitmojiByName(name: String): GitmojiDomainOp<Option<Gitmoji>> = Kleisli { repository ->
    EitherT(repository.findOneByName(name))
  }

  companion object : GitmojiService
}
