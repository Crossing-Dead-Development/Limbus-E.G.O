package me.yisang.limbusego.gift;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class GiftCatalogGUI implements InventoryHolder {

    private static final int[] TAB_SLOTS = {1, 3, 5, 7}; // 等級模式：Tier I-IV 對應 slot
    private static final int SORT_SLOT = 45;
    private static final int CLOSE_SLOT = 49;

    private static final Material[] TIER_PANES = {
        null,
        Material.LIGHT_GRAY_STAINED_GLASS_PANE,
        Material.LIME_STAINED_GLASS_PANE,
        Material.BLUE_STAINED_GLASS_PANE,
        Material.YELLOW_STAINED_GLASS_PANE
    };

    // 體系模式：九組分頁（順序對應 GiftsModule.GROUP_KEYS），佔滿頂列 slot 0-8
    private static final Material[] GROUP_PANES = {
        Material.ORANGE_STAINED_GLASS_PANE,      // 燒傷
        Material.RED_STAINED_GLASS_PANE,         // 流血
        Material.BLUE_STAINED_GLASS_PANE,        // 沉淪
        Material.PURPLE_STAINED_GLASS_PANE,      // 破裂
        Material.YELLOW_STAINED_GLASS_PANE,      // 震顫
        Material.LIGHT_BLUE_STAINED_GLASS_PANE,  // 呼吸法
        Material.LIME_STAINED_GLASS_PANE,        // 輔助
        Material.LIGHT_GRAY_STAINED_GLASS_PANE,  // 便利
        Material.PINK_STAINED_GLASS_PANE         // 原創
    };

    private final GiftsModule plugin;
    private Inventory inventory;
    private int currentTier;
    private int currentGroup = 0;
    private boolean byGroup = false; // false=依等級排序（預設）, true=依體系排序

    public GiftCatalogGUI(GiftsModule plugin, int tier) {
        this.plugin = plugin;
        this.currentTier = tier;
        build();
    }

    /** 首次建立 Inventory；之後切分頁/切模式都原地重繪，不重開視窗（避免關窗重開的卡頓與閃爍）。 */
    private void build() {
        if (inventory == null) inventory = Bukkit.createInventory(this, 54, plugin.msg("gui.catalog_title"));

        ItemStack border = makeItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 9; i++) inventory.setItem(i, border);
        for (int i = 45; i < 54; i++) inventory.setItem(i, border);

        List<Accessory> accs;
        if (byGroup) {
            // 體系 Tab（頂列 0-8）
            for (int g = 0; g < GROUP_PANES.length; g++) {
                boolean active = (g == currentGroup);
                Material mat = active ? Material.WHITE_STAINED_GLASS_PANE : GROUP_PANES[g];
                String label = plugin.getLang().get("gui.group_" + GiftsModule.GROUP_KEYS[g]);
                inventory.setItem(g, makeItem(mat, "&f" + (active ? "&l▶ " : "") + label));
            }
            // 該體系飾品，依等級再依 id 排序
            accs = plugin.getAllAccessories().stream()
                .filter(a -> !plugin.isVestige(a.getId()) && plugin.getGroup(a.getId()) == currentGroup)
                .sorted(Comparator.comparingInt((Accessory a) -> plugin.getTier(a.getId()))
                        .thenComparing(Accessory::getId))
                .collect(Collectors.toList());
        } else {
            // 階級 Tab
            for (int tier = 1; tier <= 4; tier++) {
                boolean active = (tier == currentTier);
                Material mat = active ? Material.WHITE_STAINED_GLASS_PANE : TIER_PANES[tier];
                String name = GiftsModule.tierColor(tier) + (active ? "&l▶ " : "")
                        + plugin.getLang().get("gui.tab_tier", GiftsModule.roman(tier));
                inventory.setItem(TAB_SLOTS[tier - 1], makeItem(mat, name));
            }
            accs = plugin.getAllAccessories().stream()
                .filter(a -> plugin.getTier(a.getId()) == currentTier && !plugin.isVestige(a.getId()))
                .collect(Collectors.toList());
        }

        for (int i = 0; i < 36; i++) {
            inventory.setItem(9 + i, i < accs.size() ? accs.get(i).createItem() : border);
        }

        // 排序模式切換按鈕
        String mode = plugin.getLang().get(byGroup ? "gui.sort_by_group" : "gui.sort_by_tier");
        inventory.setItem(SORT_SLOT, makeItem(Material.COMPARATOR, plugin.getLang().get("gui.sort_mode", mode)));

        // 關閉按鈕
        inventory.setItem(CLOSE_SLOT, makeItem(Material.BARRIER, plugin.getLang().get("gui.close")));
    }

    private ItemStack makeItem(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(plugin.color(name));
            item.setItemMeta(meta);
        }
        return item;
    }

    public void switchTier(Player player, int tier) {
        currentTier = tier;
        build(); // 同一個 Inventory 原地重繪，玩家視窗即時更新
    }

    public void switchGroup(Player player, int group) {
        currentGroup = group;
        build();
    }

    public void toggleSortMode(Player player) {
        byGroup = !byGroup;
        build();
    }

    public int getTierForSlot(int slot) {
        for (int i = 0; i < TAB_SLOTS.length; i++) {
            if (TAB_SLOTS[i] == slot) return i + 1;
        }
        return -1;
    }

    public int getGroupForSlot(int slot) {
        return (slot >= 0 && slot < GROUP_PANES.length) ? slot : -1;
    }

    public boolean isSortSlot(int slot) { return slot == SORT_SLOT; }

    public boolean isCloseSlot(int slot) { return slot == CLOSE_SLOT; }

    public boolean isByGroup() { return byGroup; }

    public int getCurrentTier() { return currentTier; }

    public int getCurrentGroup() { return currentGroup; }

    @Override
    public Inventory getInventory() { return inventory; }
}
