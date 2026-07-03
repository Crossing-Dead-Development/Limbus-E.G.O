package me.yisang.limbusego.gift;

import me.yisang.limbusego.gift.gifts.*;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GiftsModule implements Listener {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    private final me.yisang.limbusego.LimbusEGO plugin;

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

    private NamespacedKey ITEM_ID_KEY;
    private NamespacedKey MENU_OPENER_KEY;
    private NamespacedKey[] SLOT_KEYS;

    private final Map<String, Accessory> accessories = new LinkedHashMap<>();
    // 裝備中的飾品快取：每次傷害/互動事件與被動 tick 都會查，
    // 只在選單關閉（saveEquipped）與玩家退出時失效，避免反覆讀 5 個 PDC key。
    private final Map<UUID, List<Accessory>> equippedCache = new HashMap<>();
    // 升級等級的 PDC key 快取（NamespacedKey 建構含字串驗證，攻擊事件熱路徑會頻繁用到）
    private final Map<String, NamespacedKey> upgradeKeys = new HashMap<>();

    private GachaChestManager gachaChestManager;
    private ThreadChestManager threadChestManager;
    private ShopChestManager shopChestManager;
    private GachaListener gachaListener;
    private me.yisang.limbusego.gift.lang.LangManager lang;

    // ── 屬性系統委派（合併後直接取用武器側管理器；僅限事件鉤子內呼叫）──────
    public me.yisang.limbusego.status.StatusManager status() { return plugin.getStatusManager(); }
    public me.yisang.limbusego.status.SanityManager sanity() { return plugin.getSanityManager(); }

    // ── 語言 ────────────────────────────────────────────────────────────────
    public me.yisang.limbusego.gift.lang.LangManager getLang() { return lang; }

    /** 從語言檔取值並轉換顏色代碼。 */
    public String msg(String key) { return color(lang.get(key)); }
    public String msg(String key, Object... args) { return color(lang.get(key, args)); }

    // ── 階級表 ──────────────────────────────────────────────────────────────

    private static final Map<String, Integer> TIER_MAP = new HashMap<>();
    private static final Set<String> VESTIGE_IDS = new HashSet<>();

    static {
        // ─ Tier I (9) ─
        for (String id : new String[]{
            "rags", "ashes_to_ashes", "lithograph",
            "emerald_elytra", "plume_of_proof", "blue_zippo_lighter",
            // 新飾品
            "nixie_divergence", "prejudice", "bloody_gadget"
        }) TIER_MAP.put(id, 1);

        // ─ Tier II (23) ─
        for (String id : new String[]{
            "harestride", "strange_glyph_talisman", "frozen_cries",
            "crystallized_blood", "ebony_brooch", "tangled_bones",
            "tenacity_bolus", "oracle", "dreaming_electric_sheep",
            "trauma_shield", "homeward", "pain_of_stifled_rage",
            "golden_urn", "smoking_gunpowder", "cqc_manual",
            "mental_corruption_boosting_gas",
            // 新飾品
            "carmilla", "child_within_a_flask", "green_spirit",
            "sanguine_blossom_bolus", "late_bloomers_tattoo",
            "e_type_dimensional_dagger", "bloodflame_sword"
        }) TIER_MAP.put(id, 2);

        // ─ Tier III (27) ─
        for (String id : new String[]{
            "dust_to_dust", "phantom_pain", "moon_in_the_water",
            "ardent_flower", "hot_n_juicy_drumstick", "dry_to_the_bone_breast",
            "distant_star", "broken_compass", "illusory_hunt",
            "keenbranch", "chief_butlers_secret_arts", "finifugality",
            "trial_plan_guide", "sour_liquor_aroma", "spicebush_branch",
            "hardship", "rest", "la_manchaland_all_day_pass",
            "la_manchaland_standard_pass",
            // 新飾品
            "nebulizer", "strange_glyph_inscriptions", "rusty_commemorative_coin",
            "someones_device", "special_contract", "flower_in_the_mirror",
            "sunshower", "thunderbranch"
        }) TIER_MAP.put(id, 3);

        // ─ Tier IV (21) ─
        for (String id : new String[]{
            "clear_mirror_calm_water", "flower_mound", "piece_of_crumbled_egg",
            "piece_of_a_torn_summer", "mask_of_the_parade", "black_sheet_music",
            "cold_illusion", "ruin", "jin_gang_bolus",
            "piece_of_relationship", "tranquil_lotus_bolus", "cask_spirits",
            "the_book_of_vengeance", "dueling_manual_book_3",
            // 新飾品
            "endless_hunger", "royal_jelly_perfume", "millarca",
            "artistic_sense", "handheld_mirror", "glimpse_of_flames", "sownpour"
        }) TIER_MAP.put(id, 4);

        // ─ 殘影 ─
        VESTIGE_IDS.addAll(List.of(
            "dark_vestige", "faint_vestige", "twinkling_vestige", "brilliant_vestige"
        ));
    }

    // ── 體系分組表（80 飾品重構設計表的九組；圖鑑「依體系排序」用）──────────

    /** 九個體系的 key，順序即圖鑑分頁順序；lang 鍵為 gui.group_<key>。 */
    public static final String[] GROUP_KEYS = {
        "burn", "bleed", "sinking", "rupture", "tremor",
        "poise", "support", "qol", "original"
    };

    private static final Map<String, Integer> GROUP_MAP = new HashMap<>();

    static {
        String[][] groups = {
            // 燒傷
            {"ardent_flower", "ashes_to_ashes", "bloodflame_sword", "dust_to_dust",
             "glimpse_of_flames", "hot_n_juicy_drumstick", "pain_of_stifled_rage", "royal_jelly_perfume"},
            // 流血
            {"crystallized_blood", "la_manchaland_all_day_pass", "la_manchaland_standard_pass",
             "mask_of_the_parade", "millarca", "sanguine_blossom_bolus"},
            // 沉淪
            {"artistic_sense", "black_sheet_music", "broken_compass", "cold_illusion", "distant_star",
             "frozen_cries", "mental_corruption_boosting_gas", "rags", "rest", "tangled_bones"},
            // 破裂（含束縛掛靠）
            {"dry_to_the_bone_breast", "ebony_brooch", "flower_in_the_mirror", "harestride",
             "moon_in_the_water", "ruin", "smoking_gunpowder", "strange_glyph_inscriptions",
             "strange_glyph_talisman", "thunderbranch", "chief_butlers_secret_arts"},
            // 震顫
            {"green_spirit", "nixie_divergence", "sour_liquor_aroma", "sownpour",
             "piece_of_crumbled_egg", "handheld_mirror"},
            // 呼吸法
            {"cask_spirits", "clear_mirror_calm_water", "emerald_elytra", "finifugality",
             "keenbranch", "nebulizer", "cqc_manual"},
            // 輔助
            {"bloody_gadget", "dreaming_electric_sheep", "dueling_manual_book_3", "illusory_hunt",
             "late_bloomers_tattoo", "hardship", "phantom_pain", "tenacity_bolus",
             "the_book_of_vengeance", "special_contract", "plume_of_proof", "spicebush_branch",
             "carmilla", "e_type_dimensional_dagger", "trauma_shield"},
            // 便利
            {"blue_zippo_lighter", "child_within_a_flask", "golden_urn", "homeward", "lithograph",
             "oracle", "prejudice", "piece_of_relationship", "rusty_commemorative_coin",
             "someones_device", "sunshower", "trial_plan_guide"},
            // 原創
            {"endless_hunger", "flower_mound", "jin_gang_bolus", "piece_of_a_torn_summer",
             "tranquil_lotus_bolus"}
        };
        for (int g = 0; g < groups.length; g++)
            for (String id : groups[g]) GROUP_MAP.put(id, g);
    }

    /** 飾品所屬體系索引（對應 GROUP_KEYS；未知 id 落到原創組）。 */
    public int getGroup(String id) {
        return GROUP_MAP.getOrDefault(id, GROUP_KEYS.length - 1);
    }

    // ── 等級顯示 helper ─────────────────────────────────────────────────────

    private static final String[] TIER_ROMAN = {"", "I", "II", "III", "IV"};
    private static final String[] TIER_COLORS = {"", "&#AAAAAA", "&#55FF55", "&#55AAFF", "&#FFD700"};

    /** 等級的羅馬數字顯示（1→I … 4→IV）。 */
    public static String roman(int tier) {
        return (tier >= 1 && tier < TIER_ROMAN.length) ? TIER_ROMAN[tier] : String.valueOf(tier);
    }

    /** 等級代表色（與圖鑑分頁一致）。 */
    public static String tierColor(int tier) {
        return (tier >= 1 && tier < TIER_COLORS.length) ? TIER_COLORS[tier] : "&#AAAAAA";
    }

    // ── 初始化 ──────────────────────────────────────────────────────────────

    public void enable() {
        this.lang = new me.yisang.limbusego.gift.lang.LangManager(this);
        this.lang.load();

        ITEM_ID_KEY          = new NamespacedKey("limbusegogift", "accessory_id");
        MENU_OPENER_KEY      = new NamespacedKey("limbusegogift", "menu_opener");
        SLOT_KEYS = new NamespacedKey[5];
        for (int i = 0; i < 5; i++) SLOT_KEYS[i] = new NamespacedKey("limbusegogift", "slot_" + i);

        // ── 現有飾品 ─────────────────────────────────────────────────────
        registerAccessory(new ArdentFlower(this));
        registerAccessory(new AshesToAshes(this));
        registerAccessory(new BlackSheetMusic(this));
        registerAccessory(new BlueZippoLighter(this));
        registerAccessory(new BrilliantVestige(this));
        registerAccessory(new BrokenCompass(this));
        registerAccessory(new CaskSpirits(this));
        registerAccessory(new ChiefButlersSecretArts(this));
        registerAccessory(new ClearMirrorCalmWater(this));
        registerAccessory(new ColdIllusion(this));
        registerAccessory(new CQCManual(this));
        registerAccessory(new CrystallizedBlood(this));
        registerAccessory(new DistantStar(this));
        registerAccessory(new DreamingElectricSheep(this));
        registerAccessory(new DryToTheBoneBreast(this));
        registerAccessory(new DuelingManualBook3(this));
        registerAccessory(new DustToDust(this));
        registerAccessory(new EbonyBrooch(this));
        registerAccessory(new EmeraldElytra(this));
        registerAccessory(new Finifugality(this));
        registerAccessory(new FlowerMound(this));
        registerAccessory(new FrozenCries(this));
        registerAccessory(new GoldenUrn(this));
        registerAccessory(new Hardship(this));
        registerAccessory(new Harestride(this));
        registerAccessory(new Homeward(this));
        registerAccessory(new HotNJuicyDrumstick(this));
        registerAccessory(new IllusoryHunt(this));
        registerAccessory(new JinGangBolus(this));
        registerAccessory(new Keenbranch(this));
        registerAccessory(new LaManchalandAllDayPass(this));
        registerAccessory(new LaManchalandStandardPass(this));
        registerAccessory(new Lithograph(this));
        registerAccessory(new MaskOfTheParade(this));
        registerAccessory(new MentalCorruptionBoostingGas(this));
        registerAccessory(new MoonInTheWater(this));
        registerAccessory(new Oracle(this));
        registerAccessory(new PainOfStifledRage(this));
        registerAccessory(new PhantomPain(this));
        registerAccessory(new PieceOfATornSummer(this));
        registerAccessory(new PieceOfCrumbledEgg(this));
        registerAccessory(new PieceOfRelationship(this));
        registerAccessory(new PlumeOfProof(this));
        registerAccessory(new Rags(this));
        registerAccessory(new Rest(this));
        registerAccessory(new Ruin(this));
        registerAccessory(new SmokingGunpowder(this));
        registerAccessory(new SourLiquorAroma(this));
        registerAccessory(new SpicebushBranch(this));
        registerAccessory(new StrangeGlyphTalisman(this));
        registerAccessory(new TangledBones(this));
        registerAccessory(new TenacityBolus(this));
        registerAccessory(new TheBookOfVengeance(this));
        registerAccessory(new TranquilLotusBolus(this));
        registerAccessory(new TraumaShield(this));
        registerAccessory(new TrialPlanGuide(this));
        registerAccessory(new TwinklingVestige(this));

        // ── 新飾品 ───────────────────────────────────────────────────────
        registerAccessory(new ArtisticSense(this));
        registerAccessory(new BloodFlameSword(this));
        registerAccessory(new BloodyGadget(this));
        registerAccessory(new Carmilla(this));
        registerAccessory(new ChildWithinAFlask(this));
        registerAccessory(new DarkVestige(this));
        registerAccessory(new EndlessHunger(this));
        registerAccessory(new ETypeDimensionalDagger(this));
        registerAccessory(new FaintVestige(this));
        registerAccessory(new FlowerInTheMirror(this));
        registerAccessory(new GlimpseOfFlames(this));
        registerAccessory(new GreenSpirit(this));
        registerAccessory(new HandheldMirror(this));
        registerAccessory(new LateBloomersTattoo(this));
        registerAccessory(new Millarca(this));
        registerAccessory(new Nebulizer(this));
        registerAccessory(new NixieDivergence(this));
        registerAccessory(new Prejudice(this));
        registerAccessory(new RoyalJellyPerfume(this));
        registerAccessory(new RustyCommemorativeCoin(this));
        registerAccessory(new SanguineBlossomBolus(this));
        registerAccessory(new SomeonesDevice(this));
        registerAccessory(new Sownpour(this));
        registerAccessory(new SpecialContract(this));
        registerAccessory(new StrangeGlyphInscriptions(this));
        registerAccessory(new Sunshower(this));
        registerAccessory(new Thunderbranch(this));

        // ── Gacha 系統 ──────────────────────────────────────────────────
        gachaChestManager = new GachaChestManager(this);
        threadChestManager = new ThreadChestManager(this);
        shopChestManager = new ShopChestManager(this);
        gachaListener = new GachaListener(this, gachaChestManager, threadChestManager, shopChestManager);
        getServer().getPluginManager().registerEvents(gachaListener, getPlugin());

        startPassiveTick();
        getServer().getPluginManager().registerEvents(this, getPlugin());

        Objects.requireNonNull(getCommand("accessories")).setExecutor(
                (sender, cmd, label, args) -> { if (sender instanceof Player p) openMenu(p); return true; });
    }

    public void disable() {
        if (gachaChestManager != null) gachaChestManager.onDisable();
        if (threadChestManager != null) threadChestManager.onDisable();
        if (shopChestManager != null) shopChestManager.onDisable();
    }

    public void registerAccessory(Accessory acc) {
        accessories.put(acc.getId(), acc);
    }

    // ── 公開工具方法 ────────────────────────────────────────────────────────

    public NamespacedKey getItemIdKey()      { return ITEM_ID_KEY; }
    public NamespacedKey getMenuOpenerKey()  { return MENU_OPENER_KEY; }
    public NamespacedKey getSlotKey(int i)   { return SLOT_KEYS[i]; }

    public String getAccessoryId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        return item.getItemMeta().getPersistentDataContainer()
                .get(ITEM_ID_KEY, PersistentDataType.STRING);
    }

    public boolean isAccessory(ItemStack item) {
        return getAccessoryId(item) != null;
    }

    public Accessory getAccessory(String id) {
        return accessories.get(id);
    }

    public java.util.Collection<Accessory> getAllAccessories() {
        return accessories.values();
    }

    public List<Accessory> getEquippedAccessories(Player player) {
        return equippedCache.computeIfAbsent(player.getUniqueId(),
                k -> AccessoryMenu.getEquipped(player, this));
    }

    /** 裝備欄變動（選單關閉存檔）時呼叫，讓快取重讀 PDC。 */
    public void invalidateEquippedCache(Player player) {
        equippedCache.remove(player.getUniqueId());
    }

    public int getTier(String id) {
        return TIER_MAP.getOrDefault(id, 1);
    }

    public boolean isVestige(String id) {
        return VESTIGE_IDS.contains(id);
    }

    // ── 升級系統 ────────────────────────────────────────────────────────────

    private NamespacedKey upgradeKey(String accessoryId) {
        return upgradeKeys.computeIfAbsent(accessoryId, id -> new NamespacedKey("limbusegogift", "upgrade_" + id));
    }

    public int getUpgradeLevel(Player player, String accessoryId) {
        return player.getPersistentDataContainer()
                .getOrDefault(upgradeKey(accessoryId), PersistentDataType.INTEGER, 0);
    }

    public void setUpgradeLevel(Player player, String accessoryId, int level) {
        player.getPersistentDataContainer()
                .set(upgradeKey(accessoryId), PersistentDataType.INTEGER, Math.min(3, Math.max(0, level)));
    }

    public double getUpgradeMultiplier(Player player, String accessoryId) {
        return switch (getUpgradeLevel(player, accessoryId)) {
            case 1 -> 1.25;
            case 2 -> 1.50;
            case 3 -> 2.00;
            default -> 1.0;
        };
    }

    // 殘影 ID → 可升級的目標飾品等級
    private int vestigeTier(String vestigeId) {
        return switch (vestigeId) {
            case "dark_vestige"      -> 1;
            case "faint_vestige"     -> 2;
            case "twinkling_vestige" -> 3;
            case "brilliant_vestige" -> 4;
            default -> -1;
        };
    }

    // ── 開啟選單 ────────────────────────────────────────────────────────────

    public void openMenu(Player player) {
        new AccessoryMenu(player, this).open();
    }

    // ── 飾品選單開啟物品（NETHER_STAR + PDC menu_opener）────────────────────

    public ItemStack createMenuOpener() {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(msg("msg.menu_opener_name"));
        meta.setLore(List.of(msg("msg.menu_opener_lore")));
        meta.setUnbreakable(true);
        meta.getPersistentDataContainer().set(MENU_OPENER_KEY, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    private boolean isMenuOpener(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer()
                .has(MENU_OPENER_KEY, PersistentDataType.BYTE);
    }

    // ── 被動 Tick（每 20 tick 呼叫所有裝備飾品的 onPassiveTick）──────────────

    private void startPassiveTick() {
        Bukkit.getScheduler().runTaskTimer(getPlugin(), () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                for (Accessory acc : getEquippedAccessories(player)) {
                    acc.onPassiveTick(player);
                }
            }
        }, 0L, 20L);
    }

    // ── GUI 事件 ────────────────────────────────────────────────────────────

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        // 飾品圖鑑 click
        if (event.getInventory().getHolder() instanceof GiftCatalogGUI catalog) {
            int slot = event.getRawSlot();
            if (slot >= event.getInventory().getSize()) {
                if (event.getClick().isShiftClick()) event.setCancelled(true);
                return;
            }
            event.setCancelled(true);
            if (catalog.isSortSlot(slot)) {
                catalog.toggleSortMode(player);
            } else if (catalog.isCloseSlot(slot)) {
                player.closeInventory();
            } else if (catalog.isByGroup()) {
                int group = catalog.getGroupForSlot(slot);
                if (group != -1 && group != catalog.getCurrentGroup()) {
                    catalog.switchGroup(player, group);
                }
            } else {
                int tier = catalog.getTierForSlot(slot);
                if (tier != -1 && tier != catalog.getCurrentTier()) {
                    catalog.switchTier(player, tier);
                }
            }
            return;
        }

        // Admin GUI click
        if (event.getInventory().getHolder() instanceof GiftAdminGUI adminGui) {
            event.setCancelled(true);
            int slot = event.getSlot();
            if (slot < 0 || slot >= 54) return;
            if (slot == 45) { adminGui.changePage(-1); return; }
            if (slot == 53) { adminGui.changePage(+1); return; }
            if (adminGui.isNavSlot(slot)) return;
            ItemStack item = event.getCurrentItem();
            if (item != null && !item.getType().isAir()) {
                player.getInventory().addItem(item.clone());
                player.sendMessage(msg("msg.admin_given"));
            }
            return;
        }

        if (!(event.getInventory().getHolder() instanceof AccessoryMenu menu)) return;

        int raw = event.getRawSlot();
        int size = menu.getInventory().getSize();

        if (raw < size) {
            if (!menu.isAccessorySlot(raw)) {
                event.setCancelled(true);
                return;
            }
            // 飾品欄位：檢查是否用殘影升級
            ItemStack cursor = event.getCursor();
            if (cursor != null && !cursor.getType().isAir()) {
                String cursorId = getAccessoryId(cursor);
                if (cursorId != null && isVestige(cursorId)) {
                    // 嘗試升級當前格子的飾品
                    ItemStack current = event.getCurrentItem();
                    String currentId = getAccessoryId(current);
                    if (currentId != null && !isVestige(currentId)) {
                        int vTier = vestigeTier(cursorId);
                        int aTier = getTier(currentId);
                        if (vTier == aTier) {
                            int curLevel = getUpgradeLevel(player, currentId);
                            if (curLevel < 3) {
                                event.setCancelled(true);
                                setUpgradeLevel(player, currentId, curLevel + 1);
                                // 消耗一個殘影
                                if (cursor.getAmount() > 1) cursor.setAmount(cursor.getAmount() - 1);
                                else event.getView().setCursor(null);
                                int newLevel = curLevel + 1;
                                player.sendMessage(msg("msg.upgrade_success", newLevel));
                                // 刷新選單
                                Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
                                    menu.saveEquipped();
                                    menu.refresh();
                                }, 1L);
                            } else {
                                event.setCancelled(true);
                                player.sendMessage(msg("msg.upgrade_maxed"));
                            }
                            return;
                        }
                    }
                    event.setCancelled(true);
                    return;
                }
                // 非殘影：只允許飾品
                if (!isAccessory(cursor)) {
                    event.setCancelled(true);
                }
            }
        } else {
            if (event.getClick().isShiftClick()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (event.getInventory().getHolder() instanceof GiftCatalogGUI) {
            event.setCancelled(true);
            return;
        }
        if (!(event.getInventory().getHolder() instanceof AccessoryMenu menu)) return;

        // 殘影不可拖入飾品欄
        String draggedId = getAccessoryId(event.getOldCursor());
        if (draggedId != null && isVestige(draggedId)) {
            event.setCancelled(true);
            return;
        }

        for (int slot : event.getRawSlots()) {
            if (slot < menu.getInventory().getSize() && !menu.isAccessorySlot(slot)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        equippedCache.remove(event.getPlayer().getUniqueId());
        for (Accessory acc : accessories.values()) acc.onQuit(event.getPlayer());
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof AccessoryMenu menu) {
            menu.saveEquipped();
        }
    }

    // ── chunk 載入時補生箱子懸浮字（TextDisplay 非持久，chunk 卸載即消失）────

    @EventHandler
    public void onChunkLoad(org.bukkit.event.world.ChunkLoadEvent event) {
        if (event.isNewChunk()) return;
        gachaChestManager.respawnDisplaysInChunk(event.getChunk());
        threadChestManager.respawnDisplaysInChunk(event.getChunk());
        shopChestManager.respawnDisplaysInChunk(event.getChunk());
    }

    // ── 阻止飾品（試煉鑰匙）開啟試煉寶箱 ────────────────────────────────────

    @EventHandler
    public void onVaultInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;
        if (event.getClickedBlock().getType() != Material.VAULT) return;
        if (isAccessory(event.getItem())) event.setCancelled(true);
    }

    // ── 選單開啟物品右鍵 ────────────────────────────────────────────────────

    @EventHandler
    public void onMenuOpenerUse(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!isMenuOpener(event.getItem())) return;
        event.setCancelled(true);
        openMenu(event.getPlayer());
    }

    // ── 飾品 onInteract 分發（右鍵任意互動） ────────────────────────────────

    @EventHandler
    public void onAccessoryInteract(PlayerInteractEvent event) {
        // PlayerInteractEvent 主手/副手各發一次，只取主手避免飾品效果一次右鍵觸發兩次
        if (event.getHand() != org.bukkit.inventory.EquipmentSlot.HAND) return;
        if (isMenuOpener(event.getItem())) return; // 已由上方處理
        Player player = event.getPlayer();
        for (Accessory acc : getEquippedAccessories(player)) {
            acc.onInteract(event, player);
        }
    }

    // ── 攻擊 / 受傷 / 受任何傷害 / 擊殺事件，分發給裝備中的飾品 ──────────────

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player attacker) {
            for (Accessory acc : getEquippedAccessories(attacker)) {
                acc.onAttack(event, attacker);
            }
        }
        if (event.getEntity() instanceof Player victim) {
            for (Accessory acc : getEquippedAccessories(victim)) {
                acc.onDamaged(event, victim);
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event instanceof EntityDamageByEntityEvent) return; // 已由 onEntityDamageByEntity 處理
        if (event.getEntity() instanceof Player victim) {
            for (Accessory acc : getEquippedAccessories(victim)) {
                acc.onAnyDamage(event, victim);
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player deceased = event.getEntity();
        for (Accessory acc : getEquippedAccessories(deceased)) {
            acc.onDeath(event, deceased);
        }
        // 飾品欄開啟工具死亡不掉落
        event.getDrops().removeIf(item -> {
            if (isMenuOpener(item)) {
                event.getItemsToKeep().add(item);
                return true;
            }
            return false;
        });
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;
        for (Accessory acc : getEquippedAccessories(killer)) {
            acc.onKill(event, killer);
        }
    }

    // ── 指令 ────────────────────────────────────────────────────────────────

    public boolean onGetGift(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) return false;

        if (args[0].equalsIgnoreCase("give")) {
            if (!sender.hasPermission("limbus.admin") && !(sender instanceof org.bukkit.command.ConsoleCommandSender)) return true;
            if (args.length < 3) { sender.sendMessage(msg("cmd.usage_getgift_give")); return true; }
            Player target = Bukkit.getPlayerExact(args[1]);
            if (target == null) { sender.sendMessage(msg("cmd.player_not_found", args[1])); return true; }
            if (args[2].equalsIgnoreCase("menu")) {
                target.getInventory().addItem(createMenuOpener());
                return true;
            }
            if (args[2].equalsIgnoreCase("thread") || args[2].equalsIgnoreCase("lunacy")) {
                int amount = 1;
                if (args.length >= 4) {
                    try { amount = Math.min(64, Integer.parseInt(args[3])); } catch (NumberFormatException ignored) {}
                }
                ItemStack token = args[2].equalsIgnoreCase("thread")
                        ? gachaListener.createThread(amount)
                        : gachaListener.createLunacy(amount);
                target.getInventory().addItem(token);
                return true;
            }
            Accessory acc = accessories.get(args[2].toLowerCase());
            if (acc != null) acc.give(target);
            return true;
        }

        if (!(sender instanceof Player player)) return true;
        String id = args[0].toLowerCase();
        if (id.equals("admin")) {
            if (!player.hasPermission("limbus.admin") && !player.isOp()) return true;
            player.openInventory(new GiftAdminGUI(this, 0).getInventory());
            return true;
        }
        if (id.equals("menu")) {
            player.getInventory().addItem(createMenuOpener());
        } else if (id.equals("lunacy")) {
            if (!player.hasPermission("limbus.admin") && !player.isOp()) return true;
            int amount = 1;
            if (args.length >= 2) {
                try { amount = Math.min(64, Integer.parseInt(args[1])); } catch (NumberFormatException ignored) {}
            }
            player.getInventory().addItem(gachaListener.createLunacy(amount));
        } else if (id.equals("thread")) {
            if (!player.hasPermission("limbus.admin") && !player.isOp()) return true;
            int amount = 1;
            if (args.length >= 2) {
                try { amount = Math.min(64, Integer.parseInt(args[1])); } catch (NumberFormatException ignored) {}
            }
            player.getInventory().addItem(gachaListener.createThread(amount));
        } else {
            Accessory acc = accessories.get(id);
            if (acc != null) acc.give(player);
        }
        return true;
    }

    /** 開啟飾品圖鑑（/limbusego gift category）。舊 /egogift 的 reload/language 分支
     *  已由 /limbusego reload、/limbusego language 統一取代，不再保留。 */
    public void openCatalog(Player player) {
        player.openInventory(new GiftCatalogGUI(this, 1).getInventory());
    }

    public boolean onGachaChest(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;
        if (!player.hasPermission("limbus.admin") && !player.isOp()) {
            player.sendMessage(msg("cmd.no_permission"));
            return true;
        }
        if (args.length == 0) return false;

        org.bukkit.block.Block target = player.getTargetBlockExact(10);
        if (target == null || target.getType() != Material.CHEST) {
            player.sendMessage(msg("msg.gacha.look_at_chest"));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "set" -> {
                if (gachaChestManager.register(target.getLocation())) {
                    player.sendMessage(msg("msg.gacha.set_success"));
                } else {
                    player.sendMessage(msg("msg.gacha.set_duplicate"));
                }
            }
            case "remove" -> {
                if (gachaChestManager.unregister(target.getLocation())) {
                    player.sendMessage(msg("msg.gacha.remove_success"));
                } else {
                    player.sendMessage(msg("msg.gacha.remove_missing"));
                }
            }
            default -> { return false; }
        }
        return true;
    }

    public boolean onThreadChest(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;
        if (!player.hasPermission("limbus.admin") && !player.isOp()) {
            player.sendMessage(msg("cmd.no_permission"));
            return true;
        }
        if (args.length == 0) return false;

        org.bukkit.block.Block target = player.getTargetBlockExact(10);
        if (target == null || target.getType() != Material.CHEST) {
            player.sendMessage(msg("msg.thread.look_at_chest"));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "set" -> {
                if (args.length < 3) {
                    player.sendMessage(msg("msg.thread.usage"));
                    return true;
                }
                int cost;
                try {
                    cost = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    player.sendMessage(msg("msg.thread.cost_not_integer"));
                    return true;
                }
                if (cost < 1) {
                    player.sendMessage(msg("msg.thread.cost_too_low"));
                    return true;
                }

                String currency = "thread";
                int nameStartIdx = 2;
                if (args.length >= 4 && (args[2].equalsIgnoreCase("thread") || args[2].equalsIgnoreCase("lunacy"))) {
                    currency = args[2].toLowerCase();
                    nameStartIdx = 3;
                }
                if (nameStartIdx >= args.length) {
                    player.sendMessage(msg("msg.thread.need_name"));
                    return true;
                }

                String name = String.join(" ", java.util.Arrays.copyOfRange(args, nameStartIdx, args.length));
                String currencyDisplay = msg("currency." + currency);
                if (threadChestManager.register(target.getLocation(), name, cost, currency)) {
                    player.sendMessage(msg("msg.thread.set_success", name, cost, currencyDisplay));
                    player.sendMessage(msg("msg.thread.set_hint"));
                } else {
                    player.sendMessage(msg("msg.thread.set_duplicate"));
                }
            }
            case "remove" -> {
                if (threadChestManager.unregister(target.getLocation())) {
                    player.sendMessage(msg("msg.thread.remove_success"));
                } else {
                    player.sendMessage(msg("msg.thread.remove_missing"));
                }
            }
            default -> { return false; }
        }
        return true;
    }

    public boolean onShopChest(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;
        if (!player.hasPermission("limbus.admin") && !player.isOp()) {
            player.sendMessage(msg("cmd.no_permission"));
            return true;
        }
        if (args.length == 0) return false;

        org.bukkit.block.Block target = player.getTargetBlockExact(10);
        if (target == null || target.getType() != Material.CHEST) {
            player.sendMessage(msg("msg.shop.look_at_chest"));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "set" -> {
                if (args.length < 3) {
                    player.sendMessage(msg("msg.shop.usage"));
                    return true;
                }
                int cost;
                try {
                    cost = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    player.sendMessage(msg("msg.shop.cost_not_integer"));
                    return true;
                }
                if (cost < 1) {
                    player.sendMessage(msg("msg.shop.cost_too_low"));
                    return true;
                }

                String currency = "thread";
                int nameStartIdx = 2;
                if (args.length >= 4 && (args[2].equalsIgnoreCase("thread") || args[2].equalsIgnoreCase("lunacy"))) {
                    currency = args[2].toLowerCase();
                    nameStartIdx = 3;
                }
                if (nameStartIdx >= args.length) {
                    player.sendMessage(msg("msg.shop.need_name"));
                    return true;
                }

                String name = String.join(" ", java.util.Arrays.copyOfRange(args, nameStartIdx, args.length));
                String currencyDisplay = msg("currency." + currency);
                if (shopChestManager.register(target.getLocation(), name, cost, currency)) {
                    player.sendMessage(msg("msg.shop.set_success", name, cost, currencyDisplay));
                    player.sendMessage(msg("msg.shop.set_hint"));
                } else {
                    player.sendMessage(msg("msg.shop.set_duplicate"));
                }
            }
            case "remove" -> {
                if (shopChestManager.unregister(target.getLocation())) {
                    player.sendMessage(msg("msg.shop.remove_success"));
                } else {
                    player.sendMessage(msg("msg.shop.remove_missing"));
                }
            }
            default -> { return false; }
        }
        return true;
    }

    // ── 顏色代碼工具 ─────────────────────────────────────────────────────────

    public String color(String message) {
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String hex = matcher.group(1);
            StringBuilder r = new StringBuilder("§x");
            for (char c : hex.toCharArray()) r.append('§').append(c);
            matcher.appendReplacement(sb, r.toString());
        }
        matcher.appendTail(sb);
        return ChatColor.translateAlternateColorCodes('&', sb.toString());
    }
}
