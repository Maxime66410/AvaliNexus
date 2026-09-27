# Avali Nexus

Avali Nexus is a Minecraft mod for Forge, Fabric/Quilt and NeoForge that adds the Avali to the game, the nomadic space raptors with all there tech. You'll be able to explore their biome, find their villages, meet new creatures and get your hands on some futuristic gear to survive the cold.

> ⚠️ **Heads up:** the mod is still very early in dev. Right now theres basically only the core (config, settings command, the multi-loader setup). Everything below is what I'm planning to add, not whats in the mod yet.

## What's planned

**The biome**
A really cold polar biome, kind of like a snowy taiga but way more extreme, with alot of snow piling up and bad weather. Plus a bunch of new Avali blocks for building, decoration and tech stuff, and at least one new fluid (probably some kind of fuel or coolant, not decided yet).

**Avalis and creatures**
Avali villages and nomad Avalis wandering around, and some new animals/creatures that live in the biome. Later on I'd like to add Avali companions you can recruit, each with there own role.

**Talking to NPCs**
When you interact with an Avali you get a small dialogue overlay on the side of the screen, instead of a big menu that covers everything. You can pick between Talk, Trade or Leave. Talk shows the dialogue and lore right in the overlay, Trade opens a proper shop window to buy and sell gear and ressources.

**Quests**
Quests are handled with FTB Quests and FTB Library, so the Avalis can give you quest lines and rewards.

**Weapons**
Light melee weapons (blades and that kind of stuff) and energy weapons for ranged combat.

## Commands (OP only)

| Command | What it does |
|---|---|
| `/avalinexus settings` | Shows all the settings and there current value |
| `/avalinexus settings <parameter> <value>` | Changes a setting |

## Settings

Saved in `config/avalinexus.json`.

| Parameter | Default | What it does |
|---|---|---|
| `debugLogging` | false | Turns on extra debug logs |

## Versions

- Minecraft 26.3
- Forge 66.0.5
- NeoForge 26.3.0.23-beta
- Fabric Loader 0.19.5 / Fabric API 0.161.0+26.3
- Will depend on GeckoLib (models and animations) and FTB Quests + FTB Library later

## Project structure

All 3 loaders use the same code in `src/`, each one just has its own small package to boot the mod (`org.furranystudio.avalinexus.forge`, `.neoforge` and `.fabric`).

```
art/                         raw model files (.cpmproject, blockbench), not included in the jar
forge/ neoforge/ fabric/     one gradle project per loader
src/main/java/org/furranystudio/avalinexus/
src/main/resources/
  assets/avalinexus/
    geo/                     GeckoLib models
    animations/              GeckoLib animations
    textures/entity/         entity textures
    lang/                    translations
  data/avalinexus/           loot tables, recipes, structures...
```

The models are made as `.cpmproject`, converted with the CPM plugin in Blockbench and then rendered with GeckoLib in game.

## Building

There's no root build, each loader folder is a seperate gradle project so you build them with `-p`:

```
gradle -p forge build
gradle -p fabric build
gradle -p neoforge build
```

You need Gradle 9.7+ running on JDK 25.
