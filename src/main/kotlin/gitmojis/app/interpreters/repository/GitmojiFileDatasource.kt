package gitmojis.app.interpreters.repository

import base.ErrorOr
import arrow.core.Try
import arrow.core.left
import arrow.core.right
import arrow.data.NonEmptyList
import arrow.effects.IO
import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import gitmojis.model.Gitmoji
import gitmojis.model.Gitmojis
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.nio.channels.Channels

class GitmojiFileDatasource {

  companion object {
    private const val SOURCE_URL =
      "https://raw.githubusercontent.com/carloscuesta/gitmoji/master/src/data/gitmojis.json"
    private const val FILE_NAME = "./gitmojis.json"
  }

  fun downloadGitmojiJsonFile(): IO<ErrorOr<File>> = IO {
    try {
      val url = URL(SOURCE_URL)
      val readableByteChannel = Channels.newChannel(url.openStream())
      val fos = FileOutputStream(FILE_NAME)
      fos.channel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE)
    } catch (ex: Exception) {
      NonEmptyList("Downloading the file failed").left()
    }
  }.flatMap { openGitmojiJsonFile() }

  fun openGitmojiJsonFile(): IO<ErrorOr<File>> = IO {
    Try {
      File(FILE_NAME)
    }.fold(
      { NonEmptyList("File couldn't be opened").left() },
      { if (it.exists()) it.right() else NonEmptyList("File couldn't be opened").left() }
    )
  }

  fun parseGitmojiJsonFile(): IO<ErrorOr<Sequence<Gitmoji>>> = openGitmojiJsonFile()
    .flatMap { errorOrFile ->
      IO {
        errorOrFile.map { file ->
          val jsonReader = JsonReader(file.reader())
          val gitmojis: Gitmojis = Gson().fromJson(jsonReader, Gitmojis::class.java)
          gitmojis.gitmojis.asSequence()
        }
      }
    }
}
