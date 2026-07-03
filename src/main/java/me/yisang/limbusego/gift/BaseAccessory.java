package me.yisang.limbusego.gift;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import me.yisang.limbusego.status.StatusEffect;
import me.yisang.limbusego.status.StatusManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class BaseAccessory implements Accessory {

    private static final Map<String, Integer> CUSTOM_MODEL_DATA = Map.ofEntries(
        Map.entry("ardent_flower", 1),
        Map.entry("ashes_to_ashes", 2),
        Map.entry("artistic_sense", 3),
        Map.entry("black_sheet_music", 4),
        Map.entry("bloodflame_sword", 5),
        Map.entry("bloody_gadget", 6),
        Map.entry("blue_zippo_lighter", 7),
        Map.entry("brilliant_vestige", 8),
        Map.entry("broken_compass", 9),
        Map.entry("carmilla", 10),
        Map.entry("cask_spirits", 11),
        Map.entry("chief_butlers_secret_arts", 12),
        Map.entry("child_within_a_flask", 13),
        Map.entry("clear_mirror_calm_water", 14),
        Map.entry("cold_illusion", 15),
        Map.entry("cqc_manual", 16),
        Map.entry("crystallized_blood", 17),
        Map.entry("dark_vestige", 18),
        Map.entry("distant_star", 19),
        Map.entry("dreaming_electric_sheep", 20),
        Map.entry("dry_to_the_bone_breast", 21),
        Map.entry("dueling_manual_book_3", 22),
        Map.entry("dust_to_dust", 23),
        Map.entry("e_type_dimensional_dagger", 24),
        Map.entry("ebony_brooch", 25),
        Map.entry("emerald_elytra", 26),
        Map.entry("endless_hunger", 27),
        Map.entry("faint_vestige", 28),
        Map.entry("finifugality", 29),
        Map.entry("flower_in_the_mirror", 30),
        Map.entry("flower_mound", 31),
        Map.entry("frozen_cries", 32),
        Map.entry("glimpse_of_flames", 33),
        Map.entry("golden_urn", 34),
        Map.entry("green_spirit", 35),
        Map.entry("handheld_mirror", 36),
        Map.entry("hardship", 37),
        Map.entry("harestride", 38),
        Map.entry("homeward", 39),
        Map.entry("hot_n_juicy_drumstick", 40),
        Map.entry("illusory_hunt", 41),
        Map.entry("jin_gang_bolus", 42),
        Map.entry("keenbranch", 43),
        Map.entry("la_manchaland_all_day_pass", 44),
        Map.entry("la_manchaland_standard_pass", 45),
        Map.entry("late_bloomers_tattoo", 46),
        Map.entry("lithograph", 47),
        Map.entry("lunacy", 48),
        Map.entry("mask_of_the_parade", 49),
        Map.entry("mental_corruption_boosting_gas", 50),
        Map.entry("millarca", 51),
        Map.entry("moon_in_the_water", 52),
        Map.entry("nebulizer", 53),
        Map.entry("nixie_divergence", 54),
        Map.entry("oracle", 55),
        Map.entry("pain_of_stifled_rage", 56),
        Map.entry("phantom_pain", 57),
        Map.entry("piece_of_a_torn_summer", 58),
        Map.entry("piece_of_crumbled_egg", 59),
        Map.entry("piece_of_relationship", 60),
        Map.entry("plume_of_proof", 61),
        Map.entry("prejudice", 62),
        Map.entry("rags", 63),
        Map.entry("rest", 64),
        Map.entry("royal_jelly_perfume", 65),
        Map.entry("ruin", 66),
        Map.entry("rusty_commemorative_coin", 67),
        Map.entry("sanguine_blossom_bolus", 68),
        Map.entry("smoking_gunpowder", 69),
        Map.entry("someones_device", 70),
        Map.entry("sour_liquor_aroma", 71),
        Map.entry("sownpour", 72),
        Map.entry("special_contract", 73),
        Map.entry("spicebush_branch", 74),
        Map.entry("strange_glyph_inscriptions", 75),
        Map.entry("strange_glyph_talisman", 76),
        Map.entry("sunshower", 77),
        Map.entry("tangled_bones", 78),
        Map.entry("tenacity_bolus", 79),
        Map.entry("the_book_of_vengeance", 80),
        Map.entry("thunderbranch", 81),
        Map.entry("tranquil_lotus_bolus", 82),
        Map.entry("trauma_shield", 83),
        Map.entry("trial_plan_guide", 84),
        Map.entry("twinkling_vestige", 85)
    );

    protected final GiftsModule plugin;
    private final String id;
    private final String defaultName;
    private final String defaultDescColor;   // nullable
    private final String defaultDescription; // nullable, split by \n
    private final String defaultEffect;

    // ── 12 屬性體系共用 helper（Phase 2）─────────────────────────────────
    private final Map<UUID, Long> gateMap = new HashMap<>();

    // 無描述（舊格式向下相容）
    protected BaseAccessory(GiftsModule plugin, String id, String name, String effect) {
        this.plugin = plugin;
        this.id = id;
        this.defaultName = name;
        this.defaultDescColor = null;
        this.defaultDescription = null;
        this.defaultEffect = effect;
    }

    // 有描述（描述顏色 + 描述文字 + 效果文字）
    protected BaseAccessory(GiftsModule plugin, String id, String name,
                            String descColor, String description, String effect) {
        this.plugin = plugin;
        this.id = id;
        this.defaultName = name;
        this.defaultDescColor = descColor;
        this.defaultDescription = description;
        this.defaultEffect = effect;
    }

    @Override
    public String getId() { return id; }

    @Override
    public String getDisplayName() { return plugin.color("&#FFFFFF" + localizedName()); }

    /** 從語言檔取顯示名；找不到就回退到建構子的中文預設值。 */
    private String localizedName() {
        String v = plugin.getLang().getOrNull("gift." + id + ".name");
        return v != null ? v : defaultName;
    }

    /** 從語言檔取描述行；找不到就用建構子預設值。 */
    private List<String> localizedDescriptionLines() {
        List<String> raw = plugin.getLang().getListOrNull("gift." + id + ".description");
        if (raw != null) return raw;
        // 舊格式向下相容：把 defaultDescription split 後套 defaultDescColor
        if (defaultDescription == null) return List.of();
        List<String> out = new ArrayList<>();
        for (String part : defaultDescription.split("\n")) out.add(defaultDescColor + part);
        return out;
    }

    private String localizedEffect() {
        String v = plugin.getLang().getOrNull("gift." + id + ".effect");
        return v != null ? v : defaultEffect;
    }

    @Override
    public ItemStack createItem() {
        ItemStack item = new ItemStack(Material.TRIAL_KEY);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(plugin.color("&#FFFFFF" + localizedName()));

        List<String> lore = new ArrayList<>();
        if (!plugin.isVestige(id)) {
            int tier = plugin.getTier(id);
            lore.add(plugin.color(GiftsModule.tierColor(tier)
                    + plugin.getLang().get("gui.tier_line", GiftsModule.roman(tier))));
        }
        for (String line : localizedDescriptionLines()) lore.add(plugin.color(line));
        // 效果行預設灰字；lang 檔若自帶顏色，就別再前綴
        String effect = localizedEffect();
        String coloredEffect = effect.startsWith("&") ? effect : ("&#AAAAAA" + effect);
        lore.add(plugin.color(coloredEffect));
        meta.setLore(lore);

        meta.setItemModel(new NamespacedKey("gifts", id));
        Integer cmd = CUSTOM_MODEL_DATA.get(id);
        if (cmd != null) meta.setCustomModelData(cmd);
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
        meta.getPersistentDataContainer().set(plugin.getItemIdKey(), PersistentDataType.STRING, id);
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public void give(Player player) {
        player.getInventory().addItem(createItem());
    }

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
}
