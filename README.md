# LimbusEGO — Limbus E.G.O 統一插件

將邊獄公司（Limbus Company）的 E.G.O 武器與 E.G.O 飾品一併帶進 Minecraft 的單一 Paper 插件。

- **版本**：1.0.0
- **Minecraft 版本**：1.21.4
- **平台**：Paper
- **Java**：21
- **軟相依**：[ProtocolLib](https://www.spigotmc.org/resources/protocollib.1997/)（可選，攔截莊嚴哀悼周圍他人聽到的原版弓箭聲）

## 這是什麼

`LimbusEGO-1.0.0.jar` 是由兩個舊插件合併而成的單一插件，行為與舊版完全一致（Phase 1：合併不改遊戲行為）：

- **Limbus E.G.O Weapons v3.2.0** → 12 屬性體系、理智值（SAN）系統、8 種 E.G.O 武器
- **Limbus E.G.O Gifts v2.5.0** → 80 件 E.G.O 飾品＋4 種殘影升級材料、抽取箱／紡錘抽獎箱／購買商店箱

主類改名為 `me.yisang.limbusego.LimbusEGO`；原飾品插件主類降級為內部模組 `GiftsModule`，由主插件持有並委派生命週期。兩側原本的 PDC（物品 / 玩家資料）namespace 全數保留（見下方「安裝與資料遷移」），舊物品、舊玩家升級進度無痛沿用。

> 飾品效果改寫（串接 12 屬性體系）為 **Phase 2** 計畫；資源包合併為單一資源包為 **Phase 3** 計畫。本專案取代下方兩個舊 repo，屬於它們的延續與整合。

---

## 指令

全部指令收斂進單一指令樹 `/limbusego`（別名 `lego`），僅 `/accessories` 保留為玩家日常捷徑。

| 指令 | 說明 | 權限 |
|------|------|------|
| `/limbusego weapon give <玩家> <id> [數量]` | 給予玩家武器（主控台可執行） | `limbus.admin` |
| `/limbusego weapon catalog` | 開啟玩家武器圖鑑 | 所有人 |
| `/limbusego weapon admin` | 開啟武器管理員 GUI | `limbus.admin` / OP |
| `/limbusego weapon <id>` | 直接給自己一把指定武器 | `limbus.admin` / OP |
| `/limbusego gift menu` | 取得飾品欄位開啟物品（下界之星） | 所有人 |
| `/limbusego gift give <id> [數量]` | 給自己一件指定飾品 | `limbus.admin` / OP |
| `/limbusego gift give <玩家> <id\|menu\|thread\|lunacy> [數量]` | 給指定玩家飾品／道具（相容舊 `/getgift give` 語法） | `limbus.admin` |
| `/limbusego gift category` | 開啟飾品圖鑑（依 Tier 分頁瀏覽） | 所有人 |
| `/limbusego chest gacha <set\|remove>` | 設定／移除所看之箱子為飾品提取箱 | `limbus.admin` / OP |
| `/limbusego chest thread <set <消耗> [thread\|lunacy] <名稱...>\|remove>` | 設定／移除紡錘抽獎箱 | `limbus.admin` / OP |
| `/limbusego chest shop <set <消耗> [thread\|lunacy] <名稱...>\|remove>` | 設定／移除購買商店箱 | `limbus.admin` / OP |
| `/limbusego reload` | 重新載入設定與兩體系語言檔 | `limbus.admin` |
| `/limbusego language [代碼]` | 顯示目前語言 / 切換語言（同時套用於武器與飾品兩體系） | `limbus.admin` |
| `/accessories` | 開啟飾品欄位 GUI（`/limbusego gift menu` 的捷徑，效果為開啟選單而非給物品） | 所有人 |

全層 Tab 補完皆已支援（root → `weapon give` → 線上玩家 → 武器 id；`gift give` → 飾品 id／玩家名；`chest thread set` → 貨幣種類；`language` → 可用語言代碼……）。

### 舊 → 新指令對照

| 舊指令 | 新指令 |
|---|---|
| `/getego give <玩家> <id> [數量]` | `/limbusego weapon give <玩家> <id> [數量]` |
| `/getego catalog` / `admin` / `<id>` | `/limbusego weapon catalog` / `admin` / `<id>` |
| `/accessories` | `/limbusego gift menu`（`/accessories` 捷徑保留） |
| `/getgift <id> [數量]` | `/limbusego gift give <id> [數量]` |
| `/egogift category` | `/limbusego gift category` |
| `/gachachest <set\|remove>` | `/limbusego chest gacha <set\|remove>` |
| `/threadchest <set …\|remove>` | `/limbusego chest thread <set …\|remove>` |
| `/shopchest <set …\|remove>` | `/limbusego chest shop <set …\|remove>` |
| `/getego reload` ＋ `/egogift reload` | `/limbusego reload`（同時重載兩體系語言） |
| `/getego language <代碼>` | `/limbusego language <代碼>` |

---

## Limbus 屬性體系

每個實體身上追蹤 `(potency, count)` 雙軸狀態，potency 是威力、count 是剩餘觸發次數。全 in-memory 追蹤，mob unload / 死亡自動清除，不寫入 NBT。

| 屬性 | 效果 |
|------|------|
| 流血 BLEED | 帶血者**攻擊時**消耗 1 count → 對自己造 potency × 0.5 真傷 |
| 燒傷 BURN | 每 2 秒（40 tick）消耗 1 count → potency 真傷（DoT） |
| 易損 FRAGILE | 承傷乘 (1 + potency × 15%) 乘區 |
| 強壯 POWER | 出手乘 (1 + potency × 10%) 乘區；每次出手 −1 count |
| 沉淪 SINKING | 受擊消耗 1 count → potency 真傷 + 玩家 SAN −1（SAN 觸底轉憂鬱 ×1.5）；potency 越高移速越低（−2%/potency，上限 −50%） |
| 破裂 RUPTURE | 受擊消耗 1 count → potency × 2 真傷（速殺高血量目標用） |
| 震顫 TREMOR | 累積 potency；受擊且 potency ≥ 5 → **爆發**：消耗全部 potency 造 potency × 3 真傷 + 派生燒傷 5p/3c |
| 守護 PROTECTION | 承傷乘 (1 − potency × 5%)，在易損之前套用 |
| 迅捷 HASTE | Speed potion wrapper：amplifier = potency−1，duration = count 秒 |
| 束縛 BIND | Slowness potion wrapper，同上 |
| 呼吸法 POISE | 出手 min(60%, potency × 5%) 機率**爆擊 ×1.75**；每次出手 −1 count |
| 充能 CHARGE | 出手乘 (1 + potency × 3%)；每次出手 −1 count |

**理智值 SAN**：每位玩家一條 BossBar 常態顯示（範圍 −45 ~ +45），命中／受擊／沉淪增減 SAN，脫戰後負值自動回升、進食可回 SAN，並微調攻擊力與移速；觸底（−30 以下恐慌、−45 理智觸底）會疊加負面狀態效果。

---

## 安裝與資料遷移（從舊兩插件升級）

1. **移除舊插件**：從 `plugins/` 移出 `LimbusEGOWeapons-*.jar` 與 `LimbusEGOGift-*.jar`（建議備份而非直接刪除）。
2. **放入新插件**：把 `LimbusEGO-1.0.0.jar` 放進 `plugins/`。
3. **資料遷移**：把舊 `plugins/LimbusEGOGift/` 資料夾內的 `gacha_chests.yml`、`thread_chests.yml`、`shop_chests.yml`、`config.yml` 複製到新的 `plugins/LimbusEGO/`（若舊 `plugins/LimbusEGOWeapons/config.yml` 有自訂語言設定，注意合併 `language` 欄位，避免被覆蓋）。
4. **啟動伺服器**——舊物品與玩家升級資料自動相容：武器側 PDC 固定落在 `limbusegoweapons:` namespace、飾品側固定落在 `limbusegogift:` namespace（與舊兩插件產生的資料完全一致），玩家背包裡的舊武器／舊飾品、飾品欄位裡已升級的等級都無需任何轉檔即可繼續使用。

---

## 資源包

Phase 1（本版本）**仍沿用兩個舊資源包**，插件啟動時分別非同步下載並校驗到 data folder，交由外部 ResourcePackManager 合併分發（本插件不主動推送、不踢人）：

| 資源包 | 用途 | 目前版本 | data folder 檔名 |
|---|---|---|---|
| [Limbus-E.G.O-weapon-plugin-ResourcePack](https://github.com/Crossing-Dead-Development/Limbus-E.G.O-weapon-plugin-ResourcePack) | 武器外觀 | v.2.17 | `resourcepack-weapons.zip` |
| [Limbus_E.G.O_Gifts_plugin_ResourcePack](https://github.com/Crossing-Dead-Development/Limbus_E.G.O_Gifts_plugin_ResourcePack) | 飾品外觀 | v2.6 | `resourcepack-gifts.zip` |

**Phase 3 計畫**：兩個資源包將合併為單一新 repo `Limbus-E.G.O-ResourcePack`，屆時插件會改為單一 `PACK_URL` / `PACK_HASH`，本節會隨之更新。

---

## 舊專案與棄用聲明

本專案（`Limbus-E.G.O`）取代並整合以下兩個舊插件 repo，**舊 repo 不再更新**：

- 舊武器插件：<https://github.com/Crossing-Dead-Development/Limbus-E.G.O-Weapons>
- 舊飾品插件：<https://github.com/Crossing-Dead-Development/Limbus-E.G.O-Gifts>

（兩個資源包 repo 目前仍在使用中，尚未棄用，詳見上方「資源包」一節；待 Phase 3 資源包合併完成後，上述舊 repo 會一併封存。）

---

### 📜 授權 / 致謝

角色、飾品名稱、原始設定等版權屬 **Project Moon / Limbus Company** 所有；本專案為非商業性同人插件。
