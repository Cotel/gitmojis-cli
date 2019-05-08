package domain.services

import arrow.data.EitherT
import arrow.data.Kleisli

object GitmojiService {
  fun listAllGitmojis(): GitmojiOperation<Sequence<String>> = Kleisli.invoke { repository ->
    EitherT(
      repository.all()
        .map { gitmojisOrError ->
          gitmojisOrError.map { gitmojis ->
            gitmojis.map { (emoji, _, code, description, name) ->
              "%1s %-30s - %-30s | %s".format(emoji, name, code, description)
            }
          }
        }
    )
  }
}
