# Underdeep — Forge 1.16.1

An experimental underground expansion for **Minecraft 1.16.1 / Forge 32.0.108**.

## Engine-height status

The intended Overworld range is **Y=-64 through Y=319**, with bedrock at the new bottom. That is **not implemented yet**. Vanilla 1.16.1 uses the original 0..255 height model, so a real expansion needs changes to chunk sections, build-height checks, heightmaps, lighting, saving/loading, network packets, and client rendering.

The engine work is tracked in [docs/ENGINE_HEIGHT_EXPANSION.md](docs/ENGINE_HEIGHT_EXPANSION.md). A normal Forge 32.0.108 + Underdeep mod JAR cannot provide negative-Y support. The completed version must be a matching custom Forge client and server.

## Current playable-height cave prototype

The current branch contains an interim cave pass that remains inside the original height range:

- Large, grid-aligned underground chambers.
- Winding noise-based tunnels designed to continue across chunk borders.
- Deepslate conversion through approximately Y=44, with a transition above it.
- Sparse deepslate iron ore.
- A per-chunk marker to avoid repeating the terrain pass after the chunk has been saved.

This is a **prototype**, not the finished engine expansion. The cave pass currently spans approximately Y=5..62. It does **not** move bedrock below Y=0, create negative coordinates, or raise the build limit. The temporary cave pattern uses a fixed deterministic salt rather than the world's actual seed.

**Use a new test world.** The chunk-load pass can process any unmarked Overworld chunk, including chunks in an older save. Back up worlds before testing.

## Exact target

- Minecraft: 1.16.1
- Forge: 32.0.108
- Java: 8
- MCP mappings: 20200820-1.16.1

## Build the interim mod

GitHub Actions builds the ordinary mod and uploads a JAR artifact.

Locally, use JDK 8 and Gradle 4.10.3:

```bash
gradle --no-daemon clean build
```

Output: `build/libs/underdeep-0.1.0.jar`

A successful compile does not confirm cave behavior, lighting quality, multiplayer stability, or the engine-height port. See [docs/BUILD_VALIDATION.md](docs/BUILD_VALIDATION.md) before treating it as a release.
