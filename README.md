# Gitmoji CLI
A Kotlin CLI tool for commiting with fashion 💅

Based on [gitmoji](https://github.com/carloscuesta/gitmoji) and [gitmoji-cli](https://github.com/carloscuesta/gitmoji-cli) by [carloscuesta](https://github.com/carloscuesta)

## Usage

```
$> java -jar gitmoji-cli.jar -h
Gitmojis CLI: A tool for writing commits with fashion

 Available commands:
   -l, --list              List all gitmojis
   -c, --commit            Make a new commit with the wizard
   -k, --hook              Pre commit hook
   -s, --search (names)    Search gitmojis by name
   -h, --help              Show this text
```

## Git hook
If you want to launch this tool with every commit you can configure a hook

1. Go to your project root folder
2. Paste the `gitmoji-cli.jar`
3. `cd ./git/hooks`
4. `touch prepare-commit-msg`
5. `chmod +x prepare-commit-msg`
6. Paste the following inside `prepare-commit-msg`

```
#!/bin/sh
exec < /dev/tty
java -jar gitmoji-cli.jar --hook $1
```

## Build
For generating a jar file you must follow this steps:

1. Clone this project
2. `./gradlew shadowJar`
3. Navigate to `/build/libs/`
4. A file called `gitmoji-cli-*-all.jar` should be there
5. Copy that file into your project root and rename it to `gitmoji-cli.jar`
