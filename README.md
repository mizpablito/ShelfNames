**English** · [Polski](README.pl.md)

# ShelfNames

A lightweight Minecraft (Paper) plugin that displays the **names of items placed on shelves** (`*_SHELF`) as a **hologram above the block** when a player looks at it.

The plugin is designed with a focus on **performance**, **no unnecessary allocations**, and **minimal impact on the server thread**.

![Show only custom item names](docs/images/hologramForShelf.png)

---

## ✨ Features

- Shows item names from a shelf as a hologram
- Supports every wooden shelf variant (`Tag.WOODEN_SHELVES`)
- Preserves item name colors and formatting
- Updates the hologram only when the contents actually change
- Automatically removes the hologram once the player looks away
- No flickering and no redundant updates
- Fully compatible with Adventure / MiniMessage

---

## ⚙️ How it works

- Every configured number of ticks the plugin checks **which block the player is looking at**
- If it's a shelf:
    - its position is compared with the previous one (cache)
    - a snapshot of the contents is taken **only when the looked-at shelf changes**
- The hologram is updated **only** after the looked-at shelf changes
- Expensive operations (`BlockState`) are performed **only when actually needed**

---

## 🔧 Configuration

A Polish copy of the config comments is available at [`docs/config.pl.yml`](docs/config.pl.yml).

```yaml
# How often (in ticks) to check whether a player is looking at a shelf
update-interval-ticks: 5
# Maximum distance the shelf can be from the player (recommended: no more than 10)
rayTraceBlocks-max-distance: 5
# Whether to show custom (renamed) item names only
only-custom-names: true
# Whether to show holograms to a single player only
only-one-player: true

hologram:
  # Options:
  # - AUTO (automatic selection in the order below, ultimately STANDALONE)
  # - FANCY (FancyHolograms)
  # - STANDALONE (Bukkit/PaperMC API)
  provider: AUTO
  # Whether the hologram should follow the player's view,
  #  or stay fixed, aligned with the front of the shelf
  position-fixed: true
  # Height offset
  offset-y: 0.75
  # Distance away from the shelf
  forward-offset: -0.16
  # Scale of the hologram object
  scale: 0.32

# Hologram configuration depending on the integration in use
integration:
  fancyHolograms:
    # Text shadow
    textShadow: true
    # Text alignment
    # Available options: LEFT, CENTER, RIGHT
    textAlignment: CENTER
    # Whether the hologram should use the default background
    defaultBackground: true
    # ...if not, set the values 0-255
    backgroundARGB:
      alpha: 60
      red: 0
      green: 0
      blue: 0

  # Bukkit API
  standalone:
    # Text shadow (TextDisplay#setShadowed)
    textShadow: true
    # Text alignment
    # Available options: LEFT, CENTER, RIGHT
    textAlignment: CENTER
    # Whether the hologram should use the default background
    defaultBackground: false
    # ...if not, set the values 0-255
    backgroundARGB:
      alpha: 60
      red: 0
      green: 0
      blue: 0

```

---

## 🎮 Commands & permissions

| Command | Permission | Description |
|---|---|---|
| `/shelfnames` (or `/shelfnames info`) | *(none)* | Plugin info: name, version, GitHub link |
| `/shelfnames clear` | `shelfnames.admin` | Removes every hologram created by the plugin |
| `/shelfnames reload` | `shelfnames.admin` | Full restart: destroys holograms, reloads `config.yml`, restarts components |

Players without `shelfnames.admin` can only run the bare `/shelfnames` (info) command.
`shelfnames.admin` defaults to OP.

---

## 📦 Requirements

- Paper 1.21+
- FancyHolograms 2.8.0+
- Java 21

## 🧩 Dependencies

- [FancyHolograms](https://modrinth.com/plugin/fancyholograms)
- Paper API
- Adventure (bundled with Paper)

## 🚀 Planned features

- Optional transition smoothing (fade in / fade out)

## 🚫 Unsupported integrations

### DecentHolograms

[DecentHolograms](https://www.spigotmc.org/resources/decentholograms-1-8-1-21-11-papi-support-no-dependencies.96927/)
**is not supported** — its public API exposes no hologram pinning (billboard `FIXED`),
scaling, or background, so the integration cannot be done the way it is for
FancyHolograms or the Bukkit API. Dependency-free replacement: the `STANDALONE` provider
(Bukkit `TextDisplay`), which honors `hologram.scale` and `hologram.position-fixed`.

## 🧠 Technical notes

The plugin does not use NMS, does not send its own packets, and does not interfere with the server tick loop.

It was optimized using the Spark Profiler and tested under realistic load.
