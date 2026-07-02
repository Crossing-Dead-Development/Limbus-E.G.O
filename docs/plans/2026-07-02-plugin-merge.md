# Limbus-E.G.O 插件整合實作計畫（Phase 1：行為不變的合併）

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 把 `Limbus E.G.O Weapons`（含 12 屬性體系）與 `Limbus E.G.O Gift` 合併為單一 Paper 插件 `LimbusEGO-1.0.0.jar`，行為與舊版完全一致，舊物品／玩家資料無痛相容。

**Architecture:** 以武器插件主類為基底改名為 `LimbusEGO`（保持 JavaPlugin 身分），飾品插件主類降級為 `GiftsModule`（普通類，透過建構子持有主插件並委派 JavaPlugin 方法）。所有 `NamespacedKey` 改為顯式舊 namespace 字串，確保 PDC 相容。

**Tech Stack:** Java 21、Paper API 1.21.4-R0.1-SNAPSHOT、ProtocolLib 5.3.0（softdepend）、Gradle 9.5（wrapper 從舊專案複製）。

## Global Constraints

- 新插件名 `LimbusEGO`、版本 `1.0.0`、主類 `me.yisang.limbusego.LimbusEGO`
- **PDC 相容鐵律**：武器側所有 key 用 namespace 字串 `"limbusegoweapons"`、飾品側用 `"limbusegogift"`（`new NamespacedKey(String, String)`，不用 `(Plugin, String)` 建構子）
- 本 Phase **不改任何遊戲行為**——飾品效果改寫屬 Phase 2、資源包合併屬 Phase 3
- 兩個 lang 體系分流：jar 內 `lang/weapons/*.yml` 與 `lang/gifts/*.yml`
- 專案無單元測試基礎設施：每個 task 的驗證＝`.\gradlew.bat jar` 編譯通過；最終驗證＝部署測試伺服器跑煙霧清單
- 舊專案路徑：`C:\Users\User\IdeaProjects\Limbus E.G.O Weapons`、`C:\Users\User\IdeaProjects\Limbus E.G.O Gift`；新專案：`C:\Users\User\IdeaProjects\Limbus-E.G.O`
- 測試伺服器 plugins：`D:\mcss_win-x86-64_v13.9.1\servers\LSMP Admin\plugins`

---

### Task 1: Gradle 專案骨架

**Files:**
- Create: `build.gradle.kts`、`settings.gradle.kts`、`.gitignore`、`src/main/resources/plugin.yml`
- Copy: `gradlew.bat`、`gradlew`、`gradle/wrapper/*`（從武器專案）

**Interfaces:**
- Produces: 可編譯的空專案；plugin.yml 宣告全部 7 個指令，後續 task 的主類與模組向它註冊

- [ ] **Step 1: 複製 Gradle wrapper 並建立建置檔**

```powershell
cd "C:\Users\User\IdeaProjects\Limbus-E.G.O"
Copy-Item "C:\Users\User\IdeaProjects\Limbus E.G.O Weapons\gradlew.bat","C:\Users\User\IdeaProjects\Limbus E.G.O Weapons\gradlew" .
Copy-Item -Recurse "C:\Users\User\IdeaProjects\Limbus E.G.O Weapons\gradle" .
```

`settings.gradle.kts`：
```kotlin
rootProject.name = "LimbusEGO"
```

`build.gradle.kts`（兩專案依賴聯集）：
```kotlin
plugins {
    id("java")
}
group = "me.yisang"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.dmulloy2.net/repository/public/")
}

dependencies {
    compileOnly("io.papermc.paper", "paper-api", "1.21.4-R0.1-SNAPSHOT")
    compileOnly("com.comphenix.protocol:ProtocolLib:5.3.0")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(21)
    }
}
```

`.gitignore`：
```
.gradle/
build/
.idea/
*.iml
```

- [ ] **Step 2: 建立合併 plugin.yml**

`src/main/resources/plugin.yml`（兩邊指令聯集，描述照抄舊檔）：
```yaml
name: LimbusEGO
version: 1.0.0
main: me.yisang.limbusego.LimbusEGO
description: Limbus Company E.G.O weapons & gifts unified plugin. Face the sin, Save the E.G.O.
api-version: "1.21.4"
softdepend: [ProtocolLib]
commands:
  getego:
    description: "E.G.O weapons: give / catalog / language / reload"
    usage: /getego <give|catalog|reload|language|...>
  accessories:
    description: 開啟飾品欄位 / open accessory slots
    usage: /accessories
  getgift:
    description: 取得飾品 / grant an accessory
    usage: /getgift <id> [amount]
    permission: limbus.admin
  gachachest:
    description: 設定或移除抽取箱 / register or remove a gacha chest
    usage: /gachachest <set|remove>
    permission: limbus.admin
  threadchest:
    description: 設定或移除自訂抽獎箱（支援紡錘/狂氣貨幣）
    usage: /threadchest <set <cost> [thread|lunacy] <name...>|remove>
    permission: limbus.admin
  shopchest:
    description: 設定或移除購買商店箱
    usage: /shopchest <set <cost> [thread|lunacy] <display name...>|remove>
    permission: limbus.admin
  egogift:
    description: 飾品系統指令 / accessory system commands
    usage: /egogift <category|reload|language>
```

- [ ] **Step 3: 驗證空專案可建置**

Run: `cd "C:\Users\User\IdeaProjects\Limbus-E.G.O"; .\gradlew.bat jar`
Expected: `BUILD SUCCESSFUL`（無原始碼也應成功產出含 plugin.yml 的 jar）

- [ ] **Step 4: Commit**

```powershell
git add -A; git commit -m "feat: Gradle 骨架與合併 plugin.yml"
```

---

### Task 2: 搬移武器側原始碼（主類改名 LimbusEGO）

**Files:**
- Create: `src/main/java/me/yisang/limbusego/**`（整棵武器樹：主類＋8 武器＋status/＋lang/＋GUI）
- Create: `src/main/resources/lang/weapons/zh_TW.yml`、`lang/weapons/en_US.yml`、`config.yml`

**Interfaces:**
- Produces: `me.yisang.limbusego.LimbusEGO extends JavaPlugin`，公開 `getStatusManager()`、`getSanityManager()`、`getLang()`、`getItemIdKey()`、`translateHexColorCodes(String)`——Task 4 的 GiftsModule 依賴 `LimbusEGO` 型別

- [ ] **Step 1: 複製原始碼與資源並做套件改名**

```powershell
cd "C:\Users\User\IdeaProjects\Limbus-E.G.O"
New-Item -ItemType Directory -Force src\main\java\me\yisang\limbusego | Out-Null
Copy-Item -Recurse "C:\Users\User\IdeaProjects\Limbus E.G.O Weapons\src\main\java\me\yisang\limbus\*" src\main\java\me\yisang\limbusego\
# 套件與類名機械改寫（\b 保證不誤傷 limbusegogift）
Get-ChildItem -Recurse src\main\java -Filter *.java | ForEach-Object {
  $c = Get-Content $_.FullName -Raw
  $c = $c -replace 'me\.yisang\.limbus\b', 'me.yisang.limbusego'
  $c = $c -replace 'LimbusEGOWeapons', 'LimbusEGO'
  Set-Content $_.FullName $c -NoNewline
}
Rename-Item src\main\java\me\yisang\limbusego\LimbusEGOWeapons.java LimbusEGO.java
# 資源：lang 移到 weapons 子資料夾
New-Item -ItemType Directory -Force src\main\resources\lang\weapons | Out-Null
Copy-Item "C:\Users\User\IdeaProjects\Limbus E.G.O Weapons\src\main\resources\lang\*" src\main\resources\lang\weapons\
Copy-Item "C:\Users\User\IdeaProjects\Limbus E.G.O Weapons\src\main\resources\config.yml" src\main\resources\
```

- [ ] **Step 2: 調整武器側 LangManager 的 lang 路徑**

`src/main/java/me/yisang/limbusego/lang/LangManager.java`：把所有 `"lang"` 資料夾與 `"lang/"` 資源路徑字串改為 `"lang/weapons"` 與 `"lang/weapons/"`。涉及三處模式（實作時以 grep `"lang` 找全）：
```java
// getAvailableLangs()
File langDir = new File(plugin.getDataFolder(), "lang/weapons");
// saveDefaultConfigAndLangFiles() 與 loadLang() 中的
plugin.saveResource("lang/weapons/" + code + ".yml", false);
new File(plugin.getDataFolder(), "lang/weapons/" + code + ".yml");
plugin.getResource("lang/weapons/" + code + ".yml");
```

- [ ] **Step 3: 驗證編譯**

Run: `.\gradlew.bat jar`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: Commit**

```powershell
git add -A; git commit -m "feat: 搬移武器側原始碼，主類改名 LimbusEGO"
```

---

### Task 3: 武器側 NamespacedKey 顯式 namespace（PDC 相容）

**Files:**
- Modify: `LimbusEGO.java`、`mimicry.java`、`ShadowBladesinger.java`、`TiantuiStar.java`、`TibiaWeapon.java`、`TwilightWeapon.java`、`WCorpKnife.java`、`status/SanityManager.java`、`status/StatusManager.java`

**Interfaces:**
- Produces: 所有武器側 PDC/AttributeModifier key 落在 `limbusegoweapons:` namespace，與舊插件產生的物品、玩家屬性 modifier 完全相容

- [ ] **Step 1: 逐一改寫 19 個呼叫點**

改寫規則：`new NamespacedKey(this, X)` / `new NamespacedKey(plugin, X)` → `new NamespacedKey("limbusegoweapons", X)`。完整清單（行號為舊檔位置，實作時以 grep 定位）：

| 檔案 | key |
|---|---|
| `LimbusEGO.java` | `item_id` |
| `mimicry.java` | `mimicry_dmg`、`mimicry_spd` |
| `ShadowBladesinger.java` | `bladesinger_dmg`、`bladesinger_spd` |
| `TiantuiStar.java` | `tiantui_dmg`、`tiantui_spd` |
| `TibiaWeapon.java` | `tibia_dmg`、`tibia_spd`、`tibia_reach` |
| `TwilightWeapon.java` | `twilight_dmg`、`twilight_spd`、`twilight_reach` |
| `WCorpKnife.java` | `wcorp_dmg`、`wcorp_spd` |
| `status/SanityManager.java` | `atkModKey`、`spdModKey`（常數 ATK_MOD_KEY/SPD_MOD_KEY） |
| `status/StatusManager.java` | `sinking_speed`（SINKING_SPEED_MOD_KEY） |

範例（StatusManager.java:84）：
```java
this.sinkingSpeedKey = new NamespacedKey("limbusegoweapons", SINKING_SPEED_MOD_KEY);
```

- [ ] **Step 2: 驗證無遺漏**

Run: `Select-String -Path src\main\java\me\yisang\limbusego\*.java,src\main\java\me\yisang\limbusego\status\*.java -Pattern 'new NamespacedKey\((this|plugin),'`
Expected: 無輸出（gift/ 子樹此時尚不存在）

- [ ] **Step 3: 編譯**

Run: `.\gradlew.bat jar`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: Commit**

```powershell
git add -A; git commit -m "fix: 武器側 NamespacedKey 固定為 limbusegoweapons namespace"
```

---

### Task 4: 搬移飾品側原始碼（LimbusEGOGift → GiftsModule）

**Files:**
- Create: `src/main/java/me/yisang/limbusego/gift/**`（GiftsModule＋Accessory 基礎＋GUI＋Gacha＋gifts/ 84 個飾品＋gift 側 lang/LangManager）
- Create: `src/main/resources/lang/gifts/zh_TW.yml`、`lang/gifts/en_US.yml`

**Interfaces:**
- Consumes: `LimbusEGO`（Task 2）——JavaPlugin 委派來源
- Produces: `GiftsModule(LimbusEGO plugin)` 普通類，公開 `enable()`、`disable()`、`getPlugin()`，其餘公開方法（`getUpgradeMultiplier`、`msg`、`color`、`getAccessory`…）簽名與舊 LimbusEGOGift 完全一致

- [ ] **Step 1: 複製與機械改名**

```powershell
cd "C:\Users\User\IdeaProjects\Limbus-E.G.O"
New-Item -ItemType Directory -Force src\main\java\me\yisang\limbusego\gift | Out-Null
Copy-Item -Recurse "C:\Users\User\IdeaProjects\Limbus E.G.O Gift\src\main\java\me\yisang\limbusegogift\*" src\main\java\me\yisang\limbusego\gift\
Get-ChildItem -Recurse src\main\java\me\yisang\limbusego\gift -Filter *.java | ForEach-Object {
  $c = Get-Content $_.FullName -Raw
  $c = $c -replace 'me\.yisang\.limbusegogift', 'me.yisang.limbusego.gift'
  $c = $c -replace 'LimbusEGOGift', 'GiftsModule'
  Set-Content $_.FullName $c -NoNewline
}
Rename-Item src\main\java\me\yisang\limbusego\gift\LimbusEGOGift.java GiftsModule.java
New-Item -ItemType Directory -Force src\main\resources\lang\gifts | Out-Null
Copy-Item "C:\Users\User\IdeaProjects\Limbus E.G.O Gift\src\main\resources\lang\*" src\main\resources\lang\gifts\
```

- [ ] **Step 2: GiftsModule 降級為普通類**

`GiftsModule.java` 頂部改為：
```java
public class GiftsModule implements Listener, TabCompleter {

    private final LimbusEGO plugin;

    public GiftsModule(me.yisang.limbusego.LimbusEGO plugin) {
        this.plugin = plugin;
    }

    /** 供需要 org.bukkit.plugin.Plugin 參數的呼叫點使用。 */
    public me.yisang.limbusego.LimbusEGO getPlugin() { return plugin; }

    // ── JavaPlugin 委派（保持舊呼叫碼不變）─────────────────────
    public java.util.logging.Logger getLogger() { return plugin.getLogger(); }
    public java.io.File getDataFolder() { return plugin.getDataFolder(); }
    public org.bukkit.configuration.file.FileConfiguration getConfig() { return plugin.getConfig(); }
    public void reloadConfig() { plugin.reloadConfig(); }
    public void saveConfig() { plugin.saveConfig(); }
    public void saveResource(String path, boolean replace) { plugin.saveResource(path, replace); }
    public java.io.InputStream getResource(String path) { return plugin.getResource(path); }
    public org.bukkit.Server getServer() { return plugin.getServer(); }
    public org.bukkit.command.PluginCommand getCommand(String name) { return plugin.getCommand(name); }
```

並且：
1. `@Override public void onEnable()` → `public void enable()`；`onDisable()` → `public void disable()`（拿掉 @Override）
2. 移除 `syncResourcePackToDataFolder`、`sha1Of`、`PACK_URL/PACK_HASH/PACK_FILENAME` 與 enable 內的資源包同步呼叫（Task 6 由主類統一處理雙資源包）
3. `/egogift reload` 子指令中 `pm.disablePlugin(this); pm.enablePlugin(this)` 整段改為 `lang.reload(); sender.sendMessage(msg("msg.reload.done"));`（模組無法自我重載插件）

- [ ] **Step 3: 編譯錯誤驅動修復 Plugin 參數呼叫點**

Run: `.\gradlew.bat jar 2>&1 | Select-String "error"`

凡是把 `this`（GiftsModule 內）或 `plugin`（各 gift/GUI/manager 類內）當 `org.bukkit.plugin.Plugin` 參數傳遞的呼叫點都會編譯失敗——逐一改為 `getPlugin()` / `plugin.getPlugin()`。已知模式：
```java
// GiftsModule 內
getServer().getPluginManager().registerEvents(gachaListener, getPlugin());
getServer().getPluginManager().registerEvents(this, getPlugin());
Bukkit.getScheduler().runTaskTimer(getPlugin(), ...);          // startPassiveTick
// 各飾品/管理器內（plugin 型別是 GiftsModule）
Bukkit.getScheduler().runTask(plugin.getPlugin(), () -> ...);
new FixedMetadataValue(plugin.getPlugin(), true)
```
反覆執行編譯直到通過。

- [ ] **Step 4: gift 側 LangManager 路徑分流**

`src/main/java/me/yisang/limbusego/gift/lang/LangManager.java`：同 Task 2 Step 2 的做法，把 `"lang"` → `"lang/gifts"`、`"lang/"` → `"lang/gifts/"`。

- [ ] **Step 5: 編譯**

Run: `.\gradlew.bat jar`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 6: Commit**

```powershell
git add -A; git commit -m "feat: 搬移飾品側，LimbusEGOGift 降級為 GiftsModule"
```

---

### Task 5: 飾品側 NamespacedKey 顯式 namespace

**Files:**
- Modify: `gift/GiftsModule.java`、`gift/GachaListener.java`

**Interfaces:**
- Produces: 飾品 PDC（物品 id、選單、欄位、玩家升級等級、貨幣）全部落在 `limbusegogift:` namespace

- [ ] **Step 1: 改寫 6 個呼叫點**

| 檔案 | key | 改寫後 |
|---|---|---|
| `GiftsModule.java` | `accessory_id` | `new NamespacedKey("limbusegogift", "accessory_id")` |
| `GiftsModule.java` | `menu_opener` | `new NamespacedKey("limbusegogift", "menu_opener")` |
| `GiftsModule.java` | `slot_0..4` | `new NamespacedKey("limbusegogift", "slot_" + i)` |
| `GiftsModule.java` | `upgrade_<id>` | `new NamespacedKey("limbusegogift", "upgrade_" + id)` |
| `GachaListener.java` | `lunacy` | `new NamespacedKey("limbusegogift", "lunacy")` |
| `GachaListener.java` | `thread` | `new NamespacedKey("limbusegogift", "thread")` |

- [ ] **Step 2: 全專案驗證無殘留**

Run: `Get-ChildItem -Recurse src -Filter *.java | Select-String 'new NamespacedKey\((this|plugin|getPlugin\(\)|plugin\.getPlugin\(\)),'`
Expected: 無輸出

- [ ] **Step 3: 編譯 + Commit**

Run: `.\gradlew.bat jar` → `BUILD SUCCESSFUL`
```powershell
git add -A; git commit -m "fix: 飾品側 NamespacedKey 固定為 limbusegogift namespace"
```

---

### Task 6: 主類接線（GiftsModule 啟動＋指令＋雙資源包）

**Files:**
- Modify: `LimbusEGO.java`、`src/main/resources/config.yml`

**Interfaces:**
- Consumes: `GiftsModule.enable()/disable()`（Task 4）
- Produces: 完整可運作的合併插件

- [ ] **Step 1: LimbusEGO 掛載 GiftsModule**

`LimbusEGO.java` 新增欄位與 getter：
```java
private me.yisang.limbusego.gift.GiftsModule gifts;
public me.yisang.limbusego.gift.GiftsModule getGifts() { return gifts; }
```
`onEnable()` 末尾（資源包同步之前）加：
```java
// 飾品模組（原 LimbusEGOGift 插件）
this.gifts = new me.yisang.limbusego.gift.GiftsModule(this);
this.gifts.enable();
```
`onDisable()` 加：
```java
if (gifts != null) gifts.disable();
```
註：GiftsModule.enable() 內原本的 getCommand 註冊碼經 Task 4 委派後直接生效，指令不需在主類重複註冊。

- [ ] **Step 2: 雙資源包同步**

`LimbusEGO.java` 的資源包常數改為兩組（URL/HASH 照抄兩個舊主類現值）：
```java
private static final String PACK_URL_WEAPONS  = "https://github.com/Crossing-Dead-Development/Limbus-E.G.O-weapon-plugin-ResourcePack/releases/download/v.2.17/Limbus_E.G.O_Weapons_plugin_ResourcePack.v.2.17.zip";
private static final String PACK_HASH_WEAPONS = "060302e85c12d23127b7c4eb3b7050c82615e20d";
private static final String PACK_URL_GIFTS    = "https://github.com/Crossing-Dead-Development/Limbus_E.G.O_Gifts_plugin_ResourcePack/releases/download/v2.6/Limbus_E.G.O_Gifts_plugin_ResourcePack.v2.6.zip";
private static final String PACK_HASH_GIFTS   = "91576ab33630f5f2869c3e30516824a4bf992999";
```
`syncResourcePackToDataFolder()` 改為參數化並呼叫兩次：
```java
private void syncResourcePacks() {
    syncPack(PACK_URL_WEAPONS, PACK_HASH_WEAPONS, "resourcepack-weapons.zip");
    syncPack(PACK_URL_GIFTS,   PACK_HASH_GIFTS,   "resourcepack-gifts.zip");
}

private void syncPack(String url, String hash, String filename) {
    getDataFolder().mkdirs();
    java.io.File dest = new java.io.File(getDataFolder(), filename);
    if (dest.isFile() && hash.equalsIgnoreCase(sha1Of(dest))) {
        getLogger().info("[ResourcePack] " + filename + " 已存在且 hash 相符，跳過下載。");
        return;
    }
    getLogger().info("[ResourcePack] 下載 " + url + " → " + dest.getAbsolutePath());
    try (java.io.InputStream in = java.net.URI.create(url).toURL().openStream();
         java.io.FileOutputStream out = new java.io.FileOutputStream(dest)) {
        in.transferTo(out);
        String got = sha1Of(dest);
        if (!hash.equalsIgnoreCase(got)) {
            getLogger().warning("[ResourcePack] " + filename + " hash 不符（預期 " + hash + "，實際 " + got + "）。");
        } else {
            getLogger().info("[ResourcePack] " + filename + " 下載完成，hash 一致。");
        }
    } catch (Exception e) {
        getLogger().severe("[ResourcePack] " + filename + " 下載失敗：" + e.getMessage());
    }
}
```
onEnable 中的非同步呼叫改為 `this::syncResourcePacks`。

- [ ] **Step 3: config.yml 合併確認**

比對兩舊專案 `config.yml`（實作時 `Compare-Object (Get-Content A) (Get-Content B)`）。兩者目前只含 `language` 設定即可直接共用單一檔；若 gift 側 config 有額外 key（gacha 相關），把缺的 key 追加進新 `config.yml`。

- [ ] **Step 4: 編譯 + Commit**

Run: `.\gradlew.bat jar` → `BUILD SUCCESSFUL`
```powershell
git add -A; git commit -m "feat: 主類接線 GiftsModule 與雙資源包同步"
```

---

### Task 7: 部署測試伺服器與煙霧測試

**Files:**
- 無程式碼變更；伺服器操作

**Interfaces:**
- Consumes: `build/libs/LimbusEGO-1.0.0.jar`

- [ ] **Step 1: 部署（移除舊插件、遷移資料）**

```powershell
$srv = "D:\mcss_win-x86-64_v13.9.1\servers\LSMP Admin\plugins"
# 舊 jar 移出（保留備份）
New-Item -ItemType Directory -Force "$srv\_disabled" | Out-Null
Move-Item "$srv\LimbusEGOWeapons-*.jar","$srv\LimbusEGOGift-*.jar" "$srv\_disabled\" -ErrorAction SilentlyContinue
# 舊資料夾遷移到新 data folder（gacha 箱、語言設定等持久化檔）
New-Item -ItemType Directory -Force "$srv\LimbusEGO" | Out-Null
if (Test-Path "$srv\LimbusEGOGift")    { Copy-Item "$srv\LimbusEGOGift\*.yml" "$srv\LimbusEGO\" -ErrorAction SilentlyContinue }
if (Test-Path "$srv\LimbusEGOWeapons") { Copy-Item "$srv\LimbusEGOWeapons\config.yml" "$srv\LimbusEGO\" -Force -ErrorAction SilentlyContinue }
Copy-Item "build\libs\LimbusEGO-1.0.0.jar" $srv -Force
```
註：實作時先 `Get-ChildItem "$srv\LimbusEGOGift"` 確認 gacha/thread/shop 管理器實際存了哪些檔名，全部搬過去。

- [ ] **Step 2: 啟動伺服器煙霧測試（手動清單）**

| # | 測項 | 預期 |
|---|---|---|
| 1 | 伺服器啟動 log | `LimbusEGO 1.0.0` 啟用、無 stacktrace、兩個資源包下載/驗證訊息 |
| 2 | `/getego give <me> mimicry` 攻擊怪 | 武器效果與屬性 ActionBar 正常 |
| 3 | **舊背包裡的武器**攻擊 | 仍被識別（PDC 相容驗證） |
| 4 | `/accessories` 開選單、裝備飾品 | 選單正常、被動生效 |
| 5 | **舊飾品物品**放入欄位 | 仍被識別、**升級等級保留**（玩家 PDC） |
| 6 | 抽取箱／紡錘箱／商店箱 | 遷移資料後照常運作 |
| 7 | `/egogift reload`、`/getego reload` | 語言重載無錯誤 |
| 8 | `/getego language en_US` | 兩體系文案各自切換正常 |

- [ ] **Step 3: 修復發現的問題並 commit**

每修一項：重編譯 → 重部署 → 重測該項。

```powershell
git add -A; git commit -m "fix: 煙霧測試修正"
```

---

### Task 8: README 與推送

**Files:**
- Create: `README.md`

- [ ] **Step 1: 撰寫 README**

內容必含：插件簡介（合併自兩舊插件）、版本 1.0.0、指令表（7 指令）、12 屬性體系簡表、安裝說明（取代舊兩插件＋資料遷移步驟）、舊 repo 連結與棄用聲明、資源包說明（Phase 3 前仍用兩個舊資源包）。

- [ ] **Step 2: Commit + push**

```powershell
git add -A; git commit -m "docs: README v1.0.0"; git push
```

---

## Phase 2/3 預告（另立計畫）

- **Phase 2**：按 `docs/specs/2026-07-02-80-gifts-rework-design.md` 改寫 80 個飾品（屆時飾品直接呼叫 `plugin.getStatusManager()`）
- **Phase 3**：合併資源包 → 新 repo `Limbus-E.G.O-ResourcePack`，本地 `D:\找不到自己ㄉ電腦\MC\LSMP\Github`（未壓縮資料夾無版本號＋發佈 zip 有版本號），插件改單一 PACK_URL/HASH
- 完成後：舊 4 個 repo archive；README 更新流程按 feedback-limbus-ego-workflow 執行
