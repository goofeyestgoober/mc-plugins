# ShardSMP (SuperhumanShard)
The shard abilities plugin for the pupur SMP. Builds to `build/libs/shardsmp-<version>.jar`.

Needs the resource pack in [`resource-packs/shardsmp`](../../resource-packs/shardsmp) when `use-custom-textures: true`.

## Where this code came from
The original source code was lost, so this was rebuilt (decompiled) from the v31 jar
(`archive/shardsmp/v31-ThickWings-MoreParticles.jar`, plugin version 3.0.0).
It was checked against that jar: same classes, methods, commands, config and text.

Things to know when editing:
- Local variables have generic names (`var1`, `var2`, …) because the real names weren't in the jar.
  Rename them as you work on a file.
- Classes like `ShardV22Main` → `ShardV30Main` extend each other: each version was layered on top of the last.
  `plugin.yml` starts `ShardV30Main`.
- The old jar contained some dead code that never ran (e.g. an old Frost ability replaced by `FrostBreath`); it was left out.
