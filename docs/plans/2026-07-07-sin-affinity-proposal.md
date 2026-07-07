# 罪孽屬性抗性系統 — 企劃書

日期：2026-07-07
性質：**企劃書（未實作）**。承接 2026-07-05 交接文件 Part 3~5 的方向，落成可執行的設計。
前置：`2026-07-07-status-rebalance-design.md` 的新傷害管線（已完成）——正因狀態傷害已走原版
`EntityDamageByEntityEvent`，抗性乘區才有單一、可靠的掛載點。

---

## 1. 核心論點

Limbus Company 的傷害平衡靠**雙軸抗性框架**約束：每次攻擊帶「物理型別」與「罪」兩個標籤，
防禦方對兩軸各有 affinity 倍率。插件早期把這框架拔掉、改成各飾品手填真傷，才導致數值不可量化。
本企劃**不設計新系統，而是把原作已被官方 tune 過的框架搬回 Minecraft**。

## 2. 模型

每次攻擊攜帶：

- **物理型別**（三選一）：斬 Slash / 突 Pierce / 打 Blunt
- **罪屬性**（七選一）：暴食 Gluttony / 色慾 Lust / 怠惰 Sloth / 暴怒 Wrath / 憂鬱 Gloom / 傲慢 Pride / 嫉妒 Envy

防禦方對每軸各有 affinity，**範圍 0.4 ~ 2.0，以 1.0 為中性**，兩軸**相乘**：

```
最終傷害 = 事件傷害 × 型別affinity × 罪affinity
例：斬·憂鬱武器 打 目標(斬 1.2, 憂鬱 0.8) → ×0.96
```

乘區套在 `StatusManager.onDamage`（HIGH 優先度）同一處，位於 POWER/CHARGE 之後、
PROTECTION/FRAGILE 之前；狀態傷害（`isStatusDamage`）沿用施術武器的標籤。

倍率邊界規範（沿用原作語感）：

| affinity | 語意 | 顯示 |
|---|---|---|
| 0.4 ~ 0.7 | 耐受 Endure | §7 |
| 0.8 ~ 0.9 | 微抗 | §f |
| 1.0 | 中性 Normal | §f |
| 1.1 ~ 1.5 | 弱點 Fatal 邊緣 | §e |
| 1.6 ~ 2.0 | 致命 Fatal | §c |

## 3. 標籤來源

### 3.1 武器（現有 E.G.O 武器逐把定案，示例）

| 武器 | 型別 | 罪 |
|---|---|---|
| 擬態（Mimicry） | 斬 | 嫉妒 |
| 莊嚴哀悼（Solemn Lament） | 突 | 憂鬱 |
| 提比婭 | 斬 | 暴怒 |
| W社匕首 | 突 | 怠惰 |
| 天樞星 | 打 | 傲慢 |
| （其餘武器實作期逐把補齊） | | |

實作方式：武器模組建構時宣告 `DamageTag(type, sin)`，打擊時寫入事件
（metadata 或 PDC on damager weapon）。

### 3.2 狀態效果 → 罪的固定映射（原作對應）

| 狀態 | 罪 | 型別 |
|---|---|---|
| BURN | 暴怒 | 無型別（純罪軸） |
| BLEED | 色慾 | 無型別 |
| SINKING | 憂鬱 | 無型別 |
| TREMOR | 怠惰 | 無型別 |
| RUPTURE | 暴食 | 無型別 |
| POISE/CHARGE 等增益 | 不參與抗性 | — |

「無型別」= 型別軸按 1.0 計，只吃罪軸。

### 3.3 徒手／原版武器

無標籤 → 兩軸皆 1.0，原版體驗完全不變。**這保證系統可以增量上線。**

## 4. 實體 affinity 資料

YAML 驅動（`affinities.yml`），不寫死在程式：

```yaml
default:            # 未列出的實體
  slash: 1.0
  pierce: 1.0
  blunt: 1.0
  wrath: 1.0
  # ...未列出的軸一律 1.0

entities:
  ZOMBIE:      { slash: 1.2, blunt: 0.8, gloom: 0.7, wrath: 1.5 }
  SKELETON:    { pierce: 0.6, blunt: 1.8 }        # 骷髏怕鈍器，符合直覺
  BLAZE:       { wrath: 0.4, gloom: 1.6 }          # 火系抗暴怒(燒傷)、弱憂鬱
  WITHER:      { gloom: 0.5, pride: 1.4 }
  # ...

players:
  default: { }                                     # 玩家預設全 1.0
  # 之後可由飾品/套裝動態修改玩家 affinity（見 §6 Phase 3）
```

載入層：`AffinityManager`（reload 指令支援），查詢 O(1)：EnumMap<EntityType, Affinity>。

## 5. 玩家可見性

- 圖鑑 GUI：武器/飾品 lore 顯示型別與罪標籤（沿用現有 12 色體系，罪用原作配色：
  暴食棕、色慾橙、怠惰黃、暴怒紅、憂鬱藍、傲慢深藍、嫉妒紫）。
- 命中回饋：affinity ≠ 1.0 時 ActionBar 顯示「耐受/弱點/致命 ×N.N」（沿用 poise_crit 樣式）。
- PAPI：`%limbusego_affinity_<軸>%` 顯示玩家自身 affinity（Phase 3 起有意義）。

## 6. 實作分期

| 期 | 內容 | 規模 |
|---|---|---|
| **Phase 1** | `DamageTag` + `AffinityManager` + onDamage 乘區 + affinities.yml（怪物 10 種起步）＋武器標籤 5 把 | 核心骨架，~3 個新類 |
| **Phase 2** | 全武器標籤補齊、狀態→罪映射接入（狀態傷害吃罪軸）、圖鑑/lore/ActionBar 顯示、語言檔 | 鋪面工程 |
| **Phase 3** | 玩家 affinity（飾品/套裝修改玩家抗性軸）、PAPI 變數、副本 boss 專屬 affinity 表（接交接文件 Part 6 的鉤子） | 深化 |

## 7. 平衡護欄

- 兩軸乘積理論範圍 0.16 ~ 4.0，但資料層規範：**單一實體兩軸乘積不得超過 2.5、不得低於 0.3**
  （載入時驗證，超界 clamp 並 log 警告）。
- 與現有乘區疊乘順序固定：`武器技能 → POWER/CHARGE/POISE → 型別×罪 → PROTECTION → FRAGILE → 原版護甲`。
- Fabric 版（Limbus-E.G.O-Fabric）待 Paper 版 Phase 2 穩定後移植，資料檔共用同一份 YAML。

## 8. 風險

| 風險 | 緩解 |
|---|---|
| 全武器逐把定標籤工作量大 | Phase 1 只做 5 把；無標籤 = 中性，不阻塞上線 |
| 玩家看不懂雙軸 | 圖鑑教學頁 + 命中 ActionBar 即時回饋 |
| 與領地/其他插件衝突 | 乘區只動 `setDamage`，不繞管線；已由本輪管線重做保證 |
