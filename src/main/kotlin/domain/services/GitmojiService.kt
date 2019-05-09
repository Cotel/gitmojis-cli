package domain.services

import arrow.core.Option
import arrow.core.left
import arrow.data.EitherT
import arrow.data.Kleisli
import arrow.data.Nel
import arrow.effects.IO
import domain.Gitmoji

object GitmojiService {
  fun listAllGitmojis(): GitmojiOperation<Sequence<Gitmoji>> = Kleisli { repository ->
    EitherT(repository.all())
  }

  fun searchGitmojis(searchWords: List<String>): GitmojiOperation<Sequence<Gitmoji>> = Kleisli { repository ->
    if (searchWords.isEmpty()) EitherT(IO { Nel("Search words are empty").left() })
    else EitherT(repository.searchByName(searchWords))
  }

  fun findGitmojiByName(name: String): GitmojiOperation<Option<Gitmoji>> = Kleisli { repository ->
    EitherT(repository.findByName(name))
  }
}
