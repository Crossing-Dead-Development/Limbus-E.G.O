# 80 飾品效果重構設計表

日期：2026-07-02
範圍：`Limbus E.G.O Gift` 全部 84 個飾品中，排除 4 個殘影（faint / dark / twinkling / brilliant vestige）以外的 80 個。
目標：忠於 Limbus Company wiki 原作效果、串接武器插件 12 屬性機制（`StatusManager`）、重調 potency·count 數值、保留 `getUpgradeMultiplier` 升級倍率。

資料來源：[List of E.G.O Gifts — limbuscompany.wiki.gg](https://limbuscompany.wiki.gg/wiki/List_of_E.G.O_Gifts)（71 個命中原作；9 個為插件原創，標記〔原創〕並按 Limbus 風格補設計）。

---

## 一、轉譯約定（原作機制 → MC 機制）

| 原作概念 | MC 對應 |
|---|---|
| Offense Level Up / Damage Up | `POWER` 強壯（出手 +10%/p，每擊 -1c）|
| Defense Level Up / 減傷 | `PROTECTION` 守護（承傷 -5%/p）|
| Speed Up / Haste | `HASTE` 迅捷（藥水包裝）|
| Bind / Speed Down | `BIND` 束縛（藥水包裝）|
| Fragile | `FRAGILE` 易損（承傷 +15%/p）|
| Poise（爆擊） | `POISE` 呼吸法（爆擊率 +5%/p，×1.75）|
| Charge | `CHARGE` 充能（出手 +3%/p）|
| SP / 理智 | `SanityManager` SAN 值；SP<0＝憂鬱 |
| Stagger / 混亂 | `TREMOR` 震顫爆發（p≥5 受擊引爆 ×3 真傷）|
| Turn Start / Combat Start | `onPassiveTick`（每 5 秒脈衝）或首次進戰觸發 |
| On Hit / Clash | `onAttack` / `onDamaged` |
| 資源、Cost、商店等純 meta 效果 | 非戰鬥飾品 → 保留現有插件效果 |

**數值基準（依階級的 potency·count 預算）**

| 階級 | 每次觸發預算 | 定位 |
|---|---|---|
| I | p2·c1～c2，或 ≤20% 機率 | 入門單效 |
| II | p2～3·c2 | 單效穩定 |
| III | p3～4·c2～3，或條件加成 +15～20% | 條件強效 |
| IV | p3～5·c3+ 且帶第二段機制 | 體系核心 |

**升級倍率**：`m = getUpgradeMultiplier(...)`，各行標注作用點——`P`（potency 取整放大）、`%`（機率放大）、`N`（數值/範圍放大）。內建 CD 的效果倍率縮短 CD。

---

## 二、完整效果表（80 項，按屬性分組）

> 「原作效果」為 wiki 效果濃縮；〔原創〕表示 wiki 無此飾品，效果為配合體系的原創設計。
> 屬性縮寫：BLEED流血 BURN燒傷 SINKING沉淪 RUPTURE破裂 TREMOR震顫 POISE呼吸法 POWER強壯 PROT守護 FRAG易損 HASTE迅捷 BIND束縛 CHARGE充能。

### 🔥 燒傷 BURN（8）

| 飾品 / id | 階 | 原作效果（wiki） | 重構後效果（MC） | 鉤子 | 數值 | 升級 |
|---|---|---|---|---|---|---|
| 火光花 `ardent_flower` | III | 燒傷技能拼點強化；對低血燒傷目標 +50% 傷害 | 攻擊施加燒傷；目標 HP<30% 且燒傷中時 +30% 傷害。保留火焰免疫被動 | onAttack / onPassiveTick | BURN p2·c2 | P |
| 塵歸塵 `ashes_to_ashes` | I | 戰鬥開始時全體已燒傷敵人 +2 燒傷 | 攻擊「已燒傷」目標時追加燒傷（疊層器） | onAttack | BURN p2·c1 | P |
| 血炎刀 `bloodflame_sword` | II | 我方獲得 5 燒傷並回復 SP | 攻擊施加燒傷，並自身 +1 SAN | onAttack | BURN p3·c2 | P |
| 土歸土 `dust_to_dust` | III | 首回合對全體敵人 5 燒傷 +3 層 | 攻擊施加燒傷；擊殺時對 3 格內敵人擴散燒傷（塵爆） | onAttack / onKill | BURN p3·c2；擴散 p3·c2 | P·N(範圍) |
| 炎鱗 `glimpse_of_flames` | IV | 回合末消耗敵方燒傷層數造成爆發傷害並降防 | 攻擊燒傷中目標時「引爆」：消耗其全部燒傷 count，造成 p×c×0.5 真傷並施加易損（內建 8 秒 CD） | onAttack | 引爆上限 c5；FRAG p1·c2 | N(CD↓) |
| 火熱多汁枇杷腿 `hot_n_juicy_drumstick` | III | 施加燒傷時 +3 層（每回合 3 次） | 攻擊燒傷中目標時延長燒傷 count（`refresh`）。保留飽食度不流失 | onAttack / onPassiveTick | BURN c+2 | N(c) |
| 鬱火 `pain_of_stifled_rage` | II | 對燒傷目標再施加燒傷時獲得迅捷/攻級 | 攻擊施加燒傷；若目標已燒傷，改為自身獲得強壯 | onAttack | BURN p2·c2 / POWER p2·c1 | P |
| 蜂王漿香水 `royal_jelly_perfume` | III | +10 仇恨；受擊反施 3 燒傷；對燒傷敵減傷 15% | 受擊時對攻擊者施加燒傷；受「燒傷中敵人」的傷害 -15%。保留蜜蜂友好 | onDamaged | BURN p2·c2 | P |

### 🩸 流血 BLEED（6）

| 飾品 / id | 階 | 原作效果（wiki） | 重構後效果（MC） | 鉤子 | 數值 | 升級 |
|---|---|---|---|---|---|---|
| 血液結晶 `crystallized_blood` | II | 回合末將自身流血減半並轉為回血 | 每 5 秒：若自身有流血，消耗全部流血 count 並回復 potency×0.5 HP | onPassiveTick | 回復上限 4 HP/次 | N(上限) |
| 拉·曼查樂園自由通行證 `la_manchaland_all_day_pass` | III | 攻擊高流血目標時獲得呼吸法 | 攻擊「流血中」目標時獲得呼吸法。保留速度 I 被動 | onAttack | POISE p2·c2 | P |
| 拉·曼查樂園常規通行券 `la_manchaland_standard_pass` | I | 〔原創〕自由證下位版 | 攻擊「流血中」目標時獲得呼吸法（弱化版）。保留速度 I 被動 | onAttack | POISE p1·c1 | P |
| 遊行的面具 `mask_of_the_parade` | IV | 血鬼硬幣強化；新敵人登場即流血 | 攻擊施加流血；擊殺時對 5 格內敵人擴散流血（血之遊行）。保留潛行隱身 | onAttack / onKill | BLEED p3·c3；擴散 p2·c2 | P·N(範圍) |
| 蜜拉卡 `millarca` | II | 流血技能拼點 +1 | 攻擊施加流血；攻擊「流血中」目標時吸血回復 1 HP | onAttack | BLEED p2·c2 | P·N(吸血) |
| 血花丸 `sanguine_blossom_bolus` | II | 〔原創〕以血養花 | 攻擊施加流血；保留非戰鬥時緩慢回血 | onAttack / onPassiveTick | BLEED p2·c2 | P |

### 🌊 沉淪 SINKING（10）

| 飾品 / id | 階 | 原作效果（wiki） | 重構後效果（MC） | 鉤子 | 數值 | 升級 |
|---|---|---|---|---|---|---|
| 美感 `artistic_sense` | IV | 對 SP 受損敵人 +35% 傷害；沉淪技能再 +10% | 攻擊施加沉淪；對「沉淪中/憂鬱」目標 +25% 傷害 | onAttack | SINKING p2·c2 | P |
| 黑色樂譜 `black_sheet_music` | IV | 命中施加 5 沉淪；SP 越低傷害越高 | 攻擊施加沉淪；目標為憂鬱狀態（玩家）或沉淪 p≥4 時 +25% 傷害 | onAttack | SINKING p3·c3 | P |
| 破碎羅盤 `broken_compass` | III | 首回合對全體敵人隨機施加大量沉淪層數 | 攻擊時 25% 機率施加沉淪（高 count 版） | onAttack | SINKING p2·c3 | % |
| 冰冷的幻想 `cold_illusion` | II | 〔原創〕冰冷遲緩 | 攻擊施加沉淪＋束縛（冰緩） | onAttack | SINKING p2·c2＋BIND p1·c2 | P |
| 彼方之星 `distant_star` | III | 沉淪觸發時回 SP／追加沉淪層數 | 攻擊「沉淪中」目標時自身 +1 SAN 並延長其沉淪（`refresh` c+1） | onAttack | SAN+1；c+1 | N(SAN) |
| 冰封的哀號 `frozen_cries` | II | 最慢我方受擊/拼點勝時反施 3 沉淪 | 受擊時對攻擊者施加沉淪 | onDamaged | SINKING p3·c2 | P |
| 精神汙染加速氣體 `mental_corruption_boosting_gas` | II | 沉淪技能拼點後命中追加沉淪 | 攻擊施加沉淪；目標為玩家時額外 -1 SAN | onAttack | SINKING p2·c2 | P |
| 破布 `rags` | I | 沉淪隊伍拼點勝回 SP；低 SP 增傷 | 攻擊「沉淪中」目標時 +7.5% 傷害並自身 +1 SAN | onAttack | +7.5%；SAN+1 | N(%) |
| 安息 `rest` | III | 自身 SP 損失減少；對沉淪目標按正面硬幣增傷 | 攻擊「沉淪中」目標 +15% 傷害。保留靜止回血被動 | onAttack / onPassiveTick | +15% | N(%) |
| 破碎的骨片 `tangled_bones` | II | 沉淪體系引擎；對 SP<0 目標造成陰鬱傷害 | 攻擊施加沉淪；對「憂鬱」目標 +15% 傷害 | onAttack | SINKING p2·c2 | P |

### 💥 破裂 RUPTURE（11）

| 飾品 / id | 階 | 原作效果（wiki） | 重構後效果（MC） | 鉤子 | 數值 | 升級 |
|---|---|---|---|---|---|---|
| 乾巴柴澀雞胸肉 `dry_to_the_bone_breast` | III | 施加破裂時 +3 層（每回合 3 次） | 攻擊「破裂中」目標時延長破裂（`refresh` c+2）。保留飽食被動 | onAttack / onPassiveTick | c+2 | N(c) |
| 黑檀胸針 `ebony_brooch` | II | 開戰對全體 2 破裂＋束縛 | 攻擊施加破裂；15% 機率追加束縛。保留夜視 | onAttack | RUPTURE p2·c2；BIND p1·c2 | P |
| 鏡中花 `flower_in_the_mirror` | III | 每回合對全體敵人 3 破裂 +1 層 | 攻擊施加破裂；每 5 秒對 5 格內敵人施加破裂（鏡花瀰漫） | onAttack / onPassiveTick | p2·c2；光環 p2·c1 | P·N(範圍) |
| 卯足 `harestride` | II | 速度快於目標時追加破裂 | 攻擊時若自身有速度效果，施加破裂。保留速度 II／跳躍被動 | onAttack / onPassiveTick | RUPTURE p2·c2 | P |
| 水中月 `moon_in_the_water` | III | 攻擊高破裂目標獲得呼吸法；爆擊淨化 | 攻擊「破裂 p≥3」目標時獲得呼吸法。保留夜視 | onAttack | POISE p2·c1 | P |
| 破滅 `ruin` | IV | 破裂體系核心：施加時追加 potency/count、降攻防 | 攻擊施加破裂；若目標已破裂，追加易損（原自損 4 傷改為體系代價：自身 -0.5 HP） | onAttack | RUPTURE p3·c2；FRAG p1·c1 | P |
| 有煙火藥 `smoking_gunpowder` | II | 破裂攻擊後獲迅捷；速度高時增傷 | 攻擊施加破裂並獲得迅捷 | onAttack | RUPTURE p2·c2；HASTE p1·c2 | P |
| 篆刻的異文 `strange_glyph_inscriptions` | III | 對破裂目標拼點強化 | 攻擊「破裂中」目標 +20% 傷害並延長破裂（c+1） | onAttack | +20%；c+1 | N(%) |
| 異文符咒 `strange_glyph_talisman` | II | 擊殺破裂目標時將破裂擴散至全體敵人 | 擊殺「破裂中」目標時，對 5 格內敵人擴散其破裂（上限 p3·c2） | onKill | 擴散 ≤p3·c2 | N(範圍) |
| 雷擊木 `thunderbranch` | III | 施加破裂時 +1 potency +1 count | 攻擊施加破裂；保留 10% 落雷，落雷目標追加破裂 | onAttack | p2·c2；雷擊追加 p2·c1 | %(雷) |
| 首席管家的秘籍 `chief_butlers_secret_arts` | III | 對最慢敵人施加 5 束縛；拼點勝回復 | 攻擊施加束縛；擊殺回復 2 HP。（Bind 系掛靠破裂組） | onAttack / onKill | BIND p2·c2 | P |

### 🔨 震顫 TREMOR（6）

| 飾品 / id | 階 | 原作效果（wiki） | 重構後效果（MC） | 鉤子 | 數值 | 升級 |
|---|---|---|---|---|---|---|
| 綠光果實 `green_spirit` | I | 命中時隨機施加共 4 震顫 | 攻擊施加震顫（取代原右鍵藥水） | onAttack | TREMOR p2·c2 | P |
| 輝光變動儀 `nixie_divergence` | I | 開戰對全體 4 震顫 4 層 | 攻擊施加震顫（取代原隨機藥水） | onAttack | TREMOR p2·c2 | P |
| 酸味的酒香 `sour_liquor_aroma` | II | 震顫技能攻級 +2；對高震顫目標大幅增傷 | 攻擊施加震顫；對「震顫 p≥3」目標 +20% 傷害 | onAttack | TREMOR p2·c1；+20% | N(%) |
| 暴雨 `sownpour` | IV | 每回合對全體遞增震顫；回合末觸發引爆 | 攻擊施加震顫；保留 30% 連鎖打擊，連鎖同時對波及目標施加震顫（湊引爆） | onAttack | TREMOR p3·c2；連鎖 p2·c1 | %·P |
| 破碎之卵的殘片 `piece_of_crumbled_egg` | II | 〔原創〕雷震之卵 | 保留：死亡時對殺手落雷，並施加震顫（直接觸發爆發閾值） | onDeath | TREMOR p5·c3 | P |
| 手鏡 `handheld_mirror` | III | 對最慢兩敵施加 5 束縛＋降防；混亂連鎖 | 受擊時對攻擊者施加束縛＋易損（反制鏡像；取代 30% 反傷） | onDamaged | BIND p2·c2＋FRAG p1·c2 | P |

### 🎯 呼吸法 POISE（7）

| 飾品 / id | 階 | 原作效果（wiki） | 重構後效果（MC） | 鉤子 | 數值 | 升級 |
|---|---|---|---|---|---|---|
| 桶裝烈酒 `cask_spirits` | IV | 呼吸法體系核心：持續轉換 potency/count | 攻擊時獲得呼吸法；呼吸法爆擊觸發時自身 +1 SAN（醉拳越戰越勇） | onAttack | POISE p2·c2 | P |
| 明鏡止水 `clear_mirror_calm_water` | IV | 爆擊倍率 1.2→1.7；爆擊後獲攻級 | 攻擊時獲得呼吸法；擊殺時獲得強壯（斬鐵之心）。原 20% 減傷刪除 | onAttack / onKill | POISE p3·c2；POWER p3·c2 | P |
| 綠色鞘翅 `emerald_elytra` | I | 最快我方獲得 3 呼吸法 2 層 | 疾跑中攻擊時獲得呼吸法。保留緩降被動 | onAttack / onPassiveTick | POISE p3·c2 | P |
| 留戀 `finifugality` | III | 呼吸法總量達標時獲拼點/增傷 | 攻擊時獲得呼吸法；自身呼吸法 p≥5 時再獲強壯 | onAttack | POISE p2·c2；POWER p2·c1 | P |
| 磨尖的樹枝 `keenbranch` | III | 穿刺技能 +20% 傷害、拼點 +1 | 攻擊時 20% 機率 +30% 傷害，並獲得呼吸法 | onAttack | +30%；POISE p1·c1 | % |
| 霧化吸入器 `nebulizer` | II | 開戰全體我方獲得 4 呼吸法 4 層 | 每 5 秒自身與 5 格內玩家獲得呼吸法（取代原中毒光環） | onPassiveTick | POISE p2·c2 | P |
| 近身格鬥手冊 `cqc_manual` | III | 不耗彈藥技能強化；獲得呼吸法 | 空手或近戰攻擊時獲得呼吸法（取代原力量 I 被動） | onAttack | POISE p2·c2 | P |

### 💪 強壯／守護／迅捷 輔助系（15）

| 飾品 / id | 階 | 原作效果（wiki） | 重構後效果（MC） | 鉤子 | 數值 | 升級 |
|---|---|---|---|---|---|---|
| 鮮血裝飾 `bloody_gadget` | I | 每回合給隨機我方 2 增傷 | 每 5 秒獲得強壯（取代 +2 傷自損） | onPassiveTick | POWER p2·c2 | P |
| 夢中的電子羊 `dreaming_electric_sheep` | II | 斬擊擊殺後技能威力 +2 | 擊殺時獲得強壯。保留緩降（刪緩慢） | onKill / onPassiveTick | POWER p2·c3 | P |
| 決鬥教材第3冊 `dueling_manual_book_3` | IV | 穿刺拼點 +2；迴避成功獲增傷 | 每 5 秒獲得強壯；受擊時 25% 機率獲得迅捷（迴避成功） | onPassiveTick / onDamaged | POWER p2·c2；HASTE p2·c3 | P·% |
| 異想狩獵 `illusory_hunt` | III | 聚焦戰開始全體 +2 增傷 | 攻擊時 20% 機率獲得強壯（取代 20% 失明） | onAttack | POWER p2·c2 | % |
| 刺青：大器晚成 `late_bloomers_tattoo` | II | 單硬幣命中獲攻級＋防級（上限 5） | HP<50% 時攻擊獲得強壯＋守護（保留低血成長基調） | onAttack | POWER p2·c2＋PROT p2·c2 | P |
| 苦難 `hardship` | III | 拼點勝回 SP；高 SP 時獲攻級 | 擊殺 +2 SAN；SAN≥90 時攻擊獲得強壯（取代抗性被動） | onKill / onAttack | POWER p2·c1 | P |
| 幻痛 `phantom_pain` | III | 單體攻擊技能 +15% 傷害 | 近戰攻擊 +15% 傷害（取代 25% 減傷） | onAttack | +15% | N(%) |
| 強韌丸 `tenacity_bolus` | II | 最高血我方按受擊次數獲防級 | 受擊時獲得守護（取代抗性/生命被動） | onDamaged | PROT p2·c2 | P |
| 復仇帳簿 `the_book_of_vengeance` | IV | 鈍器拼點 +2；反擊獲護盾與增傷 | 受擊時獲得強壯＋守護（加倍奉還） | onDamaged | POWER p2·c2＋PROT p1·c2 | P |
| 特殊合約 `special_contract` | III | 混亂敵人受 33% 最大生命傷害＋易損 | 攻擊施加易損（取代力量+緩慢被動） | onAttack | FRAG p2·c2 | P |
| 證明的羽飾 `plume_of_proof` | II | 穿刺拼點勝施束縛；辛克雷獲迅捷 | 攻擊施加束縛並獲得迅捷（取代擊殺速度） | onAttack | BIND p1·c2；HASTE p1·c2 | P |
| 檀香梅枝 `spicebush_branch` | III | 三技能輪轉後獲迅捷＋威力 | 每 5 秒獲得迅捷。保留中毒轉回血 | onPassiveTick | HASTE p2·c3 | P |
| 卡蜜拉 `carmilla` | II | 開戰全體敵人受最大生命 20% 固傷 | 攻擊「滿血」目標時 +20% 傷害（初擊撕裂；刪日光自損） | onAttack | +20% | N(%) |
| E型次元短劍 `e_type_dimensional_dagger` | III | 充能技能拼點/增傷強化 | 攻擊時獲得充能；保留 25% 瞬移背刺，背刺後充能翻倍 | onAttack | CHARGE p2·c2（背刺 p4·c2） | % |
| 精神屏蔽力場 `trauma_shield` | II | 全體獲得 SP 傷害減免層數 | 保留 60 秒 CD 傷害吸收；新增：吸收觸發時免除本次 SAN 損失並 +2 SAN | onDamaged | SAN+2 | N(CD↓) |

### 🕊 體驗／便利系（12）

> 原作為純 meta 效果（資源、Cost、商店、等級）的飾品，翻譯成 MC 玩家有感的等價物：戰利品、經驗、拾取便利、保命、資訊。

| 飾品 / id | 階 | 原作效果（wiki） | 重構後效果（MC） | 鉤子 | 數值 | 升級 |
|---|---|---|---|---|---|---|
| 藍色Zippo牌打火機 `blue_zippo_lighter` | I | 每回合獲得 E.G.O 資源（meta） | 攻擊 20% 機率施加燒傷；右鍵點燃指向的方塊/生物（打火機本業，8 秒 CD） | onAttack / onInteract | BURN p2·c2 | %·N(CD↓) |
| 瓶中嬰孩 `child_within_a_flask` | II | 獲得 E.G.O 資源（meta） | 致命傷免死：回復至 4 HP＋抗性 IV 3 秒＋擊退周圍敵人（CD 2 分鐘） | onAnyDamage | 回復 4 HP | N(CD↓) |
| 金甕 `golden_urn` | II | 戰勝獲得 +20% Cost（meta） | 擊殺時 15% 機率複製該生物的全部掉落物（戰利品翻倍） | onKill | 15% | % |
| 歸途 `homeward` | II | 進戰全體回 12% HP | 脫戰 5 秒後一次性回復 20% 最大生命（每次脫戰一次） | onPassiveTick | 20% MaxHP | N(%) |
| 石板字符 `lithograph` | I | 敵人混亂時最低血我方回 5% HP | 擊殺時回復 2 HP＋2 飽食度（審判的餘裕） | onKill | 2 HP＋2 飽食 | N(HP) |
| 神諭 `oracle` | II | 回合末轉換 E.G.O 資源（meta） | 潛行時主動觸發：12 格內生物發光 3 秒（CD 10 秒） | onPassiveTick | 12 格 | N(範圍) |
| 偏見 `prejudice` | I | 開戰最低血我方回 15% HP | 對 HP 比例低於自己的目標 +15% 傷害（欺凌弱者） | onAttack | +15% | N(%) |
| 緣分殘片 `piece_of_relationship` | IV | 取得時全員 +5 等級（meta） | 經驗球 16 格磁吸＋擊殺經驗 +50%；5 格內隊友共享再生 I | onPassiveTick / onKill | 16 格；+50% | N(範圍·%) |
| 生鏽的紀念幣 `rusty_commemorative_coin` | III | 單硬幣技能未殺敵時重擲；擊殺獲威力 | 對 HP<15% 的目標直接處決（真傷斬殺，每 8 秒一次）；擊殺時獲得強壯 | onAttack / onKill | 斬殺線 15%；POWER p2·c2 | N(斬殺線↑)·P |
| 某人的裝置 `someones_device` | III | 索引螺絲釘體系（meta） | 6 格內掉落物與經驗球自動吸向玩家（磁鐵裝置） | onPassiveTick | 6 格 | N(範圍) |
| 狐雨 `sunshower` | III | 怠惰共鳴時全體 +2 威力 | 天氣雙祝福：晴天迅捷常駐；雨天再生 II＋攻擊獲強壯；雷暴免疫閃電 | onPassiveTick / onAttack | HASTE p2／POWER p2·c1 | P |
| 試用規劃指南 `trial_plan_guide` | III | 商店技能替換 -30%（meta） | 擊殺經驗 +50%＋村民英雄 I 常駐（交易折扣） | onKill / onPassiveTick | +50% | N(%) |

### 🌸 原創補完（5）

| 飾品 / id | 階 | 設計說明 | 重構後效果（MC） | 鉤子 | 數值 | 升級 |
|---|---|---|---|---|---|---|
| 無盡的飢餓 `endless_hunger` | III | 〔原創〕飽食即力量 | 保留飢餓不虛弱；飽食度 ≥16 時攻擊獲得強壯 | onAttack / onPassiveTick | POWER p2·c1 | P |
| 花塚 `flower_mound` | II | 〔原創〕葬花之悲 | 保留再生 I；擊殺時對 5 格內敵人施加沉淪 | onPassiveTick / onKill | SINKING p2·c2 | P |
| 金剛丸 `jin_gang_bolus` | II | 〔原創〕金剛不壞 | 每 5 秒獲得守護。保留吸收 I | onPassiveTick | PROT p2·c3 | P |
| 破碎之夏的殘片 `piece_of_a_torn_summer` | II | 〔原創〕夏之灼痕 | 保留：受火焰傷時觸發；改為獲得強壯（原力量藥水→體系化） | onAnyDamage | POWER p2·c2 | P |
| 靜蓮丸 `tranquil_lotus_bolus` | II | 〔原創〕靜心蓮華 | 每 5 秒獲得守護，並緩慢回復 SAN（+1/10s）。刪緩慢 I 副作用 | onPassiveTick | PROT p2·c2；SAN+1 | P |
| 血花丸（見流血組）／常規通行券（見流血組）／冰冷的幻想（見沉淪組）／破碎之卵（見震顫組） | — | 已列於對應屬性組 | — | — | — | — |

---

## 三、統計與平衡總覽

**屬性覆蓋**（80 項中涉及各屬性的飾品數）：

| 屬性 | 施加/獲得來源數 | 備註 |
|---|---|---|
| BURN | 9 | 施加 7、引爆 1、反制 1 |
| BLEED | 5 | 施加 4、轉化 1 |
| SINKING | 11 | 施加 8、增傷聯動 3 |
| RUPTURE | 10 | 施加 7、聯動 3 |
| TREMOR | 5 | 施加 4、爆發 1 |
| POISE | 9 | 全部自身獲得 |
| POWER | 13 | 自身獲得 |
| PROTECTION | 7 | 自身/隊友獲得 |
| HASTE / BIND | 4 / 6 | 藥水包裝 |
| CHARGE | 1 | E型次元短劍獨佔（W社體系保留給武器） |
| FRAGILE | 4 | 稀有 debuff，僅 III/IV 級 |
| 體驗／便利系 | 12 | 原作 meta 效果 → 戰利品/經驗/拾取/保命/資訊 QoL |

**設計原則備忘**
1. 「施加系」飾品給 count 較多（疊層），「聯動系」飾品吃已有屬性（增傷/延長），兩類搭配才成體系——單獨一顆不會過強。
2. IV 級全部帶第二段機制（引爆、擴散、雙屬性），且引爆類含內建 CD 防刷。
3. 增傷百分比封頂 +30%；potency 單次施加封頂 p3（IV 級擴散除外）；與武器插件本身的施加量疊加時由 StatusManager 的既有上限與衰減自然約束。
4. 升級倍率統一不放大 count 超過 +2，避免 DoT 無限疊層。
5. SAN 相關效果需通過武器插件 `LimbusEGOWeapons#getStatusManager()` / `SanityManager` 橋接，`plugin.yml` 需加 `softdepend: [LimbusEGOWeapons]`。

---

## 四、後續實作備註（非本表範圍）

- 飾品插件需新增對武器插件的 API 橋接類（soft-depend，缺插件時降級為原版效果或 no-op）。
- `refresh`／`triggerBleed` 等既有 API 已支援「延長」與「強制引爆」語意，引爆類飾品直接複用。
- lang 檔（`zh_TW.yml` / `en_US.yml`）80 條 effect 文案需同步改寫。
