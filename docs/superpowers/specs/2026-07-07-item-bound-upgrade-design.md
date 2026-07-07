# 飾品升級綁定物品 + Lore 隨動 設計文件

日期：2026-07-07
狀態：已核可

## 目標

升級等級從「玩家 PDC（`upgrade_<id>`）」改為綁定在飾品 ItemStack 本身，
且物品描述（lore）隨升級等級變動（附加等級標示行，含效果倍率）。

## 資料模型

| 位置 | Key | 型別 | 內容 |
|---|---|---|---|
| 物品 PDC | `limbusegogift:item_upgrade_level` | INTEGER | 該飾品自身升級等級 0–3 |
| 玩家 PDC | `limbusegogift:slot_<i>_level` | INTEGER | 第 i 格裝備中飾品的等級（與 `slot_<i>` ID 並存）|

舊玩家 PDC key `upgrade_<id>` 廢除，由一次性遷移清除。

## 資料流

- **裝備（saveEquipped）**：讀 GUI 格子物品 PDC `upgrade_level` → 寫入 `slot_<i>_level`。
- **卸下 / 重造（loadEquipped）**：以 `buildLeveledItem(id, level)` 重造物品——
  `createItem()` 後把等級寫入物品 PDC 並附加 lore 等級行。物品拖出選單後等級跟著物品走。
- **升級（選單內殘影點擊）**：以 `buildLeveledItem` 重建該格物品（新等級），同步殘影消耗；
  後續 `saveEquipped()` 把新等級寫入 slot level。
- **效果倍率（getUpgradeMultiplier / applyScaled）**：簽名不變；實作改為讀裝備格
  `slot_<i>_level`（同 ID 多格取最大值），經 per-player 快取避免熱路徑重複讀 PDC。
  40+ 飾品效果檔零改動。

## Lore 等級行

新 lang 鍵 `msg.upgrade_lore_mult`（`{0}`=等級、`{1}`=倍率），等級 > 0 時附加於 lore 末尾。
用新鍵而非改舊 `msg.upgrade_lore`，讓伺服器上舊 lang 檔透過 jar 內建回退自動取得。
抽卡、管理員 GUI、`give` 產物皆為 0 級（無此行）。描述本文與語言檔其餘內容不動。

## 遷移（一次性，PlayerJoin）

1. 掃描玩家 PDC 中 namespace=`limbusegogift`、key 前綴 `upgrade_` 的舊鍵。
2. 舊等級若對應目前裝備格的 ID → 轉寫到 `slot_<i>_level`。
3. 移除全部舊鍵；已升級但未裝備的 ID 等級作廢（核可決策）。

在 PlayerJoin 執行而非開選單時，避免老玩家開選單前效果倍率被砍回 0 級。

## 邊界情況

- 殘影不可升級、不帶等級行。
- 背包中同 ID 未升級物品不繼承等級（綁物品語義）。
- `AccessoryMenu.withUpgradeLore` 移除，統一走 `buildLeveledItem`。

## 驗證

編譯通過；測試服實測：升級 → 拖出選單 lore 保留、轉交他人等級跟物品、
重登裝備格等級保留、老玩家舊資料遷移正確。
