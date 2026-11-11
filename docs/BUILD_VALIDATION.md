# Build and runtime validation

CI must compile and reobfuscate against exactly Forge 1.16.1-32.0.108 with Java 8 and Gradle 4.10.3.

A green compile is only a compile check. Before calling this a stable release, test:
- Launching a clean client with only Forge and Underdeep.
- Launching a dedicated server with the same mod.
- Creating a fresh Overworld and exploring newly generated chunks.
- Saving, quitting, and reloading to confirm the one-time chunk marker persists.
- Checking recipes, drops, cave geometry, lighting, and chunk borders.

The current prototype intentionally stays in the vanilla 0..255 height range. Negative-Y support is a separate engine-level milestone and must be tested for chunk save/load, lighting, heightmaps, entity bounds, world border, and client/server packet consistency.
