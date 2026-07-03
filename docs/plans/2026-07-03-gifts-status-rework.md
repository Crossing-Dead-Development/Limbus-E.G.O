# Phase 2 主線：80 飾品接入 12 屬性體系 實作計畫

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 依 `docs/specs/2026-07-02-80-gifts-rework-design.md`（下稱「設計表」）把 80 件飾品效果全面重構，串接武器側 12 屬性體系（`StatusManager`）與理智值（`SanityManager`），並補 README 英文版。

**Architecture:** 合併後單一插件，飾品類可直接呼叫 `LimbusEGO#getStatusManager()`（設計表第四節的 softdepend 橋接備註已過時，**不做跨插件橋接**）。在 `BaseAccessory` 加共用 helper（屬性施加/查詢/冷卻閘），80 個飾品類逐一改寫 `onAttack`/`onDamaged`/`onKill`/`onPassiveTick` 等鉤子。按設計表的 9 個屬性分組分 9 個任務實作，每組一個 commit+push。

**Tech Stack:** Java 21 / Paper 1.21.4 API / Gradle。無測試框架——本專案為 Bukkit 插件且無 MockBukkit 基建，驗證方式沿用 Phase 1 慣例：每任務 `gradlew compileJava` 通過 + 最終 jar 部署伺服器手動冒煙測試（見 Task 13 清單）。**此為對 TDD 流程的已知偏離，經專案慣例確認。**

## Global Constraints（出自設計表第三節，全任務隱含適用）

- 增傷百分比封頂 **+30%**；單次施加 potency 封頂 **p3**（IV 級擴散除外）
- 升級倍率 `m = plugin.getUpgradeMultiplier(player, getId())`（1.0/1.25/1.5/2.0），作用點依設計表標注：`P`=potency 取整放大、`%`=機率放大、`N`=數值/範圍放大、`CD↓`=冷卻除以 m
- 升級放大 count 一律不超過 +2
- 「保留」= 該飾品現有程式碼中對應部分**不動**，只加/換標注的部分；動手前必讀現有類
- 每個 commit 訊息**中英雙語**（標題 `類型: 中文 / English`，內文逐條中英對照），結尾 `Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>`，每個任務完成即 push
- 飾品效果文案雙語：建構子內中文預設 effect 字串 + `lang/gifts/en_US.yml` 對應 `gift.<id>.effect`，兩者必須同步改
- `zh_TW.yml` 沒有 gift 條目（中文預設在建構子），**不要**往 zh_TW.yml 加 gift 條目
- 事件順序既定事實：GiftsModule 傷害監聽（NORMAL）先於 StatusManager（HIGH）跑；飾品在 onAttack 內施加的 SINKING/RUPTURE 會被**同一擊**的 StatusManager 消耗結算一次（與武器施加行為一致，屬預期行為，不要「修」它）

## 檔案結構

- Modify: `src/main/java/me/yisang/limbusego/gift/BaseAccessory.java` — 共用 helper（Task 1）
- Modify: `src/main/java/me/yisang/limbusego/status/StatusManager.java` — 公開真傷 API（Task 1）
- Modify: `src/main/java/me/yisang/limbusego/status/SanityManager.java` — `gainSan`（Task 1）
- Modify: `src/main/java/me/yisang/limbusego/gift/gifts/*.java` — 80 個飾品類（Task 2–10）
- Modify: `src/main/resources/lang/gifts/en_US.yml` — 80 條 effect 文案（隨各組任務改）
- Modify: `README.md`；Create: `README.en.md`（Task 12）
- Modify: `build.gradle.kts` / `src/main/resources/plugin.yml` — 版號 1.1.0（Task 13）

## 效果文案模板（各組任務照此生成，中英同步）

- 中文（建構子第 4/6 參）：`攻擊：施加燒傷 2·2` / `攻擊燒傷中目標：+30% 傷害` / `被動：每 5 秒獲得強壯 2·2` / `受擊：對攻擊者施加沉淪 3·2` / `擊殺：對 5 格內敵人擴散流血 2·2`；多段用 `｜` 分隔；保留段沿用原文案接在後面
- 英文（en_US.yml `effect:`）：`On hit: Inflict Burn 2·2` / `On hit vs burning target: +30% damage` / `Passive: Gain Power 2·2 every 5s` / `When hurt: Inflict Sinking 3·2 on attacker` / `On kill: Spread Bleed 2·2 to enemies within 5 blocks`；分隔用 ` | `
- 屬性英文名固定：Bleed / Burn / Sinking / Rupture / Tremor / Poise / Power / Protection / Fragile / Haste / Bind / Charge / SAN
- `2·2` = potency·count（與武器側 ActionBar 顯示格式一致）

---

### Task 1: 基礎設施 — API helper 與冷卻閘

**Files:**
- Modify: `src/main/java/me/yisang/limbusego/status/StatusManager.java`
- Modify: `src/main/java/me/yisang/limbusego/status/SanityManager.java`
- Modify: `src/main/java/me/yisang/limbusego/gift/GiftsModule.java`
- Modify: `src/main/java/me/yisang/limbusego/gift/BaseAccessory.java`

**Interfaces:**
- Produces（後續全部任務依賴，簽名必須一字不差）:
  - `StatusManager#hurtTrue(LivingEntity target, Player source, double amount, StatusEffect label)`
  - `SanityManager#gainSan(Player p, int amount)`
  - `GiftsModule#status()` → `StatusManager`、`GiftsModule#sanity()` → `SanityManager`
  - `BaseAccessory#applyScaled(LivingEntity target, StatusEffect eff, int p, int c, Player src)`（potency×m 取整，`P` 升級用）
  - `BaseAccessory#apply(LivingEntity target, StatusEffect eff, int p, int c, Player src)`（不放大）
  - `BaseAccessory#has(LivingEntity e, StatusEffect eff)`、`BaseAccessory#pot(LivingEntity e, StatusEffect eff)`
  - `BaseAccessory#gate(Player p, long ms)`（冷卻閘，true=可觸發並記錄本次）
  - `BaseAccessory#victimOf(EntityDamageByEntityEvent ev)` → `LivingEntity`（受擊者，非 LivingEntity 回 null）

- [ ] **Step 1: StatusManager 公開真傷 API**

在 `scheduleTrueDamage` 上方加：

```java
    /** 對外真傷 API：供飾品引爆/斬殺/擴散等效果使用（帶屬性標籤顯示、防遞迴）。 */
    public void hurtTrue(LivingEntity target, Player source, double amount, StatusEffect label) {
        if (amount <= 0) return;
        scheduleTrueDamage(target, source, amount, label);
    }
```

- [ ] **Step 2: SanityManager 加 gainSan**

在 `dropSan` 旁加：

```java
    /** 回復 SAN（飾品回復理智用），上限由 setSan 自動夾住。 */
    public void gainSan(Player p, int amount) {
        if (amount <= 0) return;
        setSan(p, getSan(p) + amount);
    }
```

- [ ] **Step 3: GiftsModule 加屬性系統委派**

在「── 語言 ──」區塊上方加（注意：StatusManager 在 onEnable 中晚於 gifts.enable() 建立，**任何飾品不得在建構子/enable 階段呼叫 status()**，只能在事件鉤子內用——鉤子必然在 onEnable 完成後才觸發，安全）：

```java
    // ── 屬性系統委派（合併後直接取用武器側管理器；僅限事件鉤子內呼叫）──────
    public me.yisang.limbusego.status.StatusManager status() { return plugin.getStatusManager(); }
    public me.yisang.limbusego.status.SanityManager sanity() { return plugin.getSanityManager(); }
```

- [ ] **Step 4: BaseAccessory 加共用 helper**

import 增加 `org.bukkit.entity.LivingEntity`、`org.bukkit.event.entity.EntityDamageByEntityEvent`、`me.yisang.limbusego.status.StatusEffect`、`me.yisang.limbusego.status.StatusManager`、`java.util.HashMap`、`java.util.UUID`。類內加：

```java
    // ── 12 屬性體系共用 helper（Phase 2）─────────────────────────────────
    private final Map<UUID, Long> gateMap = new HashMap<>();

    protected StatusManager status() { return plugin.status(); }

    /** 施加屬性，potency 依升級倍率取整放大（設計表升級標注 P 用這個）。 */
    protected void applyScaled(LivingEntity target, StatusEffect eff, int p, int c, Player src) {
        double m = plugin.getUpgradeMultiplier(src, getId());
        status().apply(target, eff, (int) Math.round(p * m), c, src);
    }

    /** 施加屬性，不做升級放大（升級作用在別處時用這個）。 */
    protected void apply(LivingEntity target, StatusEffect eff, int p, int c, Player src) {
        status().apply(target, eff, p, c, src);
    }

    protected boolean has(LivingEntity e, StatusEffect eff) { return pot(e, eff) > 0; }

    protected int pot(LivingEntity e, StatusEffect eff) {
        var s = status().get(e);
        return s == null ? 0 : s.potency(eff);
    }

    /** 每玩家冷卻閘：距上次觸發超過 ms 才回 true 並記錄本次。5 秒脈衝、內建 CD 都用它。 */
    protected boolean gate(Player p, long ms) {
        long now = System.currentTimeMillis();
        Long last = gateMap.get(p.getUniqueId());
        if (last != null && now - last < ms) return false;
        gateMap.put(p.getUniqueId(), now);
        return true;
    }

    /** 取受擊者（非 LivingEntity 回 null）。 */
    protected LivingEntity victimOf(EntityDamageByEntityEvent ev) {
        return ev.getEntity() instanceof LivingEntity le ? le : null;
    }

    @Override
    public void onQuit(Player player) { gateMap.remove(player.getUniqueId()); }
```

**注意**：子類若自己 override `onQuit`，必須呼叫 `super.onQuit(player)`（Task 2–10 改寫時檢查每個有 onQuit 的類）。

- [ ] **Step 5: 編譯驗證**

Run: `.\gradlew.bat compileJava -q`
Expected: EXIT=0（僅既有 deprecation 警告）

- [ ] **Step 6: Commit + push**

```
feat: 飾品接屬性體系的基礎 API / Infrastructure APIs for wiring gifts into the status system
```

---

### 各飾品任務通用步驟（Task 2–10 每組執行一輪）

1. 逐一改寫該組飾品類（規格見下方各組明細；「保留」項先讀現有類再動）
2. 同步改建構子中文 effect 文案 + `en_US.yml` 對應 `gift.<id>.effect`（照文案模板）
3. `.\gradlew.bat compileJava -q` → EXIT=0
4. Commit（雙語）+ push

**通用實作模式**（各組明細以此為基準，只列差異）：

```java
// 模式 A：攻擊施加屬性（升級 P）
@Override public void onAttack(EntityDamageByEntityEvent event, Player attacker) {
    LivingEntity target = victimOf(event);
    if (target == null) return;
    applyScaled(target, StatusEffect.BURN, 2, 2, attacker);
}

// 模式 B：條件增傷（升級 N%，封頂 +30%）
if (has(target, StatusEffect.SINKING)) {
    double m = plugin.getUpgradeMultiplier(attacker, getId());
    event.setDamage(event.getDamage() * (1.0 + Math.min(0.30, 0.15 * m)));
}

// 模式 C：每 5 秒被動（onPassiveTick 是每秒呼叫，用 gate 併成 5 秒脈衝；升級 P）
@Override public void onPassiveTick(Player player) {
    if (!gate(player, 5000)) return;
    applyScaled(player, StatusEffect.POWER, 2, 2, player);
}

// 模式 D：受擊反制（升級 P）
@Override public void onDamaged(EntityDamageByEntityEvent event, Player victim) {
    if (event.getDamager() instanceof LivingEntity atk)
        applyScaled(atk, StatusEffect.SINKING, 3, 2, victim);
}

// 模式 E：擊殺範圍擴散（升級 N 範圍 = 基礎格數 × m 取整）
@Override public void onKill(EntityDeathEvent event, Player killer) {
    double m = plugin.getUpgradeMultiplier(killer, getId());
    double r = 3 * m;
    for (var e : event.getEntity().getLocation().getNearbyLivingEntities(r)) {
        if (e.equals(killer) || e instanceof Player) continue;
        apply(e, StatusEffect.BURN, 3, 2, killer);
    }
}

// 模式 F：機率觸發（升級 % = 機率 × m，封頂 1.0）
if (Math.random() < Math.min(1.0, 0.20 * m)) { ... }

// 模式 G：內建 CD（升級 CD↓ = 基礎 ms ÷ m）
if (!gate(attacker, (long)(8000 / m))) return;
```

---

### Task 2: 🔥 燒傷組（8 件）

**Files:** Modify `gift/gifts/{ArdentFlower,AshesToAshes,BloodFlameSword,DustToDust,GlimpseOfFlames,HotNJuicyDrumstick,PainOfStifledRage,RoyalJellyPerfume}.java` + `en_US.yml`

- [ ] `ardent_flower`（III，升級 P）：**保留** onPassiveTick 火焰免疫。新增 onAttack：`applyScaled(target, BURN, 2, 2, attacker)`；若 `has(target, BURN)` 且 `target.getHealth() < target.getAttribute(Attribute.MAX_HEALTH).getValue() * 0.30` → `event.setDamage(event.getDamage() * 1.30)`
- [ ] `ashes_to_ashes`（I，升級 P）：onAttack：僅當 `has(target, BURN)` 時 `applyScaled(target, BURN, 2, 1, attacker)`（疊層器，對未燒傷目標無效）
- [ ] `bloodflame_sword`（II，升級 P）：onAttack：`applyScaled(target, BURN, 3, 2, attacker)` + `plugin.sanity().gainSan(attacker, 1)`
- [ ] `dust_to_dust`（III，升級 P·N範圍）：onAttack：`applyScaled(target, BURN, 3, 2, attacker)`；onKill：模式 E，半徑 `3*m`，擴散 `apply(e, BURN, 3, 2, killer)`
- [ ] `glimpse_of_flames`（IV，升級 CD↓）：onAttack 引爆：

```java
@Override public void onAttack(EntityDamageByEntityEvent event, Player attacker) {
    LivingEntity target = victimOf(event);
    if (target == null) return;
    var s = status().get(target);
    if (s == null || s.potency(StatusEffect.BURN) <= 0) return;
    double m = plugin.getUpgradeMultiplier(attacker, getId());
    if (!gate(attacker, (long)(8000 / m))) return;
    int p = s.potency(StatusEffect.BURN);
    int consumed = s.consume(StatusEffect.BURN, 5);           // 引爆上限 c5
    if (consumed <= 0) return;
    status().hurtTrue(target, attacker, p * consumed * 0.5, StatusEffect.BURN);
    apply(target, StatusEffect.FRAGILE, 1, 2, attacker);
}
```

- [ ] `hot_n_juicy_drumstick`（III，升級 N(c)，count 加值封頂 +2 → 固定 c+2 不放大）：**保留**飽食被動；onAttack：若 `has(target, BURN)` → `status().refresh(target, StatusEffect.BURN, 2)`
- [ ] `pain_of_stifled_rage`（II，升級 P）：onAttack：若 `has(target, BURN)` → `applyScaled(attacker, POWER, 2, 1, attacker)`，否則 `applyScaled(target, BURN, 2, 2, attacker)`
- [ ] `royal_jelly_perfume`（III，升級 P）：**保留**蜜蜂友好；onDamaged：模式 D 對攻擊者 `applyScaled(atk, BURN, 2, 2, victim)`；另在 onDamaged 開頭：若 `event.getDamager() instanceof LivingEntity atk && has(atk, BURN)` → `event.setDamage(event.getDamage() * 0.85)`
- [ ] 文案雙語同步、`compileJava`、commit+push：`feat: 燒傷組 8 飾品接入屬性體系 / Wire 8 Burn-group gifts into the status system`

---

### Task 3: 🩸 流血組（6 件）

**Files:** Modify `gift/gifts/{CrystallizedBlood,LaManchalandAllDayPass,LaManchalandStandardPass,MaskOfTheParade,Millarca,SanguineBlossomBolus}.java` + `en_US.yml`

- [ ] `crystallized_blood`（II，升級 N上限）：onPassiveTick + `gate(player,5000)`：若 `pot(player, BLEED) > 0`：

```java
var s = status().get(player);
int p = s.potency(StatusEffect.BLEED);
int c = s.consume(StatusEffect.BLEED, Integer.MAX_VALUE);   // 消耗全部
double m = plugin.getUpgradeMultiplier(player, getId());
double heal = Math.min(4 * m, p * 0.5);
player.setHealth(Math.min(player.getAttribute(Attribute.MAX_HEALTH).getValue(), player.getHealth() + heal));
```

- [ ] `la_manchaland_all_day_pass`（III，升級 P）：**保留**速度 I 被動；onAttack：若 `has(target, BLEED)` → `applyScaled(attacker, POISE, 2, 2, attacker)`
- [ ] `la_manchaland_standard_pass`（I，升級 P）：**保留**速度 I 被動；onAttack：若 `has(target, BLEED)` → `applyScaled(attacker, POISE, 1, 1, attacker)`
- [ ] `mask_of_the_parade`（IV，升級 P·N範圍）：**保留**潛行隱身；onAttack：`applyScaled(target, BLEED, 3, 3, attacker)`；onKill：模式 E 半徑 `5*m`，`apply(e, BLEED, 2, 2, killer)`
- [ ] `millarca`（II，升級 P·N吸血）：onAttack：`applyScaled(target, BLEED, 2, 2, attacker)`；若施加前 `has(target, BLEED)` → 吸血 `1*m` HP（同 crystallized_blood 的 setHealth 夾法）。**先判 has 再 applyScaled**（順序敏感）
- [ ] `sanguine_blossom_bolus`（II，升級 P）：**保留**非戰鬥回血被動；onAttack：`applyScaled(target, BLEED, 2, 2, attacker)`
- [ ] 文案雙語同步、`compileJava`、commit+push：`feat: 流血組 6 飾品接入屬性體系 / Wire 6 Bleed-group gifts into the status system`

---

### Task 4: 🌊 沉淪組（10 件）

**Files:** Modify `gift/gifts/{ArtisticSense,BlackSheetMusic,BrokenCompass,ColdIllusion,DistantStar,FrozenCries,MentalCorruptionBoostingGas,Rags,Rest,TangledBones}.java` + `en_US.yml`

- [ ] `artistic_sense`（IV，升級 P）：onAttack：`applyScaled(target, SINKING, 2, 2, attacker)`；施加前若 `has(target, SINKING) || plugin.sanity().isDepressed(target)` → `event.setDamage(×1.25)`
- [ ] `black_sheet_music`（IV，升級 P）：onAttack：`applyScaled(target, SINKING, 3, 3, attacker)`；施加前若 `plugin.sanity().isDepressed(target) || pot(target, SINKING) >= 4` → `event.setDamage(×1.25)`
- [ ] `broken_compass`（III，升級 %）：onAttack 模式 F：`Math.random() < Math.min(1.0, 0.25*m)` → `apply(target, SINKING, 2, 3, attacker)`
- [ ] `cold_illusion`（II，升級 P）：onAttack：`applyScaled(target, SINKING, 2, 2, attacker)` + `apply(target, BIND, 1, 2, attacker)`
- [ ] `distant_star`（III，升級 N(SAN)）：onAttack：若 `has(target, SINKING)` → `plugin.sanity().gainSan(attacker, (int)Math.round(1*m))` + `status().refresh(target, StatusEffect.SINKING, 1)`
- [ ] `frozen_cries`（II，升級 P）：onDamaged 模式 D：`applyScaled(atk, SINKING, 3, 2, victim)`
- [ ] `mental_corruption_boosting_gas`（II，升級 P）：onAttack：`applyScaled(target, SINKING, 2, 2, attacker)`；若 `target instanceof Player pv` → `plugin.sanity().dropSan(pv, 1)`
- [ ] `rags`（I，升級 N%）：onAttack：若 `has(target, SINKING)` → `event.setDamage(×(1 + Math.min(0.30, 0.075*m)))` + `plugin.sanity().gainSan(attacker, 1)`
- [ ] `rest`（III，升級 N%）：**保留**靜止回血被動；onAttack：若 `has(target, SINKING)` → `event.setDamage(×(1 + Math.min(0.30, 0.15*m)))`
- [ ] `tangled_bones`（II，升級 P）：onAttack：`applyScaled(target, SINKING, 2, 2, attacker)`；施加前若 `plugin.sanity().isDepressed(target)` → `event.setDamage(×1.15)`
- [ ] 文案雙語同步、`compileJava`、commit+push：`feat: 沉淪組 10 飾品接入屬性體系 / Wire 10 Sinking-group gifts into the status system`

---

### Task 5: 💥 破裂組（11 件，含束縛掛靠）

**Files:** Modify `gift/gifts/{DryToTheBoneBreast,EbonyBrooch,FlowerInTheMirror,Harestride,MoonInTheWater,Ruin,SmokingGunpowder,StrangeGlyphInscriptions,StrangeGlyphTalisman,Thunderbranch,ChiefButlersSecretArts}.java` + `en_US.yml`

- [ ] `dry_to_the_bone_breast`（III，count 固定 +2）：**保留**飽食被動；onAttack：若 `has(target, RUPTURE)` → `status().refresh(target, StatusEffect.RUPTURE, 2)`
- [ ] `ebony_brooch`（II，升級 P）：**保留**夜視；onAttack：`applyScaled(target, RUPTURE, 2, 2, attacker)`；15% 機率（不隨升級放大）追加 `apply(target, BIND, 1, 2, attacker)`
- [ ] `flower_in_the_mirror`（III，升級 P·N範圍）：onAttack：`applyScaled(target, RUPTURE, 2, 2, attacker)`；onPassiveTick + `gate(player,5000)`：對 `player.getLocation().getNearbyLivingEntities(5*m)` 中非玩家敵對目標 `apply(e, RUPTURE, 2, 1, player)`
- [ ] `harestride`（II，升級 P）：**保留**速度 II／跳躍被動；onAttack：若 `attacker.hasPotionEffect(PotionEffectType.SPEED)` → `applyScaled(target, RUPTURE, 2, 2, attacker)`
- [ ] `moon_in_the_water`（III，升級 P）：**保留**夜視；onAttack：若 `pot(target, RUPTURE) >= 3` → `applyScaled(attacker, POISE, 2, 1, attacker)`
- [ ] `ruin`（IV，升級 P）：onAttack：先記 `boolean already = has(target, RUPTURE)`；`applyScaled(target, RUPTURE, 3, 2, attacker)`；若 already → `apply(target, FRAGILE, 1, 1, attacker)` + 自身代價 `attacker.setHealth(Math.max(1.0, attacker.getHealth() - 0.5))`（不打死自己）
- [ ] `smoking_gunpowder`（II，升級 P）：onAttack：`applyScaled(target, RUPTURE, 2, 2, attacker)` + `apply(attacker, HASTE, 1, 2, attacker)`
- [ ] `strange_glyph_inscriptions`（III，升級 N%）：onAttack：若 `has(target, RUPTURE)` → `event.setDamage(×(1 + Math.min(0.30, 0.20*m)))` + `status().refresh(target, StatusEffect.RUPTURE, 1)`
- [ ] `strange_glyph_talisman`（II，升級 N範圍）：onKill：無條件對 `5*m` 格內非玩家敵人 `apply(e, RUPTURE, 3, 2, killer)`。（**不要**嘗試讀死者的 RUPTURE 狀態——EntityDeathEvent 時序上 StatusManager.onDeath 可能已清 states，讀取不可靠，故設計簡化為固定 p3·c2 擴散）效果文案寫「擊殺：對 5 格內敵人擴散破裂 3·2」
- [ ] `thunderbranch`（III，升級 %雷）：**保留** 10% 落雷（機率 ×m）；onAttack：`apply(target, RUPTURE, 2, 2, attacker)`；落雷命中分支追加 `apply(target, RUPTURE, 2, 1, attacker)`
- [ ] `chief_butlers_secret_arts`（III，升級 P）：onAttack：`applyScaled(target, BIND, 2, 2, attacker)`；onKill：回復 2 HP（setHealth 夾法）
- [ ] 文案雙語同步、`compileJava`、commit+push：`feat: 破裂組 11 飾品接入屬性體系 / Wire 11 Rupture-group gifts into the status system`

---

### Task 6: 🔨 震顫組（6 件）

**Files:** Modify `gift/gifts/{GreenSpirit,NixieDivergence,SourLiquorAroma,Sownpour,PieceOfCrumbledEgg,HandheldMirror}.java` + `en_US.yml`

- [ ] `green_spirit`（I，升級 P）：**移除**原右鍵藥水效果；onAttack：`applyScaled(target, TREMOR, 2, 2, attacker)`
- [ ] `nixie_divergence`（I，升級 P）：**移除**原隨機藥水；onAttack：`applyScaled(target, TREMOR, 2, 2, attacker)`
- [ ] `sour_liquor_aroma`（II，升級 N%）：onAttack：`apply(target, TREMOR, 2, 1, attacker)`；施加前若 `pot(target, TREMOR) >= 3` → `event.setDamage(×(1 + Math.min(0.30, 0.20*m)))`
- [ ] `sownpour`（IV，升級 %·P）：onAttack：`applyScaled(target, TREMOR, 3, 2, attacker)`；**保留** 30% 連鎖打擊（機率 ×m 封頂 1.0），連鎖分支對波及目標追加 `apply(e, TREMOR, 2, 1, attacker)`
- [ ] `piece_of_crumbled_egg`（II，升級 P）：**保留**死亡對殺手落雷；追加 `applyScaled(killer目標, TREMOR, 5, 3, deceased)`（p5 直接達爆發閾值；此處 killer 是 LivingEntity 檢查後施加）
- [ ] `handheld_mirror`（III，升級 P）：**取代**原 30% 反傷；onDamaged 模式 D：`applyScaled(atk, BIND, 2, 2, victim)` + `apply(atk, FRAGILE, 1, 2, victim)`
- [ ] 文案雙語同步、`compileJava`、commit+push：`feat: 震顫組 6 飾品接入屬性體系 / Wire 6 Tremor-group gifts into the status system`

---

### Task 7: 🎯 呼吸法組（7 件）

**Files:** Modify `gift/gifts/{CaskSpirits,ClearMirrorCalmWater,EmeraldElytra,Finifugality,Keenbranch,Nebulizer,CQCManual}.java` + `en_US.yml`

- [ ] `cask_spirits`（IV，升級 P）：onAttack：`applyScaled(attacker, POISE, 2, 2, attacker)`。「呼吸法爆擊觸發時 +1 SAN」無爆擊回呼 API——**簡化**：攻擊時若自身 `pot(attacker, POISE) >= 4` 額外 `gainSan(attacker, 1)`（醉拳語意近似），文案照此寫
- [ ] `clear_mirror_calm_water`（IV，升級 P）：**移除**原 20% 減傷；onAttack：`applyScaled(attacker, POISE, 3, 2, attacker)`；onKill：`applyScaled(killer, POWER, 3, 2, killer)`
- [ ] `emerald_elytra`（I，升級 P）：**保留**緩降被動；onAttack：若 `attacker.isSprinting()` → `applyScaled(attacker, POISE, 3, 2, attacker)`
- [ ] `finifugality`（III，升級 P）：onAttack：`applyScaled(attacker, POISE, 2, 2, attacker)`；若施加後 `pot(attacker, POISE) >= 5` → `applyScaled(attacker, POWER, 2, 1, attacker)`
- [ ] `keenbranch`（III，升級 %）：onAttack 模式 F：`0.20*m` 機率 → `event.setDamage(×1.30)` + `apply(attacker, POISE, 1, 1, attacker)`
- [ ] `nebulizer`（II，升級 P）：**取代**原中毒光環；onPassiveTick + `gate(player,5000)`：自身與 5 格內玩家 `applyScaled(p2, POISE, 2, 2, player)`
- [ ] `cqc_manual`（III，升級 P）：**取代**原力量 I 被動；onAttack（近戰事件本就限定）：`applyScaled(attacker, POISE, 2, 2, attacker)`
- [ ] 文案雙語同步、`compileJava`、commit+push：`feat: 呼吸法組 7 飾品接入屬性體系 / Wire 7 Poise-group gifts into the status system`

---

### Task 8: 💪 強壯／守護／迅捷輔助組（15 件）

**Files:** Modify `gift/gifts/{BloodyGadget,DreamingElectricSheep,DuelingManualBook3,IllusoryHunt,LateBloomersTattoo,Hardship,PhantomPain,TenacityBolus,TheBookOfVengeance,SpecialContract,PlumeOfProof,SpicebushBranch,Carmilla,ETypeDimensionalDagger,TraumaShield}.java` + `en_US.yml`

- [ ] `bloody_gadget`（I，升級 P）：**取代**原 +2 傷自損；onPassiveTick 模式 C：`applyScaled(player, POWER, 2, 2, player)`
- [ ] `dreaming_electric_sheep`（II，升級 P）：**保留**緩降、**移除**緩慢；onKill：`applyScaled(killer, POWER, 2, 3, killer)`
- [ ] `dueling_manual_book_3`（IV，升級 P·%）：onPassiveTick 模式 C：`applyScaled(player, POWER, 2, 2, player)`；onDamaged 模式 F：`0.25*m` 機率 → `apply(victim, HASTE, 2, 3, victim)`
- [ ] `illusory_hunt`（III，升級 %）：**取代**原 20% 失明；onAttack 模式 F：`0.20*m` → `apply(attacker, POWER, 2, 2, attacker)`
- [ ] `late_bloomers_tattoo`（II，升級 P）：onAttack：若 `attacker.getHealth() < attacker.getAttribute(Attribute.MAX_HEALTH).getValue() * 0.5` → `applyScaled(attacker, POWER, 2, 2, attacker)` + `apply(attacker, PROTECTION, 2, 2, attacker)`
- [ ] `hardship`（III，升級 P）：**取代**抗性被動；onKill：`gainSan(killer, 2)`；onAttack：若 `plugin.sanity().getSan(attacker) >= 40`（SAN上限45，「≥90%」→ 40）→ `applyScaled(attacker, POWER, 2, 1, attacker)`
- [ ] `phantom_pain`（III，升級 N%）：**取代** 25% 減傷；onAttack：`event.setDamage(×(1 + Math.min(0.30, 0.15*m)))`
- [ ] `tenacity_bolus`（II，升級 P）：**取代**抗性/生命被動；onDamaged：`applyScaled(victim, PROTECTION, 2, 2, victim)`
- [ ] `the_book_of_vengeance`（IV，升級 P）：onDamaged：`applyScaled(victim, POWER, 2, 2, victim)` + `apply(victim, PROTECTION, 1, 2, victim)`
- [ ] `special_contract`（III，升級 P）：**取代**力量+緩慢被動；onAttack：`applyScaled(target, FRAGILE, 2, 2, attacker)`
- [ ] `plume_of_proof`（II，升級 P）：**取代**擊殺速度；onAttack：`applyScaled(target, BIND, 1, 2, attacker)` + `apply(attacker, HASTE, 1, 2, attacker)`
- [ ] `spicebush_branch`（III，升級 P）：**保留**中毒轉回血；onPassiveTick 模式 C：`applyScaled(player, HASTE, 2, 3, player)`
- [ ] `carmilla`（II，升級 N%）：**移除**日光自損；onAttack：若 `target.getHealth() >= target.getAttribute(Attribute.MAX_HEALTH).getValue() - 0.01` → `event.setDamage(×(1 + Math.min(0.30, 0.20*m)))`
- [ ] `e_type_dimensional_dagger`（III，升級 %）：**保留** 25% 瞬移背刺（機率 ×m 封頂）；onAttack：`apply(attacker, CHARGE, 2, 2, attacker)`；背刺分支改為 `apply(attacker, CHARGE, 4, 2, attacker)`
- [ ] `trauma_shield`（II，升級 CD↓）：**保留** 60s CD 傷害吸收（CD 改 `(long)(60000/m)`，換用 `gate`，刪自有 cooldowns map 與 onQuit）；吸收觸發分支追加 `gainSan(victim, 2)`
- [ ] 文案雙語同步、`compileJava`、commit+push：`feat: 輔助組 15 飾品接入屬性體系 / Wire 15 support-group gifts into the status system`

---

### Task 9: 🕊 體驗／便利組（12 件）

**Files:** Modify `gift/gifts/{BlueZippoLighter,ChildWithinAFlask,GoldenUrn,Homeward,Lithograph,Oracle,Prejudice,PieceOfRelationship,RustyCommemorativeCoin,SomeonesDevice,Sunshower,TrialPlanGuide}.java` + `en_US.yml`

- [ ] `blue_zippo_lighter`（I，升級 %·CD↓）：onAttack 模式 F：`0.20*m` → `apply(target, BURN, 2, 2, attacker)`；onInteract：`gate(player, (long)(8000/m))` 通過時，右鍵實體 → `setFireTicks(100)`；右鍵方塊 → 其上方為 AIR 時 `setType(Material.FIRE)`（先讀現有類，若已有點火實作則沿用只加 gate）
- [ ] `child_within_a_flask`（II，升級 CD↓）：讀現有類；效果統一為 onAnyDamage 致命傷免死：傷害會歸零、`setHealth(4.0)`、抗性 IV 3 秒、擊退 3 格內敵人，CD `(long)(120000/m)` 用 gate
- [ ] `golden_urn`（II，升級 %）：onKill 模式 F：`0.15*m` → `event.getDrops()` 每項 `dropItemNaturally` 複製一份
- [ ] `homeward`（II，升級 N%）：自追戰鬥時間（onAttack/onDamaged 記 `lastCombat` map + onQuit 清）；onPassiveTick：脫戰 ≥5s 且本次脫戰未領過 → 回復 `MaxHP * Math.min(0.5, 0.20*m)`，記已領；再進戰重置
- [ ] `lithograph`（I，升級 N(HP)）：onKill：回復 `2*m` HP（夾上限）+ 飽食 `setFoodLevel(Math.min(20, +2))`
- [ ] `oracle`（II，升級 N範圍）：onPassiveTick：若 `player.isSneaking()` 且 `gate(player, 10_000)` → `12*m` 格內生物 `addPotionEffect(GLOWING, 60, 0)`
- [ ] `prejudice`（I，升級 N%）：onAttack：若目標血量比例 < 自身血量比例 → `event.setDamage(×(1 + Math.min(0.30, 0.15*m)))`
- [ ] `piece_of_relationship`（IV，升級 N範圍·%）：onPassiveTick：`16*m` 格內 ExperienceOrb `teleport` 至玩家；5 格內其他玩家 `addPotionEffect(REGENERATION, 120, 0)`；onKill：`event.setDroppedExp((int)(event.getDroppedExp() * 1.5))`
- [ ] `rusty_commemorative_coin`（III，升級 N斬殺線↑·P）：**取代**金粒掉落；onAttack：若目標 `getHealth()/MaxHP < Math.min(0.30, 0.15*m)` 且 `gate(attacker, 8000)` → `status().hurtTrue(target, attacker, target.getHealth() + 10, StatusEffect.RUPTURE)`（保證斬殺，標籤用 RUPTURE 顯示）；onKill：`applyScaled(killer, POWER, 2, 2, killer)`
- [ ] `someones_device`（III，升級 N範圍）：onPassiveTick：`6*m` 格內 Item/ExperienceOrb `teleport` 至玩家
- [ ] `sunshower`（III，升級 P）：onPassiveTick 模式 C：晴天 `apply(player, HASTE, 2, 2, player)`；雨天 `addPotionEffect(REGENERATION II, 120)`；雷暴 metadata 免閃電（讀現有類看是否已有）；onAttack：雨天時 `applyScaled(attacker, POWER, 2, 1, attacker)`
- [ ] `trial_plan_guide`（III，升級 N%）：onKill：`setDroppedExp(×1.5)`；onPassiveTick：`addPotionEffect(HERO_OF_THE_VILLAGE, 120, 0)`
- [ ] 文案雙語同步、`compileJava`、commit+push：`feat: 便利組 12 飾品接入屬性體系 / Wire 12 QoL-group gifts into the status system`

---

### Task 10: 🌸 原創補完組（5 件）

**Files:** Modify `gift/gifts/{EndlessHunger,FlowerMound,JinGangBolus,PieceOfATornSummer,TranquilLotusBolus}.java` + `en_US.yml`

- [ ] `endless_hunger`（III，升級 P）：**保留**飢餓不虛弱；onAttack：若 `attacker.getFoodLevel() >= 16` → `applyScaled(attacker, POWER, 2, 1, attacker)`
- [ ] `flower_mound`（II，升級 P）：**保留**再生 I；onKill：模式 E 半徑 5，`applyScaled(e, SINKING, 2, 2, killer)`
- [ ] `jin_gang_bolus`（II，升級 P）：**保留**吸收 I；onPassiveTick 模式 C：`applyScaled(player, PROTECTION, 2, 3, player)`
- [ ] `piece_of_a_torn_summer`（II，升級 P）：**保留**火焰傷觸發判定；觸發改為 `applyScaled(victim, POWER, 2, 2, victim)`
- [ ] `tranquil_lotus_bolus`（II，升級 P）：**移除**緩慢 I 副作用；onPassiveTick 模式 C：`applyScaled(player, PROTECTION, 2, 2, player)`；另 `gate` 10 秒節奏 `gainSan(player, 1)`——一個 gate 不夠用兩種週期，此類**自留一個獨立 timestamp map**（附 onQuit super 呼叫 + 自清）
- [ ] 文案雙語同步、`compileJava`、commit+push：`feat: 原創組 5 飾品接入屬性體系 / Wire 5 original-design gifts into the status system`

---

### Task 11: 全量文案覆核

- [ ] 逐一核對 `en_US.yml` 的 80 條 `gift.<id>.effect` 與新建構子中文 effect 語意一致（含「保留」段）
- [ ] `grep -c "effect:" en_US.yml` 條數不變（85 件含殘影/lunacy 相關條目）
- [ ] 檢查 Task 2–10 中所有 override `onQuit` 的類都呼叫了 `super.onQuit(player)`
- [ ] `compileJava` 通過；有改動則 commit+push：`docs: 飾品效果文案全量覆核 / Full sweep of gift effect texts`

### Task 12: README 更新 + 英文版

**Files:** Modify `README.md`；Create `README.en.md`

- [ ] `README.md` 頂部加語言切換：`繁體中文 | [English](README.en.md)`；更新「這是什麼」段（Phase 2 已完成：80 飾品接入 12 屬性）、指令表不變、更新紀錄加 1.1.0 條目（列九組重構摘要）
- [ ] 建 `README.en.md`：完整英譯（標題、指令表、屬性表、安裝遷移、資源包、更新紀錄、授權），頂部 `[繁體中文](README.md) | English`
- [ ] commit+push：`docs: README 1.1.0 與英文版 / README 1.1.0 update and English edition`

### Task 13: 版號、建置、部署、收尾

- [ ] `build.gradle.kts` `version = "1.1.0"`；`plugin.yml` `version: 1.1.0`；README 兩檔內 jar 檔名同步
- [ ] `Remove-Item build\libs\*.jar` → `.\gradlew.bat jar` → 確認產出 `LimbusEGO-1.1.0.jar`
- [ ] 部署：清 `D:\mcss_win-x86-64_v13.9.1\servers\LSMP Admin\plugins\LimbusEGO-*.jar` → 複製新 jar
- [ ] 手動冒煙清單（重啟測試伺服器後）：`/limbusego gift give <自己> bloody_gadget` 等各組抽 1 件，確認 ActionBar 出現屬性施加訊息、無 console error；`/limbusego reload` 正常
- [ ] `.superpowers/sdd/progress.md` 記 Phase 2 主線完成
- [ ] commit+push：`chore: v1.1.0 建置部署 / Build and deploy v1.1.0`
- [ ] 問用戶是否發 GitHub Release v1.1.0（repo 目前無任何 release）

## 驗證方式總覽

1. 每任務 `.\gradlew.bat compileJava -q` EXIT=0
2. Task 13 完整 jar 建置 + 測試伺服器部署
3. 冒煙測試：九組各抽一件飾品裝備實測，觀察 ActionBar 屬性訊息與 console
4. 已知偏離：無單元測試（專案無測試基建，Bukkit API 需 MockBukkit 才可單測，屬 Phase 3 以後的選項）
