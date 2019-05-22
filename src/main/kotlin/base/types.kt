package base

import arrow.core.Either
import arrow.data.NonEmptyList
import arrow.effects.IO
import gitmojis.model.GitmojiErrors

typealias ErrorOr<A> = Either<NonEmptyList<GitmojiErrors>, A>
typealias BIO<A> = IO<ErrorOr<A>>
