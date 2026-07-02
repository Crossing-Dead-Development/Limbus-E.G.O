package me.yisang.limbusego.gift;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.stream.Collectors;

public class GiftCatalogGUI implements InventoryHolder {

    private static final int[] TAB_SLOTS = {1, 3, 5, 7}; // Tier 1-4 對應 slot
    private static final int CLOSE_SLOT = 49;

    private static final String[] TIER_COLORS = {"", "&#AAAAAA", "&#55FF55", "&#55AAFF", "&#FFD700"};
    private static final Material[] TIER_PANES = {
        null,
        Material.LIGHT_GRAY_STAINED_GLASS_PANE,
        Material.LIME_STAINED_GLASS_PANE,
        Material.BLUE_STAINED_GLASS_PANE,
        Material.YELLOW_STAINED_GLASS_PANE
    };

    private final GiftsModule plugin;
    private Inventory inventory;
    private int currentTier;

    public GiftCatalogGUI(GiftsModule plugin, int tier) {
        this.plugin = plugin;
        this.currentTier = tier;
        build();
    }

    private void build() {
        inventory = Bukkit.createInventory(this, 54, plugin.msg("gui.catalog_title"));

        ItemStack border = makeItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 9; i++) inventory.setItem(i, border);
        for (int i = 45; i < 54; i++) inventory.setItem(i, border);

        // 階級 Tab
        for (int tier = 1; tier <= 4; tier++) {
            boolean active = (tier == currentTier);
            Material mat = active ? Material.WHITE_STAINED_GLASS_PANE : TIER_PANES[tier];
            String name = TIER_COLORS[tier] + (active ? "&l▶ " : "") + plugin.getLang().get("gui.tab_tier", tier);
            inventory.setItem(TAB_SLOTS[tier - 1], makeItem(mat, name));
        }

        // 飾品列表
        List<Accessory> accs = plugin.getAllAccessories().stream()
            .filter(a -> plugin.getTier(a.getId()) == currentTier && !plugin.isVestige(a.getId()))
            .collect(Collectors.toList());

        for (int i = 0; i < 36; i++) {
            inventory.setItem(9 + i, i < accs.size() ? accs.get(i).createItem() : border);
        }

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
        build();
        player.openInventory(inventory);
    }

    public int getTierForSlot(int slot) {
        for (int i = 0; i < TAB_SLOTS.length; i++) {
            if (TAB_SLOTS[i] == slot) return i + 1;
        }
        return -1;
    }

    public boolean isCloseSlot(int slot) { return slot == CLOSE_SLOT; }

    public int getCurrentTier() { return currentTier; }

    @Override
    public Inventory getInventory() { return inventory; }
}
