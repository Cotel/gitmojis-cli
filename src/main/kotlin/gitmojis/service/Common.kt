package gitmojis.service

import arrow.data.EitherT
import arrow.data.EitherTPartialOf
import arrow.data.Kleisli
import arrow.data.Nel
import arrow.data.ReaderT
import arrow.data.extensions.eithert.monad.monad
import arrow.data.extensions.kleisli.monad.monad
import arrow.effects.ForIO
import arrow.effects.IO
import arrow.effects.extensions.io.monad.monad
import gitmojis.repository.GitmojiRepository

val GitmojiOperationMonad = ReaderT.monad<EitherTPartialOf<ForIO, Nel<String>>, GitmojiRepository>(
  EitherT.monad(IO.monad())
)

typealias GitmojiOperation<A> = Kleisli<EitherTPartialOf<ForIO, Nel<String>>, GitmojiRepository, A>
