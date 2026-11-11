# Build and runtime validation

CI must compile and reobfuscate against exactly Forge 1.16.1-32.0.108 with Java 8 and Gradle 4.10.3.

## Interim cave prototype

A green compile is only a compile check. Before treating the cave prototype as stable, test:

- Launching a clean client with only Forge and Underdeep.
- Launching a dedicated server with the same mod.
- Creating a **fresh test world**; do not first test on a valuable save.
- Exploring several adjacent chunks and checking that tunnels and chambers meet cleanly across chunk borders.
- Checking the cave density and ensuring large rooms do not remove important surface terrain or the bottom bedrock.
- Saving, quitting, and reloading to confirm the one-time chunk marker persists and chunks are not carved again.
- Checking recipes, drops, lighting, and server/client agreement.
- Checking performance while generating several new chunks, especially on lower-end computers.

The current cave pass deliberately stays within the original 0..255 height range (its cave volume is approximately Y=5..62). It does **not** move bedrock below zero or raise the build limit. Its temporary noise salt is fixed, so this interim pattern is not yet based on each world's actual seed.

## Engine-height milestone

Negative-Y support is a separate custom-engine milestone. Before calling it complete, test:

- New world build limits: minimum Y=-64 and maximum placeable block Y=319.
- Bottom bedrock placement and deepslate generation below the old floor.
- Block placement, breaking, and entity collision in all 24 vertical chunk sections.
- Heightmaps, skylight, block light, and client chunk rendering in negative and upper sections.
- Save, full shutdown, and reload; verify blocks, entities, block entities, and light data.
- Dedicated-server restart and matching custom client reconnect; verify chunk and light packets.
- Cave continuity across chunk borders and around the new bottom bedrock.
- Legacy-world behavior only on a separate backup copy with an explicit migration plan.

A green mod-JAR build is not evidence that the custom engine build works. The final release must include matching client and dedicated-server distributions and pass the tests above.
