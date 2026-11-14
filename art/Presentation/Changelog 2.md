# Avali Nexus 1.1.0

Hello everyone !

This update is big, i worked a lot on the performance, the villages and many small things you reported to me. Thank you so much for all the feedback since the first version, it help me a lot !

## What's new

- 2 new rare skins : **Ruben** and **Reaper**, good luck to find them ;3
- Avalis now have 3 colors instead of one (body, pattern and feathers), so every pack look more different. And babies mix the 3 colors from the parents !
- New settings for the cold : **Cold speed** for the polar taiga and **Liquid cold speed** for the ammonia and coolant, from 0% to 500%. You can also change them with `/avalinexus settings coldSpeed <value>` or `liquidColdSpeed`
- New command `/avalinexus debug avali`, look at an Avali and you see all his informations in the chat
- Support for **Tough As Nails** ! When it's installed, my cold is off and TAN manage everything : the polar taiga is icy, the heater warm you (only when it's on), the ammonia ice and the liquids cool you, the spiced stew warm you and the kiri fruit and kiri dessert give you a little bit of thirst. Inside a building in the polar taiga you are a bit less cold, and in the ammonia and the coolant you freeze 2 times faster haha. It's optional, the mod work without it
- The graphene tanks, the machines and the heater now work with the pipes of other mods ! And my graphene pipes can also fill and empty the tanks of other mods :D I tested with **Pipez**, **XNet** and **Tech Reborn**. The machines still take only their own fluids from their input sides
- The pipes, tanks and the pump now work with **all the fluids**, not only ours ! Lava, the oil of Tech Reborn, the blood of Biomes O' Plenty... everything go in the pipes and the tanks with its own texture :D
- The **Fluid Tap** can now fill your bucket from the tanks of other mods too, with any fluid that have a bucket (yes even lava haha)
- The graphene tank accept the buckets of any fluid now
- Support for **JEI**, **REI** and **Jade** ! You can see all the recipes of the machines, the loom and the weapon workbench, and with Jade you see the machine progress, the fluids in tanks and pipes, and the job, role and reputation of the Avalis
- Dialogue keys are now in the controls (Avali Nexus category), so you can change them like you want ([#9](https://github.com/Maxime66410/AvaliNexus/issues/9))
- Buildings in villages and camps now have **ice bricks foundations**, no more houses floating over the holes or the water
- New **Avali Village Map** and **Avali Camp Map** ! The merchants sell them for 20 Nexite when you are Friendly with their pack, and sometimes you find one in the warehouses of the villages or in the storage of the camps. The map always show you another village or camp, never the one where you got it
- New logo and a contributors page !

## Changes

- The settings menu have now 2 tabs : Client and Server, with sliders. On a server only the operators can change the server settings
- JEI, REI and Jade work only on NeoForge and Fabric, they don't exist for Forge 26.3 for now. The mod work perfectly without them don't worry
- With Tough As Nails the Cold speed and Liquid cold speed settings are grey in the menu, because TAN manage the cold now
- Villages and camps are more common now ! Someone found his first village at 14000 blocks from spawn haha, so villages are now every 40 chunks (before 64) and camps every 24 chunks (before 32)
- Villages have always enough Avalis now : minimum 4 in the small ones and 8 in the big ones
- The dialogue hint show your real keys, on any keyboard (AZERTY friends this is for you)
- Other keys on the same key than a dialogue key don't trigger anymore during a dialogue
- Dialogue choices are bigger for the long answers
- The log at startup is much more clean, all the "Registered" lines only show with the debug logs

## Performance

I spent a lot of time on this one !

With 100 Avalis on the screen, i was at around **40 FPS** before, now it's around **140 FPS** :D
The Avali textures are now merged in one texture instead of 5 layers, and the game skip everything you can't see (empty faces, hidden bones, transparent pixels...).

The wall tapestries are also much lighter now, and they only render at 32 blocks or less. In my test village full of tapestries i go from ~80 to ~114 FPS.

## Big fixes

- After changing world, you could not talk to the Avalis or shoot with the rail guns anymore until you restart the game
- Streets on slopes was digging tunnels under the road, the players and Avalis falled inside
- On slopes, some streets could also be placed one on another and make big holes in the ground
- All the chests in villages and camps was giving only 1 item of each thing, the enchanted books was not enchanted and the Nexite Tonic was not even a Nexite Tonic haha. Now the quantities are random like they should be
- A **Graphene Tank** lost all its fluid when you break it, now it keep it inside
- On small screens or with a big GUI scale, the dialogue choices and the hint could go out of the screen ([#10](https://github.com/Maxime66410/AvaliNexus/issues/10))

## Small fixes

- Avalis wake up now when you push them out of their bed, like the villagers ([#11](https://github.com/Maxime66410/AvaliNexus/issues/11))
- The dialogue hint was always showing the same keys, even if you changed them ([#9](https://github.com/Maxime66410/AvaliNexus/issues/9))
- The "Debug logs" setting was doing nothing, oops ! Now it really write the details of the Avalis, machines, cold, trades and world generation in the server log

## Translations

- English, French and Spanish are updated with all the new texts

## For modders and modpacks

- You can build the 3 loaders in one time from the root of the project now
- `packs.json` have a new format with `base`, `pattern` and `feathers` colors, the old format still work
- Village layouts have a new `min_beds` value
- Streets use their own processor list `avalinexus:avali_street`
- Tanks, machines and heater use the fluid API of each loader (Forge `IFluidHandler`, NeoForge `ResourceHandler`, Fabric Transfer API), and the graphene pipes see the fluid storages of other mods

## Your old worlds

- The changes on villages and camps only work in new chunks, so you need to explore new zones to find them
- Your Avalis keep the color of their pack, the 2 new colors are picked one time when they load

Thanks again to everyone for the reports !

You found a bug ? Tell me on [GitHub](https://github.com/Maxime66410/AvaliNexus/issues) !
