# Welcome Messages — Fabric 1.21.1

Server-side Fabric mod for personal welcome messages.

A player name is checked against regex rules. The **first matching rule** wins, and **one random message** from that rule is sent only to that player.

## Requirements

- Minecraft 1.21.1
- Fabric Loader 0.19.5 or newer
- Fabric API 0.116.17+1.21.1 or compatible
- Java 21+

## Install

Put `welcome-messages.jar` into the server's `mods/` directory.

After the first server start, the config is created at:

```text
<server root>/config/welcome-messages/config.json
```

For a server directory called `mc-data`, the resulting path is:

```text
mc-data/config/welcome-messages/config.json
```

## Config

Example:

```json
{
  "rules": [
    {
      "pattern": "^(Алиска|Nijika)$",
      "messages": [
        "&dААА, {player} приехала! 🥁✨",
        "&5{player}, Konnichiwa! Добро пожаловать домой! 🎸"
      ]
    },
    {
      "pattern": "^Bocchi.*$",
      "messages": [
        "&5Боччи... {player}, ты всё-таки пришла 👉👈",
        "&d{player}, сервер немного нервничал без тебя..."
      ]
    },
    {
      "pattern": ".*",
      "messages": [
        "&7Добро пожаловать, &d{player}&7!",
        "&7С возвращением, &d{player}&7!"
      ]
    }
  ]
}
```

Rules are checked from top to bottom. Once a regex matches the player's **exact username**, one random message from that rule is sent and processing stops. Put the catch-all `.*` rule **last**, otherwise it will match everyone before the more specific rules.

`{player}` is replaced with the player's username.

Legacy Minecraft formatting codes are supported as `&0`–`&9`, `&a`–`&f`, `&k`–`&o`, and `&r`.

An invalid regex is logged and skipped instead of crashing the server.

If no rule matches, the mod sends nothing.

## CI/CD

Every push runs a build in GitHub Actions and updates a release tagged `latest`.

Permanent latest-download URL:

```text
https://github.com/OWNER/REPOSITORY/releases/latest/download/welcome-messages.jar
```

Replace `OWNER/REPOSITORY` with the GitHub repository name. The URL stays the same while the JAR is replaced by the newest successful build.

This same URL can be used as a direct mod download URL in server tooling such as itzg/minecraft-server.
