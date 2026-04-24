# FTB Chunks: Sable Aerospace

FTB Chunks: Sable Aerospace is a NeoForge 1.21.1 addon that closes a long-standing protection gap between Sable and FTB Chunks.

By default, Sable structures can still be interacted with even inside claimed land. This addon makes FTB Chunks claims apply to Sable assemblies as well, so other players cannot use, break, or blow up air structures inside protected territory.

## Features

- Blocks interaction with Sable structures inside claimed chunks unless the player is allowed there.
- Blocks breaking Sable structures inside claimed chunks unless the player is allowed there.
- Protects Sable structures from explosion block damage when the target FTB Chunks claim disallows terrain explosions.
- Adds predictive airspace warnings when a player is approaching another team's claimed area by air.
- Shows localized enter and exit messages when a player crosses into or out of foreign claimed airspace.
- Includes a configurable high-altitude free zone above a chosen Y level. By default this starts above Y `320`.
- In the default free-zone ruleset, normal use is allowed, but block breaking, block placement, and TNT ignition are denied.
- Suppresses airspace warnings inside the free zone so the zone behaves consistently.
- Includes localized messages plus separate common and client config files.
- Built for NeoForge 1.21.1 with Java 21.

## Requirements

- Minecraft 1.21.1
- NeoForge 21.1.227 or compatible 1.21.1 NeoForge build
- Sable 1.1.3+
- FTB Chunks 2101.1.14+

## Installation

1. Install NeoForge for Minecraft 1.21.1.
2. Put Sable, FTB Chunks, and this mod into your `mods` folder.
3. Start the game once so the config files are generated.

## Common Config

The common config is generated as `config/ftbchunksaerospace-common.toml`.

Available options:

- `enabled`: enables or disables the high-altitude free zone ruleset.
- `freeZoneAboveY`: Y level above which foreign claimed chunks switch to free-zone rules. Default is `320`.
- `allowBlockUse`: allows normal block and item use in the free zone. Default is `true`.
- `allowBlockBreak`: allows breaking blocks in the free zone. Default is `false`.
- `allowBlockPlace`: allows placing blocks and fluids in the free zone. Default is `false`.
- `denyTntIgnition`: blocks TNT ignition in the free zone. Default is `true`.
- `showDenyMessage`: shows a deny message when a free-zone rule blocks an action. Default is `true`.

## Client Config

The client config is generated as `config/ftbchunksaerospace-client.toml`.

Available options:

- `enableApproachWarning`: enables predictive actionbar warning before entering foreign airspace.
- `approachWarningSeconds`: warning horizon in seconds. Default is `60.0`.
- `warningCheckIntervalTicks`: how often the client checks future movement. Default is `10`.
- `enableEnteredMessage`: shows a message when entering foreign closed airspace.
- `enableExitedMessage`: shows a message when leaving foreign closed airspace.

## Development

Build the mod locally:

```bash
./gradlew build
```

On Windows:

```powershell
.\gradlew.bat build
```

The built jar is written to `build/libs/`.

For the VS Code dev setup used in this repository, the helper scripts also prepare the generated client run arguments:

```bash
sh ./.vscode/prepareClientRun.sh
```

On Windows:

```powershell
.\.vscode\prepareClientRun.bat
```

## GitHub Actions And Releases

This repository includes two GitHub workflows:

- `Build`: runs on pushes, pull requests, tags, and manual dispatch. It builds the mod and uploads the jar in the Actions run as an artifact.
- `Release`: runs when you publish a GitHub Release or when you start it manually from the Actions tab. It builds the jar and uploads it to the GitHub Release as a release asset.

Recommended release flow:

1. Push your changes.
2. Create and push a tag such as `v1.0.0`, or publish a release from the GitHub Releases page.
3. The workflow will build the mod and attach the jar to the release.

## Downloads

- Modrinth: [ftbchunksaerospace](https://modrinth.com/mod/ftbchunksaerospace)
- CurseForge: [FTB Chunks: Sable Aerospace](https://legacy.curseforge.com/minecraft/mc-mods/ftb-chunks-sable-aerospace)
- GitHub: [maks-gaming/ftbchunksaerospace](https://github.com/maks-gaming/ftbchunksaerospace)
