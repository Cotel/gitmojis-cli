import arrow.core.Either
import arrow.data.NonEmptyList

typealias ErrorOr<A> = Either<NonEmptyList<String>, A>
