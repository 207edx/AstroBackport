# Underdeep: real world-height expansion

## Target behavior

The intended Overworld range is **Y=-64 through Y=319 inclusive** (384 buildable layers). The bottom bedrock floor is to move to the new minimum, with a deepslate-rich deep layer and large connected cave systems above it.

This is an engine port, not a normal Forge mod feature. The current 1.16.1 world/chunk/save implementation assumes the original 0..255 range. Do not ship or advertise negative Y until the engine work below is implemented and tested.

## Required engine work

1. **Vertical chunk storage:** change the Overworld section range from 16 sections to 24 sections, with a section-index offset for Y=-64. Preserve the Nether and End dimensions unless they are explicitly migrated too.
2. **World bounds and build checks:** update minimum/maximum build-height queries, valid-position checks, player placement/break checks, collision and entity bounds, void/fall handling, and structure placement limits.
3. **Heightmaps:** make all heightmap types account for the -64 origin and the 384-block span. Recompute heightmaps for generated and migrated chunks.
4. **Lighting:** update sky-light propagation, block-light propagation, section boundary handling, light nibble arrays, and client-side light application for all 24 sections.
5. **Chunk persistence:** update chunk serialization/deserialization and region-file NBT section handling. Old worlds need a safe migration path; never reinterpret their existing section Y values.
6. **Networking:** update full and partial chunk packets, section masks, heightmap payloads, and light update packets. A client and dedicated server must run the same Underdeep engine build.
7. **Client rendering:** update chunk-section indexing, render chunk creation, frustum/culling bounds, and reload behavior for negative sections.
8. **World generation:** generate the new lower terrain, place the bottom bedrock floor at Y=-64, distribute deepslate and ores below the old floor, and carve caves into the new range.

## Cave-generation design

The current interim mod pass uses world-coordinate noise tunnels and grid-aligned cavern rooms. It is still constrained to the original height range and is only a stepping stone. The final generator should run during chunk generation, use the world's seed, be deterministic across chunk borders, and include large rooms, long winding tunnels, vertical shafts, and underground aquifers without changing already-saved terrain unexpectedly.

## Build and release shape

The finished deliverable must be a **matching custom Forge client/server distribution**, not only the ordinary Underdeep mod JAR. The existing ordinary Forge 32.0.108 profile cannot load negative-Y chunks safely by itself.

Pin the exact Minecraft 1.16.1 / Forge 32.0.108 / Java 8 toolchain. Keep all engine patches tied to that exact base. Do not silently upgrade Minecraft, Forge, mappings, or Java.

## Required acceptance tests

- Create a new world and verify the minimum build Y is -64 and the highest placeable block is Y=319; Y=320 is rejected.
- Mine below the old bedrock floor and verify the new bottom bedrock layer is at -64.
- Place, break, and interact with blocks in every vertical section, including Y=-64..-1 and Y=304..319.
- Travel vertically through section boundaries without invisible blocks, lighting gaps, entity clipping, or client/server disagreement.
- Save, fully exit, and reload; verify blocks, heightmaps, lighting, tile entities, and entities persist.
- Restart a dedicated server and reconnect with the matching client; verify chunk and light packets at negative Y.
- Test caves across chunk borders and around the bottom bedrock layer.
- Test a backed-up legacy 1.16.1 world separately; never use a valuable save as the first migration test.

## Current status

- [x] Existing block/recipe prototype.
- [ ] Large cave pass compile and in-game validation.
- [ ] Custom Forge 1.16.1 engine workspace.
- [ ] 24-section chunk storage and negative-Y serialization.
- [ ] Heightmaps, lighting, packets, and client rendering.
- [ ] Bottom bedrock/deepslate generation below Y=0.
- [ ] Client, dedicated-server, save/reload, and legacy-world tests.
