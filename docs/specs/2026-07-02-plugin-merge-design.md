# Limbus-E.G.O 插件整合設計（草案）

日期：2026-07-02
目標：將 `Limbus E.G.O Weapons` 與 `Limbus E.G.O Gift` 兩個 Paper 插件整合為單一插件 **Limbus-E.G.O**，讓 80 個重構飾品直接接上 12 屬性體系（見 `2026-07-02-80-gifts-rework-design.md`）。

## 架構決策（待用戶確認）

**方案：單一插件、單一 Gradle 專案、單一 jar（`LimbusEGO-<version>.jar`）**

- 飾品直接呼叫 `StatusManager` / `SanityManager`，不需 soft-depend 橋接（原設計表第四節的橋接方案作廢）
- 伺服器只裝一個 jar，取代舊的兩個插件
- 備選方案（多模組單 jar／多 jar）已評估，維護成本高於效益，除非用戶另有考量

## 套件佈局

```
me.yisang.limbusego
├── LimbusEGO.java          ← 唯一主類（合併兩邊 onEnable/資源包推送/指令註冊）
├── status/                 ← 原 me.yisang.limbus.status 原樣搬入（體系核心）
├── weapon/                 ← 原武器類（dacapo, mimicry, ringbrush, …）
├── gift/                   ← 80 個飾品（依重構設計表改寫）+ Accessory/BaseAccessory
├── gui/                    ← WeaponAdminGUI + GiftAdminGUI + Catalog 類
├── gacha/                  ← GachaChestManager / GachaListener（原 Gift 插件）
└── lang/                   ← LangManager 合併（weapons + gifts 的 lang key 併成一份）
```

## 合併要點

| 項目 | 處理 |
|---|---|
| 主類 | 兩個 onEnable 合併；指令（/accessories /getgift /egogift /gachachest…＋武器側指令）全數保留 |
| 資源包 | MC 1.21.4 伺服器單資源包限制 → 兩個資源包合併為一個，**另開獨立 repo `Limbus-E.G.O-ResourcePack`**（沿用舊模式：zip 上傳 Release、直連下載）；assets/weapons + assets/gifts 命名空間互不衝突可直接合併；插件內單一 PACK_URL/HASH |
| lang | zh_TW / en_US 各自合併，key 前綴已天然分流（`status.*`、`gift.*`、武器 key） |
| config.yml | 兩份合併，衝突 key 重新命名 |
| PDC key | 沿用各自的 NamespacedKey 字串，舊物品無痛相容（需驗證兩邊 plugin name 變更後 NamespacedKey namespace 是否受影響 → 建構時顯式用舊 namespace 字串） |
| 版本號 | 重置為 1.0.0 |
| 舊 repo | 完成遷移後 archive（Weapons / Gifts 兩個插件 repo；資源包 repo 併為一個新 repo） |
| Fabric 版 | `LimbusEGOWeapons-Fabric` 暫不動，之後另行同步 |

## 更新工作流程（用戶指定）

每次推送更新時，除了編譯部署到測試伺服器（`D:\mcss_win-x86-64_v13.9.1\servers\LSMP Admin\plugins`），**必須同步更新 README**（版本號、更新內容、飾品/武器清單變動），再 commit/push。

## 風險與待驗證

1. **舊物品相容**：現有玩家背包裡的武器/飾品 PDC tag 與 item-model namespace 必須繼續被新插件識別（遷移測試必做）
2. **資源包合併**：pack.mcmeta 版本一致性、兩包若有同名檔案需清點
3. **StatusManager 事件優先度**：飾品的 onAttack 修改傷害需跑在屬性乘區之前（維持 NORMAL → HIGH 順序）

## 待用戶決定

- [ ] 架構方案確認（單一插件？）
- [ ] GitHub repo 可見性（public / private）
- [ ] 舊 repo 是否 archive、資源包 repo 是否也合併
