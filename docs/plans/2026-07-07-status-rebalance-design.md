# 狀態系統重平衡與傷害管線重做 — 設計文件

日期：2026-07-07
版本：v1.3.0
狀態：已實作

## 背景

前一輪外部修改（Gemini）把「所有數值除以十」當成平衡手段，同時手刻了一套護甲減傷計算。診斷結果：

1. **一律除十是錯的**：百分比乘區（+30% 傷害）、處決閾值（30% HP）、擊殺回血等無量綱數值也被除十，導致飾品廢化（單發燒傷總傷 0.4、處決閾值 3%、擊殺回血 0.1 顆心）。
2. **手刻 `applyResistances()` 造成雙重減傷**：先手動算一次護甲，`target.damage()` 又套一次原版護甲。
3. **交接文件（2026-07-05）Part 1 的領地掛人漏洞（A/B）完全沒修**，無敵根因（C/D）只被拖慢。

## 決策（已與作者確認）

| 決策點 | 選擇 |
|---|---|
| DoT 接軌護甲/抗性方式 | **走原版傷害管線** `damage(amount, source)`，刪手刻計算 |
| 致命度基準 | **未穿甲 20HP、中等致命度**：單發 proc 1~2 顆心；滿疊重堆 8~15 秒逐步致命 |
| 層數/級數 | **兩軸皆設上限**（potency 10 / count 20，config 可調） |
| 罪孽抗性系統 | **本輪只出企劃書**（見 `2026-07-07-sin-affinity-proposal.md`） |

## 實作內容

### 1. 傷害管線（修交接文件 Bug A + B + 雙重減傷）

- 刪除手刻 `applyResistances()`。
- `StatusManager.dealTrueDamage()` 改用 `target.damage(amount, source)`（來源在線時）；原版護甲、附魔、抗性藥水、領地/PvP 保護自動生效；DoT 擊殺正確歸屬。
- **遞迴防護重做**：舊碼把 meta 標在受害者、卻檢查攻擊者（帶來源後會失效）。改為結算前後把 `lce_status_true_dmg` meta 同時標在**兩端**，`StatusManager.onDamage` 與 `GiftsModule.onEntityDamageByEntity` 以 `StatusManager.isStatusDamage(event)` 統一跳過。
- **無敵幀**：結算前暫存並清零 `noDamageTicks`、結算後還原——proc 落在近戰 i-frames 內也實打實生效，且 DoT 不給目標免傷窗。
- `GiftsModule` 兩個傷害監聽加 `ignoreCancelled = true`；`onEntityDamageByEntity` 加 `finalDamage > 0` 閘。禁 PvP 領地內、零傷攻擊不再施加狀態。

### 2. 上限（修交接文件 Bug C + D）

- `StatusState.add(e, potency, count, potencyCap, countCap)` 疊加時夾住兩軸。
- config `status-caps`（default potency 10 / count 20，可逐效果覆寫）。
- PROTECTION / FRAGILE 改為**受擊消耗 1 count**，不再永久留存。金剛丸掛機無敵徹底根治（4%/potency × cap 10 = 減傷數學上限 40%）。

### 3. 數值表（20HP 基準）

| 效果 | 係數 | 未穿甲手感（cap 10） |
|---|---|---|
| BURN | potency × 0.5 /跳（每 2 秒） | 單發 2·2 ≈ 2 傷；滿疊 5/跳 |
| BLEED | potency × 0.5 /次攻擊 | 自懲，滿疊每刀 5 |
| SINKING | potency × 0.5 /受擊（憂鬱 ×1.5） | 附 -2%/potency 移速 |
| RUPTURE | potency × 0.75 /受擊 | 滿疊 7.5/擊 |
| TREMOR 爆發 | potency × 1.5（閾值 5） | 爆發 7.5~15 |
| POWER | +5%/potency（滿疊 +50%，出手耗層） | |
| CHARGE | +3%/potency（滿疊 +30%，出手耗層） | |
| FRAGILE | +5%/potency（滿疊 +50%，受擊耗層） | |
| PROTECTION | -4%/potency（上限 -40%，受擊耗層） | |
| POISE | 爆率 5%/potency 上限 50%，爆傷 ×1.5，出手耗層 | |

飾品自身的百分比乘區／閾值／回血全部**回滾到除十前的原值**（22 檔 + en_US.yml）。

### 4. 保留自前一輪的正確部分

- 施加冷卻：per-effect、config `status-cooldowns`（預設 1000ms）。
- PAPI 擴充 `%limbusego_san% / _effects / _<效果>_potency|count`（作者名修正為 YiSang）、softdepend、build 依賴。
- plugin.yml 版本 1.3.0。

## 驗證清單（部署測試伺服器後）

1. 領地（禁 PvP）內攻擊玩家：不疊任何狀態。
2. 裸身 vs 鐵甲吃燒傷：傷害差距 ≈ 一次原版護甲減免（無雙重減傷）。
3. 金剛丸掛機 5 分鐘：PROTECTION potency 停在 10、受擊會掉層。
4. DoT 擊殺歸屬：掉落物與擊殺訊息記在施術者名下。
5. PAPI：`/papi parse me %limbusego_effects%` 顯示正常。
