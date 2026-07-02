package me.yisang.limbusego.gift;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class GachaListener implements Listener {

    private final int lunacyCost;
    private final double tier4Chance;

    private final GiftsModule plugin;
    private final GachaChestManager chestMgr;
    private final ThreadChestManager threadMgr;
    private final ShopChestManager shopMgr;
    private final NamespacedKey LUNACY_KEY;
    private final NamespacedKey THREAD_KEY;
    private final Random rng = new Random();

    public GachaListener(GiftsModule plugin, GachaChestManager chestMgr,
                         ThreadChestManager threadMgr, ShopChestManager shopMgr) {
        this.plugin = plugin;
        this.chestMgr = chestMgr;
        this.threadMgr = threadMgr;
        this.shopMgr = shopMgr;
        this.LUNACY_KEY = new NamespacedKey(plugin.getPlugin(), "lunacy");
        this.THREAD_KEY = new NamespacedKey(plugin.getPlugin(), "thread");
        this.lunacyCost = plugin.getConfig().getInt("gacha.lunacy-cost", 32);
        this.tier4Chance = plugin.getConfig().getDouble("gacha.tier4-chance", 0.10);
    }

    // ── 紡錘代幣 ──────────────────────────────────────────────────────────────

    public ItemStack createThread(int amount) {
        ItemStack item = new ItemStack(Material.STRING, amount);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(plugin.color("&#FFE5A0紡錘"));
        meta.setLore(List.of(plugin.color("&#FFE5A0將所有可能性如絲線般紡織在一起的物品。")));
        meta.setItemModel(new NamespacedKey("gifts", "thread"));
        meta.getPersistentDataContainer().set(THREAD_KEY, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    public boolean isThread(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(THREAD_KEY, PersistentDataType.BYTE);
    }

    private int countThread(Player player) {
        int total = 0;
        for (ItemStack i : player.getInventory().getContents()) {
            if (isThread(i)) total += i.getAmount();
        }
        return total;
    }

    private void removeThread(Player player, int amount) {
        int remaining = amount;
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length && remaining > 0; i++) {
            ItemStack item = contents[i];
            if (!isThread(item)) continue;
            if (item.getAmount() <= remaining) {
                remaining -= item.getAmount();
                contents[i] = null;
            } else {
                item.setAmount(item.getAmount() - remaining);
                remaining = 0;
            }
        }
        player.getInventory().setContents(contents);
    }

    // ── 狂氣物品 ──────────────────────────────────────────────────────────────

    public ItemStack createLunacy(int amount) {
        ItemStack item = new ItemStack(Material.WITHER_ROSE, amount);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(plugin.color("&#FF0000狂氣"));
        meta.setLore(List.of(plugin.color("&#FF0000人因心懷狂氣，才有前進的動力")));
        meta.setItemModel(new NamespacedKey("gifts", "lunacy"));
        meta.getPersistentDataContainer().set(LUNACY_KEY, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    public boolean isLunacy(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(LUNACY_KEY, PersistentDataType.BYTE);
    }

    private int countLunacy(Player player) {
        int total = 0;
        for (ItemStack i : player.getInventory().getContents()) {
            if (isLunacy(i)) total += i.getAmount();
        }
        return total;
    }

    private void removeLunacy(Player player, int amount) {
        int remaining = amount;
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length && remaining > 0; i++) {
            ItemStack item = contents[i];
            if (!isLunacy(item)) continue;
            if (item.getAmount() <= remaining) {
                remaining -= item.getAmount();
                contents[i] = null;
            } else {
                item.setAmount(item.getAmount() - remaining);
                remaining = 0;
            }
        }
        player.getInventory().setContents(contents);
    }

    // ── 阻止代幣放置成方塊（狂氣→凋零玫瑰、紡錘→絆線）──────────────────────────

    @EventHandler
    public void onCurrencyBlockPlace(BlockPlaceEvent event) {
        ItemStack held = event.getItemInHand();
        if (isLunacy(held) || isThread(held)) {
            event.setCancelled(true);
        }
    }

    // ── 右鍵箱子觸發 ─────────────────────────────────────────────────────────

    @EventHandler
    public void onChestClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;
        if (event.getClickedBlock().getType() != Material.CHEST) return;
        if (!chestMgr.isGachaChest(event.getClickedBlock().getLocation())) return;
        event.setCancelled(true);

        Player player = event.getPlayer();
        int lunacyCount = countLunacy(player);

        if (lunacyCount < lunacyCost) {
            player.sendMessage(plugin.color("&#FF5555狂氣不足！需要 " + lunacyCost + " 個，你只有 " + lunacyCount + " 個。"));
            return;
        }

        removeLunacy(player, lunacyCost);
        Accessory result = draw(player);

        if (result == null) {
            player.sendMessage(plugin.color("&#FF5555抽取失敗：飾品池為空。"));
            return;
        }

        boolean isTierIV = plugin.getTier(result.getId()) == 4;
        playDrawEffect(event.getClickedBlock().getLocation(), isTierIV);

        result.give(player);

        String tierColor = isTierIV ? "&#FFD700" : "&#FFFFFF";
        player.sendMessage(plugin.color("&#9928BB[飾品提取] &#AAAAAA獲得：" + tierColor) + result.getDisplayName());
    }

    // ── 自訂抽獎箱（支援紡錘 / 狂氣兩種貨幣）─────────────────────────────────

    @EventHandler
    public void onThreadChestClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;
        if (event.getClickedBlock().getType() != Material.CHEST) return;
        Location chestLoc = event.getClickedBlock().getLocation();
        if (!threadMgr.isThreadChest(chestLoc)) return;

        Player player = event.getPlayer();
        boolean isAdmin = player.hasPermission("limbus.admin") || player.isOp();

        // 管理員潛行右鍵：照常開箱以編輯獎池
        if (isAdmin && player.isSneaking()) return;

        // 其餘一律不可開箱，改為抽取
        event.setCancelled(true);

        int cost = threadMgr.getCost(chestLoc);
        String currency = threadMgr.getCurrency(chestLoc);
        boolean useLunacy = currency.equals("lunacy");

        int have = useLunacy ? countLunacy(player) : countThread(player);
        String currencyName = useLunacy ? "狂氣" : "紡錘";
        if (have < cost) {
            player.sendMessage(plugin.color("&#FF5555" + currencyName + "不足！需要 " + cost + " 個，你只有 " + have + " 個。"));
            return;
        }

        ItemStack prize = drawFromChest(event.getClickedBlock());
        if (prize == null) {
            player.sendMessage(plugin.color("&#FF5555此抽獎箱目前是空的。"));
            return;
        }

        if (useLunacy) {
            removeLunacy(player, cost);
        } else {
            removeThread(player, cost);
        }
        playThreadEffect(chestLoc);

        // 給獎（背包滿則掉落腳邊）
        Map<Integer, ItemStack> overflow = player.getInventory().addItem(prize);
        for (ItemStack left : overflow.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), left);
        }

        String boxName = threadMgr.getName(chestLoc);
        String prizeName = (prize.hasItemMeta() && prize.getItemMeta().hasDisplayName())
                ? prize.getItemMeta().getDisplayName()
                : prize.getType().name();
        String msgColor = useLunacy ? "&#FF0000" : "&#FFE5A0";
        player.sendMessage(plugin.color(msgColor + "[" + boxName + "] &#AAAAAA獲得：&#FFFFFF" + prizeName));
    }

    // ── 購買商店箱（付固定貨幣量,拿走箱內全部商品的複本;箱內不減）─────────────

    @EventHandler
    public void onShopChestClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;
        if (event.getClickedBlock().getType() != Material.CHEST) return;
        Location chestLoc = event.getClickedBlock().getLocation();
        if (!shopMgr.isShopChest(chestLoc)) return;

        Player player = event.getPlayer();
        boolean isAdmin = player.hasPermission("limbus.admin") || player.isOp();

        // 管理員潛行右鍵:照常開箱以編輯商品內容
        if (isAdmin && player.isSneaking()) return;

        event.setCancelled(true);

        int cost = shopMgr.getCost(chestLoc);
        String currency = shopMgr.getCurrency(chestLoc);
        boolean useLunacy = currency.equals("lunacy");
        int have = useLunacy ? countLunacy(player) : countThread(player);
        String currencyName = useLunacy ? "狂氣" : "紡錘";

        if (have < cost) {
            player.sendMessage(plugin.color("&#FF5555" + currencyName + "不足!需要 " + cost + " 個,你只有 " + have + " 個。"));
            return;
        }

        // 取出所有商品的複本
        List<ItemStack> goods = collectShopGoods(event.getClickedBlock());
        if (goods.isEmpty()) {
            player.sendMessage(plugin.color("&#FF5555此商店目前沒有商品。"));
            return;
        }

        if (useLunacy) removeLunacy(player, cost);
        else removeThread(player, cost);

        playShopEffect(chestLoc);

        for (ItemStack it : goods) {
            Map<Integer, ItemStack> overflow = player.getInventory().addItem(it);
            for (ItemStack left : overflow.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), left);
            }
        }

        String boxName = shopMgr.getName(chestLoc);
        String msgColor = useLunacy ? "&#FF0000" : "&#FFE5A0";
        player.sendMessage(plugin.color("&#9BE1FF[商店] " + msgColor + boxName + " &#AAAAAA已購入 &#FFFFFF" + goods.size() + " &#AAAAAA件商品(消耗 " + cost + " " + currencyName + ")"));
    }

    /** 取出箱內所有商品的複本,不改變原箱內容。 */
    private List<ItemStack> collectShopGoods(org.bukkit.block.Block block) {
        List<ItemStack> out = new ArrayList<>();
        if (!(block.getState() instanceof org.bukkit.block.Container container)) return out;
        for (ItemStack it : container.getInventory().getContents()) {
            if (it != null && !it.getType().isAir()) out.add(it.clone());
        }
        return out;
    }

    private void playShopEffect(Location loc) {
        Location center = loc.clone().add(0.5, 1.0, 0.5);
        loc.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, center, 12, 0.3, 0.3, 0.3, 0.01);
        loc.getWorld().playSound(center, Sound.ENTITY_VILLAGER_YES, 0.7f, 1.2f);
    }

    /** 從箱子 inventory 依疊總數加權抽一件，回傳數量 1 的複本（箱內不減）。 */
    private ItemStack drawFromChest(org.bukkit.block.Block block) {
        if (!(block.getState() instanceof org.bukkit.block.Container container)) return null;
        List<ItemStack> slots = new ArrayList<>();
        int totalWeight = 0;
        for (ItemStack it : container.getInventory().getContents()) {
            if (it != null && !it.getType().isAir()) {
                slots.add(it);
                totalWeight += it.getAmount();
            }
        }
        if (slots.isEmpty() || totalWeight <= 0) return null;

        int roll = rng.nextInt(totalWeight);
        for (ItemStack it : slots) {
            roll -= it.getAmount();
            if (roll < 0) {
                ItemStack prize = it.clone();
                prize.setAmount(1);
                return prize;
            }
        }
        return null; // 理論上不會到這
    }

    private void playThreadEffect(Location loc) {
        Location center = loc.clone().add(0.5, 1.0, 0.5);
        // 低調白光：少量 END_ROD，無音效
        loc.getWorld().spawnParticle(Particle.END_ROD, center, 12, 0.25, 0.3, 0.25, 0.01);
    }

    // ── 抽取邏輯 ──────────────────────────────────────────────────────────────

    private Accessory draw(Player player) {
        int tier = pickTier();
        List<Accessory> pool = plugin.getAllAccessories().stream()
            .filter(a -> plugin.getTier(a.getId()) == tier && !plugin.isVestige(a.getId()))
            .collect(Collectors.toList());

        if (pool.isEmpty()) return null;

        Accessory result = pool.get(rng.nextInt(pool.size()));

        // 抽到重複（背包或飾品欄已持有）→ 改給對應等級殘影
        if (playerHasInInventory(player, result.getId()) || playerHasEquipped(player, result.getId())) {
            return getVestigeForTier(tier);
        }

        return result;
    }

    private int pickTier() {
        double roll = rng.nextDouble();
        if (roll < tier4Chance) return 4;
        roll -= tier4Chance;
        double each = (1.0 - tier4Chance) / 3.0;
        if (roll < each) return 1;
        if (roll < each * 2) return 2;
        return 3;
    }

    private Accessory getVestigeForTier(int tier) {
        String id = switch (tier) {
            case 1 -> "dark_vestige";
            case 2 -> "faint_vestige";
            case 3 -> "twinkling_vestige";
            default -> "brilliant_vestige";
        };
        return plugin.getAccessory(id);
    }

    // ── 背包與飾品欄掃描 ──────────────────────────────────────────────────────

    private boolean playerHasInInventory(Player player, String id) {
        for (ItemStack i : player.getInventory().getContents()) {
            if (id.equals(plugin.getAccessoryId(i))) return true;
        }
        return false;
    }

    private boolean playerHasEquipped(Player player, String id) {
        return plugin.getEquippedAccessories(player).stream().anyMatch(a -> a.getId().equals(id));
    }

    // ── 粒子效果 ─────────────────────────────────────────────────────────────

    private void playDrawEffect(Location loc, boolean tierIV) {
        Location center = loc.clone().add(0.5, 1.0, 0.5);
        Particle particle = tierIV ? Particle.END_ROD : Particle.SNOWFLAKE;
        loc.getWorld().spawnParticle(particle, center, 40, 0.4, 0.4, 0.4, 0.05);
        loc.getWorld().playSound(center, tierIV ? Sound.UI_TOAST_CHALLENGE_COMPLETE : Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
    }
}
