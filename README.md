# Avali Nexus

Avali Nexus is a Minecraft mod for Forge, Fabric/Quilt and NeoForge that adds the Avali to the game, the nomadic space chicken with all there tech. You'll be able to explore their biome, find their villages and get your hands on some futuristic gear to survive the cold.

> ⚠️ **Heads up:** the mod is still in active dev and not released yet. Most of the stuff below is already in the game, but things can still change or break.

## What's in the mod

**The biome**
A really cold polar biome, kind of like a snowy taiga but way more extreme, with alot of snow piling up and blizzards. If you stay out there without warm gear you slowly freeze. The caves under it are frozen too, and you can find lakes of ammonia rimmed with ice.

**Building blocks**
A whole set of Avali blocks: graphene plating, bricks and pillars, aerogel glass in every color, nanocanvas, nanofibre poles, ice bricks and packed snow, plus beds, cushions, carpets, lamps, light strips, curtains and tapestries you design yourself on the Avali loom.

**Fluids and machines**
Three fluids (ammonia, coolant and fuel) that you move around with graphene pipes, tanks, pumps and taps. Machines turn them into stuff: the refinery, the cryo freezer, the graphene press, the weapon workbench, and a heater to stay warm.

**Gear and weapons**
A Nexite tier between diamond and netherite (tools, armor, even horse armor). Light aerotech blades, and rail guns (pistol, carbine, assault rifle, shotgun and sniper) that shoot Nexite quills from magazines you reload.

**Avalis**
Avalis live in packs, each one with its own name, look and mood. When you interact with an Avali you get a small dialogue overlay on the side of the screen, instead of a big menu that covers everything. They fight, flee, sleep, play music together, form couples and have babies, and you can recruit them as companions with there own role. Every pack remembers how you treat them, so your reputation changes what they say, sell and do.

**Camps and villages**
Nomad camps with tents out in the snow, and bigger Avali villages with houses, workshops and archives. Stealing from a village or breaking their machines won't go unnoticed.

**Trading**
Each Avali has a shop based on its job (fisher, hunter, mechanic...) or its workshop. The offers change over time, and the better your reputation the more you unlock.

**Avalon plants**
Piru, Nakati and Kiri, the plants from the Avali lore. You find them in the wild on snow and ice (the Nakati even glows in frozen caves), grow them without any light, and cook Avali food with them, like the spiced stew that keeps you warm for a while. Villages have their own fields, looked after by their gatherers, so don't go picking from them while they're watching.

**Lore**
The terminal opens the Avali archives, with entries about their history, biology and the stuff in the mod.

## What's planned

- **Quests**: FTB Quests support, but that's for way later.

## Customization

Almost everything about the Avalis is in JSON files under `data/avalinexus/avali/` (behavior, combat, dialogues, shops, music, reputation, breeding...), so you can change it with a datapack. Use `/avalinexus reload` to apply your changes without restarting.

## Commands (OP only)

| Command | What it does |
|---|---|
| `/avalinexus settings` | Shows all the settings and there current value |
| `/avalinexus settings <parameter> <value>` | Changes a setting |
| `/avalinexus reload [config]` | Reloads all the Avali JSON files, or just one of them |
| `/avalinexus music list` | Lists all the songs |
| `/avalinexus music play <song>` | Plays a song where you are |
| `/avalinexus music avali <song>` | Makes the closest Avali play a song |
| `/avalinexus music stop` | Stops the music around you |
| `/avalinexus reputation get` | Shows your reputation with every pack |
| `/avalinexus reputation set <pack> <value>` | Sets your reputation with a pack |
| `/avalinexus ping` | Checks the connection between the server and your game |

## Settings

Saved in `config/avalinexus.json`.

| Parameter | Default | What it does |
|---|---|---|
| `debugLogging` | false | Turns on extra debug logs |
| `avaliFontUi` | true | Uses the Avali font for titles, buttons and labels |
| `avaliFontReading` | false | Uses the Avali font for texts you read (dialogues, archives, shop) |

## Versions

- Minecraft 26.3
- Forge 66.1.0
- NeoForge 26.3.0.95
- Fabric Loader 0.19.5 / Fabric API 0.164.0+26.3
- GeckoLib 5.5.7 (models and animations), needs to be installed with the mod
- TerraBlender 26.3.0.0.9 (adds the polar biome to the overworld), needs to be installed with the mod too
- Mod Menu 21.0.0 on Fabric is optional, it just adds the config button to the mod list
- FTB Quests + FTB Library support is planned for way later

## Project structure

All 3 loaders use the same code in `src/`, each one just has its own small package to boot the mod (`org.furranystudio.avalinexus.forge`, `.neoforge` and `.fabric`).

```
.github/                     issue and pull request templates
art/                         raw model files (.cpmproject, blockbench), not included in the jar
forge/ neoforge/ fabric/     one gradle project per loader
src/main/java/org/furranystudio/avalinexus/
src/main/resources/
  assets/avalinexus/
    geckolib/models/         GeckoLib models
    geckolib/animations/     GeckoLib animations
    textures/                block, item and entity textures
    lang/                    translations
  data/avalinexus/           loot tables, recipes, structures, Avali configs...
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

## Contributing

Want to help? Check [CONTRIBUTING.md](CONTRIBUTING.md) first. Translations are done on [Crowdin](https://crowdin.com/project/avalinexus). If you find a security issue, please follow [SECURITY.md](SECURITY.md) instead of opening a public issue. Everyone is expected to follow the [Code of Conduct](CODE_OF_CONDUCT.md).

## Credits

- The **Avali Latin** font used in the dialogue UI is made by **AikaDee** on [FontStruct](https://fontstruct.com/fontstructions/show/2855105/avali-scratch-28), under the [Creative Commons Attribution Share Alike 3.0](http://creativecommons.org/licenses/by-sa/3.0/) license.
- The lore entries of the terminal archives are based on [The Official Avali Wiki](https://avali.fandom.com/wiki/The_Official_Avali_Wiki), whose content is under the [Creative Commons Attribution Share Alike](https://www.fandom.com/licensing) license. The Avali were created by **RyuujinZERO**.
- The Avali player model used for the Avali NPCs comes from the [CPM Avali Model](https://github.com/Cynadyde/CpmAvaliModel) made by [**Cynadyde**](https://github.com/Cynadyde).

## License

Avali Nexus is under the [MIT License](LICENSE).
