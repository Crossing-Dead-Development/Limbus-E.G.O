# LimbusEGO — Unified Limbus E.G.O Plugin

[繁體中文](README.md) | English

A single Paper plugin that brings both the E.G.O weapons and E.G.O gifts (accessories) of Limbus Company into Minecraft.

- **Version**: 1.2.0
- **Minecraft version**: 1.21.4
- **Platform**: Paper
- **Java**: 21
- **Soft dependency**: [ProtocolLib](https://www.spigotmc.org/resources/protocollib.1997/) (optional; suppresses the vanilla bow sound others hear around Solemn Lament)

## What is this

`LimbusEGO-1.2.0.jar` is a single plugin merged from two legacy plugins:

- **Limbus E.G.O Weapons v3.2.0** → the 12-status system, the Sanity (SAN) system, and 8 E.G.O weapons
- **Limbus E.G.O Gifts v2.5.0** → 80 E.G.O gifts + 4 vestige upgrade materials, plus gacha / thread lottery / shop chests

The main class is renamed to `me.yisang.limbusego.LimbusEGO`; the old gift plugin's main class is demoted to an internal module `GiftsModule`, owned by the main plugin which delegates its lifecycle. All original PDC (item / player data) namespaces on both sides are preserved (see "Installation & data migration" below), so legacy items and player upgrade progress carry over seamlessly.

**Phase 2 is complete (v1.1.0)**: all 80 gift effects have been reworked. Their on-hit / when-hurt / on-kill / passive hooks now plug directly into the weapon-side 12-status system and Sanity (SAN), and effect strength scales with the gift's upgrade tier (vestiges).

> Merging the two resource packs into a single pack is the **Phase 3** plan. This project supersedes the two legacy repos below and is their continuation and integration.

---

## Commands

All commands converge into a single command tree `/limbusego` (alias `lego`); only `/accessories` is kept as a daily shortcut for players.

| Command | Description | Permission |
|------|------|------|
| `/limbusego weapon give <player> <id> [amount]` | Give a weapon to a player (console allowed) | `limbus.admin` |
| `/limbusego weapon catalog` | Open the weapon catalog | everyone |
| `/limbusego weapon admin` | Open the weapon admin GUI | `limbus.admin` / OP |
| `/limbusego weapon <id>` | Give yourself a specific weapon | `limbus.admin` / OP |
| `/limbusego gift menu` | Open the gift menu (same as `/accessories`; to give the menu item use `/limbusego gift give menu`) | everyone |
| `/limbusego gift give <id> [amount]` | Give yourself a specific gift | `limbus.admin` / OP |
| `/limbusego gift give <player> <id\|menu\|thread\|lunacy> [amount]` | Give a gift / item to a player (compatible with the old `/getgift give` syntax) | `limbus.admin` |
| `/limbusego gift category` | Open the gift catalog (browse by tier) | everyone |
| `/limbusego chest gacha <set\|remove>` | Set / remove the targeted chest as a gift gacha chest | `limbus.admin` / OP |
| `/limbusego chest thread <set <cost> [thread\|lunacy] <name...>\|remove>` | Set / remove a thread lottery chest | `limbus.admin` / OP |
| `/limbusego chest shop <set <cost> [thread\|lunacy] <name...>\|remove>` | Set / remove a shop chest | `limbus.admin` / OP |
| `/limbusego reload` | Reload config and both language files | `limbus.admin` |
| `/limbusego language [code]` | Show the current language / switch language (applies to both weapons and gifts) | `limbus.admin` |
| `/accessories` | Open the gift slot GUI (shortcut for `/limbusego gift menu`; opens the menu rather than giving an item) | everyone |

Tab completion is supported at every level (root → `weapon give` → online players → weapon ids; `gift give` → gift ids / player names; `chest thread set` → currency type; `language` → available language codes, and so on).

### Old → new command mapping

| Old command | New command |
|---|---|
| `/getego give <player> <id> [amount]` | `/limbusego weapon give <player> <id> [amount]` |
| `/getego catalog` / `admin` / `<id>` | `/limbusego weapon catalog` / `admin` / `<id>` |
| `/accessories` | `/limbusego gift menu` (the `/accessories` shortcut remains) |
| `/getgift <id> [amount]` | `/limbusego gift give <id> [amount]` |
| `/egogift category` | `/limbusego gift category` |
| `/gachachest <set\|remove>` | `/limbusego chest gacha <set\|remove>` |
| `/threadchest <set …\|remove>` | `/limbusego chest thread <set …\|remove>` |
| `/shopchest <set …\|remove>` | `/limbusego chest shop <set …\|remove>` |
| `/getego reload` + `/egogift reload` | `/limbusego reload` (reloads both language systems) |
| `/getego language <code>` | `/limbusego language <code>` |

---

## The Limbus status system

Each entity carries `(potency, count)` two-axis statuses: potency is strength, count is the remaining trigger count. All tracking is in-memory; statuses are cleared automatically on mob unload / death and are never written to NBT.

| Status | Effect |
|------|------|
| BLEED | When the bleeding entity **attacks**, consume 1 count → deal potency × 0.5 true damage to itself |
| BURN | Every 2s (40 ticks) consume 1 count → potency true damage (DoT) |
| FRAGILE | Damage taken multiplied by (1 + potency × 15%) |
| POWER | Damage dealt multiplied by (1 + potency × 10%); −1 count per attack |
| SINKING | On being hit, consume 1 count → potency true damage + player targets lose 1 SAN (SAN bottoming out turns Depressed for ×1.5); higher potency slows movement (−2%/potency, capped at −50%) |
| RUPTURE | On being hit, consume 1 count → potency × 2 true damage (for bursting high-HP targets) |
| TREMOR | Accumulates potency; when hit at potency ≥ 5 → **burst**: consume all potency for potency × 3 true damage + derived Burn 5p/3c |
| PROTECTION | Damage taken multiplied by (1 − potency × 5%), applied before Fragile |
| HASTE | Speed potion wrapper: amplifier = potency−1, duration = count seconds |
| BIND | Slowness potion wrapper, same as above |
| POISE | On attack, min(60%, potency × 5%) chance to **crit ×1.75**; −1 count per attack |
| CHARGE | Damage dealt multiplied by (1 + potency × 3%); −1 count per attack |

**Sanity (SAN)**: every player has a persistent BossBar (range −45 to +45). Hitting / getting hit / Sinking raise or lower SAN; negative SAN recovers automatically out of combat and eating restores SAN. SAN slightly adjusts attack power and movement speed, and bottoming out (panic below −30, rock bottom at −45) stacks negative status effects.

---

## Installation & data migration (upgrading from the two legacy plugins)

1. **Remove the old plugins**: move `LimbusEGOWeapons-*.jar` and `LimbusEGOGift-*.jar` out of `plugins/` (back them up rather than deleting).
2. **Drop in the new plugin**: put `LimbusEGO-1.2.0.jar` into `plugins/`.
3. **Migrate data**: copy `gacha_chests.yml`, `thread_chests.yml`, `shop_chests.yml`, and `config.yml` from the old `plugins/LimbusEGOGift/` folder into the new `plugins/LimbusEGO/` (if the old `plugins/LimbusEGOWeapons/config.yml` had a custom language setting, merge the `language` field carefully so it isn't overwritten).
4. **Start the server** — legacy items and player upgrade data are automatically compatible: weapon-side PDC stays in the `limbusegoweapons:` namespace and gift-side PDC in the `limbusegogift:` namespace (identical to what the old plugins produced), so old weapons / gifts in inventories and upgraded gift levels keep working without any conversion.

---

## Resource packs

Phase 1 (still true for this version) **keeps using the two legacy resource packs**. The plugin downloads and verifies both asynchronously into its data folder on startup, then leaves merging and distribution to an external ResourcePackManager (this plugin never force-pushes packs or kicks players):

| Resource pack | Purpose | Current version | data folder filename |
|---|---|---|---|
| [Limbus-E.G.O-weapon-plugin-ResourcePack](https://github.com/Crossing-Dead-Development/Limbus-E.G.O-weapon-plugin-ResourcePack) | Weapon visuals | v.2.17 | `resourcepack-weapons.zip` |
| [Limbus_E.G.O_Gifts_plugin_ResourcePack](https://github.com/Crossing-Dead-Development/Limbus_E.G.O_Gifts_plugin_ResourcePack) | Gift visuals | v2.6 | `resourcepack-gifts.zip` |

**Phase 3 plan**: the two packs will be merged into a single new repo `Limbus-E.G.O-ResourcePack`; the plugin will then switch to a single `PACK_URL` / `PACK_HASH`, and this section will be updated accordingly.

---

## Changelog

### 1.2.0 (2026-07-03) — Catalog sort modes & tier display

- The gift catalog gains a sort-mode toggle button (bottom-left): **By Tier** (Tier I–IV tabs, the original mode) / **By Status Group** (nine group tabs: Burn, Bleed, Sinking, Rupture, Tremor, Poise, Support, Utility, Original), with gifts sorted by tier within each group
- Every gift's lore now starts with a tier line (tier color + Roman numeral)
- All tier displays now use Roman numerals (I / II / III / IV)

### 1.1.0 (2026-07-03) — Phase 2: 80 gifts wired into the 12-status system

All 80 gift effects were rewritten per the rework design table, completed in nine status groups, fully plugged into the status system and SAN:

- 🔥 **Burn group (8)**: inflict / stack / detonate Burn, including true-damage detonation and bonus damage vs burning targets
- 🩸 **Bleed group (6)**: inflict Bleed, gain benefits vs bleeding targets (Poise / lifesteal / heal conversion)
- 🌊 **Sinking group (10)**: inflict Sinking plus SAN warfare (drain target SAN, restore own SAN, bonus damage vs depressed targets)
- 💥 **Rupture group (11, incl. Bind riders)**: inflict Rupture, bonus damage vs ruptured targets, on-kill spread
- 🔨 **Tremor group (6)**: inflict Tremor, threshold bonus damage, death retaliation
- 🎯 **Poise group (7)**: build Poise on attack, convert at thresholds into Power / SAN
- 💪 **Support group (15)**: self-buffs and counters via Power / Protection / Haste / Charge / Fragile
- 🕊 **QoL group (12)**: lethal-damage save, execute, out-of-combat healing, item magnet, drop duplication and other experience-oriented remakes
- 🌸 **Original group (5)**: original-design gifts wired in (keeping their signature passives)

Global rules: bonus damage capped at +30%, single-application potency capped at p3, and the upgrade multiplier (1.0/1.25/1.5/2.0) applies to potency / chance / values / cooldowns as annotated per gift; all 84 effect texts are synchronized in Chinese and English.

### 1.0.1 (2026-07-03) — Post-merge cleanup

- `/limbusego gift give` (missing arguments) and `/limbusego chest gacha|thread|shop` (missing `set`/`remove`) no longer fail silently; they now reply with usage hints
- The "usage / player not found" messages of `gift give` now go through the language files and follow `/limbusego language`
- Removed merge leftovers: obsolete `/egogift`, `/gachachest` etc. tab completion and reload/language branches in `GiftsModule` (now unified under `/limbusego reload` and `/limbusego language`), plus orphaned language keys
- Fixed paths in `config.yml` comments and in the missing-language-file warning

### 1.0.0 (2026-07-02) — Phase 1 plugin merge

- Weapons v3.2.0 + Gifts v2.5.0 merged into a single plugin with unchanged behavior; unified command tree `/limbusego`

---

## Legacy projects & deprecation notice

This project (`Limbus-E.G.O`) supersedes and integrates the following two legacy plugin repos, which **will no longer be updated**:

- Legacy weapons plugin: <https://github.com/Crossing-Dead-Development/Limbus-E.G.O-Weapons>
- Legacy gifts plugin: <https://github.com/Crossing-Dead-Development/Limbus-E.G.O-Gifts>

(The two resource pack repos are still in use and not yet deprecated — see "Resource packs" above; they will be archived together once the Phase 3 resource pack merge is complete.)

---

### 📜 License / Credits

Characters, gift names, and original settings are the property of **Project Moon / Limbus Company**; this project is a non-commercial fan-made plugin.
