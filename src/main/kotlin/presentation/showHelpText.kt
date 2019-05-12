package presentation

import consoleRender
import consoleRenderBlankLine

fun showHelpText() {
  consoleRender("Gitmojis CLI: A tool for writing commits with fashion ✨")
  consoleRenderBlankLine()
  consoleRender(" Available commands:")
  consoleRender("   -l, --list              List all gitmojis")
  consoleRender("   -c, --commit            Make a new commit with the wizard")
  consoleRender("   -s, --search (names)    Search gitmojis by name")
  consoleRender("   -h, --help              Show this text")
}
