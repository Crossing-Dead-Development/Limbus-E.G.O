package me.yisang.limbusego.gift;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class AccessoryMenu implements InventoryHolder {

    // 5 個飾品欄位在 27 格 GUI 中的位置（3+2 排列）
    public static final int[] ACCESSORY_SLOTS = {11, 13, 15, 21, 23};

    private static final ItemStack FILLER;

    static {
        FILLER = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fm = FILLER.getItemMeta();
        fm.setDisplayName(" ");
        FILLER.setItemMeta(fm);
    }

    private final Inventory inventory;
    private final Player player;
    private final GiftsModule plugin;

    public AccessoryMenu(Player player, GiftsModule plugin) {
        this.player = player;
        this.plugin = plugin;
        this.inventory = Bukkit.createInventory(this, 27, plugin.msg("gui.menu_title"));

        // 填充裝飾格（飾品欄位留空）
        for (int i = 0; i < 27; i++) {
            if (!isAccessorySlot(i)) inventory.setItem(i, FILLER.clone());
        }

        loadEquipped();
    }

    @Override
    public Inventory getInventory() { return inventory; }

    public boolean isAccessorySlot(int rawSlot) {
        for (int s : ACCESSORY_SLOTS) {
            if (s == rawSlot) return true;
        }
        return false;
    }

    public void open() { player.openInventory(inventory); }

    // 關閉時將飾品欄內容寫入 PDC
    public void saveEquipped() {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        for (int i = 0; i < ACCESSORY_SLOTS.length; i++) {
            NamespacedKey key = plugin.getSlotKey(i);
            ItemStack item = inventory.getItem(ACCESSORY_SLOTS[i]);
            String id = plugin.getAccessoryId(item);
            if (id != null) {
                pdc.set(key, PersistentDataType.STRING, id);
            } else {
                pdc.remove(key);
            }
        }
        plugin.invalidateEquippedCache(player);
    }

    // 開啟時從 PDC 還原已裝備的飾品
    private void loadEquipped() {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        for (int i = 0; i < ACCESSORY_SLOTS.length; i++) {
            NamespacedKey key = plugin.getSlotKey(i);
            String id = pdc.get(key, PersistentDataType.STRING);
            if (id != null) {
                Accessory acc = plugin.getAccessory(id);
                if (acc != null) {
                    inventory.setItem(ACCESSORY_SLOTS[i], withUpgradeLore(acc, player));
                }
            }
        }
    }

    // 重新整理顯示（升級後呼叫）
    public void refresh() {
        loadEquipped();
    }

    // 若飾品有升級，附加升級等級到 Lore
    private ItemStack withUpgradeLore(Accessory acc, Player player) {
        ItemStack item = acc.createItem();
        int level = plugin.getUpgradeLevel(player, acc.getId());
        if (level <= 0) return item;
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        lore.add(plugin.msg("msg.upgrade_lore", level));
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    // 供被動 Tick / 事件讀取目前裝備（直接讀 PDC，不依賴介面是否開啟）
    public static List<Accessory> getEquipped(Player player, GiftsModule plugin) {
        List<Accessory> result = new ArrayList<>();
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        for (int i = 0; i < ACCESSORY_SLOTS.length; i++) {
            String id = pdc.get(plugin.getSlotKey(i), PersistentDataType.STRING);
            if (id != null) {
                Accessory acc = plugin.getAccessory(id);
                if (acc != null) result.add(acc);
            }
        }
        return result;
    }

}
