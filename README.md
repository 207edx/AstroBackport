# Underdeep — Forge 1.16.1

An experimental underground-expansion project for **Minecraft 1.16.1 / Forge 32.0.108**.

## Important compatibility note

Minecraft 1.16.1 has a fixed 0–255 world-height model. Moving the Overworld floor to Y=-64 is not a normal block/world-generation change: it requires coordinated changes to chunk section storage, heightmaps, lighting, world bounds, chunk serialization and the client/server chunk protocol.

This first repository milestone is a **playable-height prototype**, not a claim that negative Y is already implemented. It adds the deepslate-style block layer and a deterministic underground cave pass within the existing 1.16.1 height range. The true -64 engine port is tracked separately and must not be treated as complete until it passes client + dedicated-server testing and save/reload tests.

Use a **new test world** for this prototype. Do not add it to a valuable existing save.

## Target

- Minecraft: 1.16.1
- Forge: 32.0.108
- Java: 8
- Mappings: 20200820-1.16.1

## Build

GitHub Actions builds the mod on every push and uploads the reobfuscated JAR as an artifact.

Locally, use JDK 8 and Gradle 4.10.3:

```bash
gradle --no-daemon clean build
```

Output: `build/libs/underdeep-0.1.0.jar`

## Current milestone

- Deepslate and cobbled-deepslate blocks.
- A deepslate iron ore block.
- Basic block models, loot tables and crafting/smelting recipes.
- An opt-in-by-world-generation chunk pass that converts natural stone in the lower part of newly explored Overworld terrain and opens small underground chambers.
- The true negative-Y engine expansion remains unfinished and is explicitly not advertised as working.

## Installation

Install Forge 1.16.1-32.0.108, then put the built JAR in the `mods` folder. Both client and server need the mod. Create a fresh test world.
