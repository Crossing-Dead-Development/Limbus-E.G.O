package me.yisang.limbusego;

import org.bukkit.*;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LimbusEGO extends JavaPlugin implements Listener {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    private NamespacedKey ITEM_ID_KEY;

    private final Map<String, EGOWeapon> weaponModules = new HashMap<>();
    /** 以武器 PDC item id（getId()）為 key，供近戰事件單次查表，避免逐模組讀 ItemMeta。 */
    private final Map<String, EGOWeapon> weaponsByItemId = new HashMap<>();
    /** 特殊物品（彈藥/組合包）工廠：id → amount 建物品。give 指令與 tab-complete 共用。 */
    private final Map<String, java.util.function.IntFunction<ItemStack>> specialItems = new LinkedHashMap<>();
    /** 莊嚴哀悼系列的 give 類型。 */
    private static final Set<String> SOLEMN_TYPES = Set.of("black", "white", "butterflies", "shield");
    private final Map<UUID, Long> solemnCooldowns = new HashMap<>();
    private solemnlament solemn;
    private TiantuiStar tiantui;
    private TwilightWeapon twilight;
    private TibiaWeapon tibia;
    private SoundSuppressor soundSuppressor;
    private me.yisang.limbusego.status.StatusManager statusManager;
    private me.yisang.limbusego.status.SanityManager sanityManager;
    private me.yisang.limbusego.lang.LangManager lang;
    private me.yisang.limbusego.gift.GiftsModule gifts;

    private static final String PACK_URL_WEAPONS  = "https://github.com/Crossing-Dead-Development/Limbus-E.G.O-weapon-plugin-ResourcePack/releases/download/v.2.17/Limbus_E.G.O_Weapons_plugin_ResourcePack.v.2.17.zip";
    private static final String PACK_HASH_WEAPONS = "060302e85c12d23127b7c4eb3b7050c82615e20d";
    private static final String PACK_URL_GIFTS    = "https://github.com/Crossing-Dead-Development/Limbus_E.G.O_Gifts_plugin_ResourcePack/releases/download/v2.6/Limbus_E.G.O_Gifts_plugin_ResourcePack.v2.6.zip";
    private static final String PACK_HASH_GIFTS   = "91576ab33630f5f2869c3e30516824a4bf992999";

    /**
     * 同步兩份資源包（武器＋飾品）到本插件 data folder。
     * 不主動推送給玩家——交由外部 ResourcePackManager 合併分發。
     */
    private void syncResourcePacks() {
        syncPack(PACK_URL_WEAPONS, PACK_HASH_WEAPONS, "resourcepack-weapons.zip");
        syncPack(PACK_URL_GIFTS,   PACK_HASH_GIFTS,   "resourcepack-gifts.zip");
    }

    /**
     * 同步單一資源包到本插件 data folder。已存在且 SHA-1 與 hash 相符就跳過下載。
     */
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

    private static String sha1Of(java.io.File file) {
        try (java.io.InputStream in = new java.io.FileInputStream(file)) {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-1");
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) md.update(buf, 0, n);
            StringBuilder sb = new StringBuilder();
            for (byte b : md.digest()) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    // ── 公開工具方法 ────────────────────────────────────────────────────────────

    public NamespacedKey getItemIdKey() { return ITEM_ID_KEY; }
    public solemnlament getSolemn() { return solemn; }
    public TiantuiStar getTiantui() { return tiantui; }
    public TwilightWeapon getTwilight() { return twilight; }
    public TibiaWeapon getTibia() { return tibia; }
    public EGOWeapon getWeaponModule(String id) { return weaponModules.get(id); }
    public me.yisang.limbusego.status.StatusManager getStatusManager() { return statusManager; }
    public me.yisang.limbusego.status.SanityManager getSanityManager() { return sanityManager; }
    public me.yisang.limbusego.lang.LangManager getLang() { return lang; }
    public me.yisang.limbusego.gift.GiftsModule getGifts() { return gifts; }

    /** 讀語言 key 並轉換顏色代碼。 */
    public String msg(String key) {
        return translateHexColorCodes(lang.get(key));
    }

    /** 讀語言 key、代入佔位符後再轉顏色。 */
    public String msg(String key, Object... args) {
        return translateHexColorCodes(lang.get(key, args));
    }

    /** 讀 List 型 lore，逐行轉顏色。 */
    public java.util.List<String> msgList(String key) {
        java.util.List<String> raw = lang.getList(key);
        java.util.List<String> out = new java.util.ArrayList<>(raw.size());
        for (String line : raw) out.add(translateHexColorCodes(line));
        return out;
    }

    /** 讀取物品的 PDC item id；非本插件物品回傳 null。 */
    public String getItemId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        return item.getItemMeta()
                .getPersistentDataContainer()
                .get(ITEM_ID_KEY, PersistentDataType.STRING);
    }

    public boolean hasItemId(ItemStack item, String id) {
        return id.equals(getItemId(item));
    }

    // ── 初始化 ──────────────────────────────────────────────────────────────────

    @Override
    public void onEnable() {
        this.ITEM_ID_KEY = new NamespacedKey("limbusegoweapons", "item_id");
        this.lang = new me.yisang.limbusego.lang.LangManager(this);
        this.lang.load();

        this.solemn = new solemnlament(this);
        mimicry    m  = new mimicry(this);
        dacapo     d  = new dacapo(this);
        ringbrush  r  = new ringbrush(this);
        this.tiantui = new TiantuiStar(this);
        this.twilight = new TwilightWeapon(this);
        this.tibia = new TibiaWeapon(this);
        WCorpKnife wknife = new WCorpKnife(this);
        ShadowBladesinger blade = new ShadowBladesinger(this);

        weaponModules.put("mimicry", m);
        weaponModules.put("dacapo",  d);
        weaponModules.put("brush",   r);
        weaponModules.put("tiantui", tiantui);
        weaponModules.put("twilight", twilight);
        weaponModules.put("tibia", tibia);
        weaponModules.put("w_corp_knife", wknife);
        weaponModules.put("bladesinger", blade);

        for (EGOWeapon w : weaponModules.values()) weaponsByItemId.put(w.getId(), w);

        specialItems.put("tiger_mark",        tiantui::createTigerMark);
        specialItems.put("savage_tiger_mark", tiantui::createSavageTigerMark);
        specialItems.put("chatuhu",           tiantui::createChatuhuPack);
        specialItems.put("apocalypse_bird",   twilight::createApocalypseBirdPack);

        registerModule(m);
        registerModule(d);
        registerModule(r);
        registerModule(tiantui);
        registerModule(twilight);
        registerModule(tibia);
        registerModule(wknife);
        registerModule(blade);

        startShieldTick();

        // Limbus 屬性系統 + 理智值
        this.sanityManager = new me.yisang.limbusego.status.SanityManager(this);
        this.statusManager = new me.yisang.limbusego.status.StatusManager(this, sanityManager);
        this.sanityManager.start();
        this.statusManager.start();
        getServer().getPluginManager().registerEvents(statusManager, this);
        getServer().getPluginManager().registerEvents(new me.yisang.limbusego.status.SanityListener(sanityManager), this);

        getServer().getPluginManager().registerEvents(this, this);

        // 飾品模組（原 LimbusEGOGift 插件）
        this.gifts = new me.yisang.limbusego.gift.GiftsModule(this);
        this.gifts.enable();

        CommandRouter router = new CommandRouter(this);
        org.bukkit.command.PluginCommand root = getCommand("limbusego");
        if (root != null) { root.setExecutor(router); root.setTabCompleter(router); }

        // 同步資源包到本插件 data folder，供 ResourcePackManager 合併分發
        getServer().getScheduler().runTaskAsynchronously(this, this::syncResourcePacks);

        // 原版弓箭聲音攔截（需 ProtocolLib）
        if (getServer().getPluginManager().isPluginEnabled("ProtocolLib")) {
            try {
                soundSuppressor = new SoundSuppressor(this);
                soundSuppressor.register();
                getLogger().info("已啟用原版弓箭聲音攔截 (ProtocolLib)。");
            } catch (Throwable t) {
                soundSuppressor = null;
                getLogger().warning("ProtocolLib 聲音攔截初始化失敗：" + t.getMessage());
            }
        } else {
            getLogger().info("未偵測到 ProtocolLib，跳過原版弓箭聲音攔截。");
        }
    }

    private void registerModule(org.bukkit.event.Listener module) {
        getServer().getPluginManager().registerEvents(module, this);
    }

    @Override
    public void onDisable() {
        if (sanityManager != null) sanityManager.shutdown();
        if (gifts != null) gifts.disable();
    }

    // ── 聖宣盾牌 Tick ─────────────────────────────────────────────────────────

    private void startShieldTick() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                ItemStack offHand  = player.getInventory().getItemInOffHand();
                ItemStack mainHand = player.getInventory().getItemInMainHand();

                // 先做便宜的材質檢查，沒拿盾的玩家不必複製 ItemMeta
                if (offHand.getType() == Material.SHIELD && solemn.hasId(offHand, "solemn_shield")) {
                    solemn.handleShieldTick(player, offHand);
                } else if (mainHand.getType() == Material.SHIELD && solemn.hasId(mainHand, "solemn_shield")) {
                    solemn.handleShieldTick(player, mainHand);
                }
            }
        }, 0L, 5L);
    }

    // ── 莊嚴哀悼射擊（弩兩段式：右鍵上弦 → 再右鍵發射）──
    //
    // 由於底材為 CROSSBOW 並隱藏附魔「快速上弦 V」(QUICK_CHARGE 5)，上弦近乎瞬發。
    // - onWeaponInteract：右鍵時播自訂裝填音，並 mark soundSuppressor 抑制 vanilla 上弦音。
    // - onSolemnCrossbowLoad：確保上弦消耗的是蝴蝶彈藥（避免普通箭被吃進弩）。
    // - onSolemnBowShoot：攔截 vanilla 箭矢、清空弩的 chargedProjectiles，改發蝴蝶彈幕。

    // 右鍵：播放自訂裝填音、提早抑制 vanilla 上弦音。
    @EventHandler(priority = EventPriority.LOWEST)
    public void onWeaponInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null || !solemn.isSolemnLament(item)) return;

        // 無蝴蝶彈藥 → 不准上弦（含創造、含背包只有普通箭的情況）
        if (!solemn.hasButterflyQuartz(player)) {
            event.setCancelled(true);
            return;
        }

        // 提早 mark：涵蓋整個上弦階段，抑制 vanilla 弩的 loading_start/middle/end 音
        // 1500ms 足以涵蓋無附魔弩的 25 tick 上弦 + 緩衝
        if (soundSuppressor != null) soundSuppressor.mark(player, 1500L);

        int quickLevel = item.getEnchantmentLevel(org.bukkit.enchantments.Enchantment.QUICK_CHARGE);
        String loadSound = (quickLevel > 0)
                ? "solemnlament:solemn.quick_load." + Math.min(quickLevel, 3)
                : "solemnlament:solemn.load";
        player.getWorld().playSound(player.getLocation(), loadSound, 0.6f, 1.0f);
        // 不取消事件：讓 vanilla 弩正常進入上弦（舉手）動畫
    }

    // 發射：攔截原版箭矢、清空 chargedProjectiles 讓弩可再次上弦，改發蝴蝶彈幕。
    // 註：若玩家身上同時有蝴蝶與普通箭，vanilla 可能優先選普通箭上弦。
    // 玩家自行管理彈藥順序（建議蝴蝶放副手或 hotbar 第一格）。
    @EventHandler
    public void onSolemnBowShoot(org.bukkit.event.entity.EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        ItemStack bow = event.getBow();

        // 不是莊嚴哀悼：若用了蝴蝶彈藥則擋下（蝴蝶彈藥專屬莊嚴哀悼）
        if (bow == null || !solemn.isSolemnLament(bow)) {
            if (solemn.isButterfly(event.getConsumable())) event.setCancelled(true);
            return;
        }

        // 攔截：不射出原版箭矢、不損耐久
        event.setCancelled(true);

        // 清空弩的 chargedProjectiles（吃進去的蝴蝶在此消耗），讓弩可立即重新上弦
        if (bow.getItemMeta() instanceof org.bukkit.inventory.meta.CrossbowMeta cbMeta) {
            cbMeta.setChargedProjectiles(java.util.Collections.emptyList());
            bow.setItemMeta(cbMeta);
        }

        long now = System.currentTimeMillis();
        int quickLevel = bow.getEnchantmentLevel(org.bukkit.enchantments.Enchantment.QUICK_CHARGE);
        long cooldown = quickLevel > 0 ? Math.max(400L, 1200L - quickLevel * 300L) : 1200L;
        if (now - solemnCooldowns.getOrDefault(player.getUniqueId(), 0L) < cooldown) return;

        solemnCooldowns.put(player.getUniqueId(), now);

        // 抑制這次射擊在玩家附近的原版弓箭聲音
        if (soundSuppressor != null) soundSuppressor.mark(player);

        ItemMeta meta = bow.getItemMeta();
        String model = (meta != null && meta.getItemModel() != null)
                ? meta.getItemModel().toString() : "";
        solemn.handleShootManual(player, bow, model);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        solemnCooldowns.remove(event.getPlayer().getUniqueId());
    }

    // ── 近戰攻擊 ──────────────────────────────────────────────────────────────

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent event) {
        if (event.getDamager().hasMetadata("lsmp_custom_damage")) return;
        if (!(event.getDamager() instanceof Player player)) return;
        ItemStack item = player.getInventory().getItemInMainHand();

        // 讀一次 PDC id 直接查表，避免逐模組各複製一份 ItemMeta
        String id = getItemId(item);
        if (id == null) return;
        EGOWeapon ego = weaponsByItemId.get(id);
        if (ego != null) ego.handleMelee(event, player);
    }

    // ── 環指筆刷右鍵生物 ──────────────────────────────────────────────────────

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getHand() != org.bukkit.inventory.EquipmentSlot.HAND) return;
        Player player = event.getPlayer();
        if (!(event.getRightClicked() instanceof LivingEntity target)) return;

        ItemStack item = player.getInventory().getItemInMainHand();
        if (!hasItemId(item, "brush")) return;

        EGOWeapon ego = weaponModules.get("brush");
        if (ego instanceof ringbrush brush) {
            brush.handleInteractEntity(player, target);
        }
    }

    // ── 指令 ─────────────────────────────────────────────────────────────────

    @EventHandler
    public void onAdminGUIClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!(event.getInventory().getHolder() instanceof WeaponAdminGUI gui)) return;
        event.setCancelled(true);
        if (!gui.isItemSlot(event.getSlot())) return;
        ItemStack item = event.getCurrentItem();
        if (item != null && !item.getType().isAir()) {
            player.getInventory().addItem(item.clone());
            player.sendMessage(msg("msg.admin.given"));
        }
    }

    @EventHandler
    public void onCatalogGUIClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!(event.getInventory().getHolder() instanceof WeaponCatalogGUI gui)) return;
        event.setCancelled(true); // 唯讀
        int slot = event.getRawSlot();
        if (gui.isCloseSlot(slot)) { player.closeInventory(); return; }
        int tab = gui.getTabForSlot(slot);
        if (tab >= 0 && tab != gui.getCurrentTab()) gui.switchTab(player, tab);
    }

    /** /limbusego weapon <give|catalog|admin|id...> 分派。args[0] 為子指令。 */
    public void handleWeaponCommand(CommandSender sender, String[] args) {
        if (args.length == 0) { sender.sendMessage(msg("cmd.usage_root")); return; }
        String first = args[0].toLowerCase();

        if ("give".equals(first)) {
            if (!sender.hasPermission("limbus.admin") && !(sender instanceof org.bukkit.command.ConsoleCommandSender)) return;
            if (args.length < 3) { sender.sendMessage(msg("cmd.usage_give")); return; }
            Player target = Bukkit.getPlayerExact(args[1]);
            if (target == null) { sender.sendMessage(msg("cmd.player_not_found", args[1])); return; }
            String weaponId = args[2].toLowerCase();
            int amount = 1;
            if (args.length >= 4) {
                try { amount = Math.max(1, Integer.parseInt(args[3])); } catch (NumberFormatException ignored) {}
            }
            giveWeaponItem(target, weaponId, amount);
            return;
        }

        if (!(sender instanceof Player player)) return;
        if ("admin".equals(first)) {
            if (!player.hasPermission("limbus.admin") && !player.isOp()) return;
            player.openInventory(new WeaponAdminGUI(this).getInventory());
            return;
        }
        if ("catalog".equals(first)) {
            player.openInventory(new WeaponCatalogGUI(this, WeaponCatalogGUI.TAB_ALL).getInventory());
            return;
        }
        // 其餘子指令（直接給玩家自己物品）需要管理權限
        if (!player.hasPermission("limbus.admin") && !player.isOp()) return;
        giveWeaponItem(player, first, 1);
    }

    /** 依 id 給予武器/彈藥/莊嚴哀悼系列物品。give 指令的兩個分支共用。 */
    private boolean giveWeaponItem(Player target, String id, int amount) {
        java.util.function.IntFunction<ItemStack> factory = specialItems.get(id);
        if (factory != null) {
            target.getInventory().addItem(factory.apply(amount));
            return true;
        }
        EGOWeapon module = weaponModules.get(id);
        if (module != null) {
            for (int i = 0; i < amount; i++) module.give(target);
            return true;
        }
        if (SOLEMN_TYPES.contains(id)) {
            solemn.give(target, id, amount);
            return true;
        }
        return false;
    }

    /** 武器 give 可用 id（武器 + 特殊物品 + 莊嚴哀悼系列），供 Tab 補完。 */
    public java.util.List<String> getWeaponGiveIds() {
        java.util.List<String> ids = new java.util.ArrayList<>(weaponModules.keySet());
        ids.addAll(specialItems.keySet());
        ids.addAll(SOLEMN_TYPES);
        java.util.Collections.sort(ids);
        return ids;
    }

    // ── 顏色代碼工具 ─────────────────────────────────────────────────────────

    public String translateHexColorCodes(String message) {
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String hex = matcher.group(1);
            StringBuilder replacement = new StringBuilder("§x");
            for (char c : hex.toCharArray()) replacement.append('§').append(c);
            matcher.appendReplacement(sb, replacement.toString());
        }
        matcher.appendTail(sb);
        return ChatColor.translateAlternateColorCodes('&', sb.toString());
    }
}
