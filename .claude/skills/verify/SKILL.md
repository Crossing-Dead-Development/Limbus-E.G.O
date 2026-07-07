---
name: verify
description: 用拋棄式 Paper 伺服器 + mineflayer 機器人實測 LimbusEGO 插件變更（GUI 點擊、指令、PDC 驗證）
---

# LimbusEGO 拋棄式伺服器實測流程

## 架伺服器（scratchpad）

1. 下載 Paper：`https://fill.papermc.io/v3/projects/paper/versions/1.21.4/builds/latest`
   → `downloads.'server:default'.url`（舊 v2 API 已停用）。
2. `eula.txt`: `eula=true`；`server.properties`:
   `online-mode=false`、`server-port=25599`、`enable-rcon=true`、`rcon.port=25600`、
   `rcon.password=limbustest`、`level-type=flat`。
3. 預先 op 機器人：離線 UUID = MD5("OfflinePlayer:<名>") 加 version bits（TestBot =
   `30fecbe1-2271-3418-8553-d3ded0e95f56`），寫入 `ops.json`。
4. `gradlew.bat jar` 後複製 `build/libs/LimbusEGO-*.jar` 到 `plugins/`，
   背景啟動 `java -jar paper.jar nogui *> server.log`。

## 機器人（mineflayer + rcon-client）

- `npm i mineflayer rcon-client`；createBot `{version:'1.21.4', auth:'offline'}`。
- 飾品選單：bot.chat('/accessories') → `windowOpen`；容器 0–26，玩家背包 27+。
  裝備＝clickWindow(來源格) → clickWindow(11)；升級＝拿起殘影點飾品格。
- 物品驗證：`JSON.stringify(item)` 內含 PDC（`PublicBukkitValues`）與 lore 全文。
- 玩家 PDC：RCON `data get entity <名>`（注意 RCON 回應 ~4096 bytes 截斷，
  長資料改由機器人端 bot.inventory 讀）。
- 效果驗證：`spawn-monsters=false` 會即刻移除召喚的殭屍——**改攻擊牛**
  （applyScaled 對任何 LivingEntity 有效），再 `limbusego status get @e[type=cow,limit=1]`。
- 遷移類測試：機器人離線後用 prismarine-nbt 改 `world/playerdata/<uuid>.dat`
  的 `BukkitValues`（gzip 寫回），重登觀察。

## 踩雷

- 給飾品：`limbusego gift give <玩家> <id>`（每次一件，數量參數只對 thread/lunacy 有效）。
- 升級素材對應：dark/faint/twinkling/brilliant vestige ↔ Tier I–IV。
- 伺服器 console 無法直接下指令（背景啟動無 stdin）→ 一律走 RCON。
