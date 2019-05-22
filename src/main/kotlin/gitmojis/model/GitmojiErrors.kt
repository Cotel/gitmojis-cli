package gitmojis.model

import arrow.typeclasses.Show

sealed class GitmojiErrors { companion object }
data class PersistenceError(val message: String): GitmojiErrors()
object SearchInputIsEmptyError : GitmojiErrors()

fun GitmojiErrors.Companion.show(): Show<GitmojiErrors> = object : Show<GitmojiErrors> {
  override fun GitmojiErrors.show(): String = when (this) {
    is PersistenceError -> this.message
    is SearchInputIsEmptyError -> "Search input cannot be empty"
  }
}
