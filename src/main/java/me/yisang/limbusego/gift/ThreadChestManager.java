package me.yisang.limbusego.gift;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * 自訂抽獎箱管理器。每個箱子可由管理員自訂「名稱」「每抽消耗量」和「貨幣類型」（紡錘／狂氣）。
 * 獎池即為箱子本身的 inventory（無限池：抽出複本，箱內不減；依疊總數加權）。
 */
public class ThreadChestManager {

    private final GiftsModule plugin;
    private final File dataFile;
    private YamlConfiguration config;
    private final Map<String, UUID> textDisplays = new HashMap<>();
    // chunk key ("world:cx:cz") → 該 chunk 內的箱子位置。懸浮字為非持久實體，
    // chunk 卸載即消失，靠 ChunkLoadEvent 查此索引補生。
    private final Map<String, List<Location>> byChunk = new HashMap<>();

    public ThreadChestManager(GiftsModule plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "thread_chests.yml");
        load();
    }

    // ── 持久化 ────────────────────────────────────────────────────────────────

    private void load() {
        if (!dataFile.exists()) {
            plugin.getDataFolder().mkdirs();
            config = new YamlConfiguration();
            rebuildChunkIndex();
            return;
        }
        config = YamlConfiguration.loadConfiguration(dataFile);
        rebuildChunkIndex();
        Bukkit.getScheduler().runTaskLater(plugin.getPlugin(), this::respawnAllDisplays, 40L);
    }

    public void save() {
        try { config.save(dataFile); } catch (IOException e) {
            plugin.getLogger().severe("無法儲存 thread_chests.yml: " + e.getMessage());
        }
    }

    // ── 登記 / 移除 ──────────────────────────────────────────────────────────

    public boolean register(Location loc, String name, int cost, String currency) {
        String key = locKey(loc);
        if (config.contains("chests." + key)) return false;
        config.set("chests." + key + ".world", loc.getWorld().getName());
        config.set("chests." + key + ".x", loc.getBlockX());
        config.set("chests." + key + ".y", loc.getBlockY());
        config.set("chests." + key + ".z", loc.getBlockZ());
        config.set("chests." + key + ".name", name);
        config.set("chests." + key + ".cost", cost);
        config.set("chests." + key + ".currency", currency);
        save();
        rebuildChunkIndex();
        spawnDisplay(loc);
        return true;
    }

    public boolean unregister(Location loc) {
        String key = locKey(loc);
        if (!config.contains("chests." + key)) return false;
        config.set("chests." + key, null);
        save();
        rebuildChunkIndex();
        removeDisplay(key);
        return true;
    }

    public boolean isThreadChest(Location loc) {
        return config.contains("chests." + locKey(loc));
    }

    public String getName(Location loc) {
        return config.getString("chests." + locKey(loc) + ".name", "紡錘抽獎");
    }

    public int getCost(Location loc) {
        return config.getInt("chests." + locKey(loc) + ".cost", 1);
    }

    /** 取得箱子使用的貨幣類型："thread" 或 "lunacy"，預設 "thread"（向下相容）。 */
    public String getCurrency(Location loc) {
        return config.getString("chests." + locKey(loc) + ".currency", "thread");
    }

    public Set<Location> getAllLocations() {
        Set<Location> result = new HashSet<>();
        if (!config.contains("chests")) return result;
        for (String key : config.getConfigurationSection("chests").getKeys(false)) {
            Location loc = keyToLoc(key);
            if (loc != null) result.add(loc);
        }
        return result;
    }

    // ── TextDisplay 懸浮文字（顯示自訂名稱）─────────────────────────────────────

    private void spawnDisplay(Location loc) {
        String key = locKey(loc);
        removeDisplay(key);
        Location above = loc.clone().add(0.5, 1.4, 0.5);
        String name = getName(loc);
        String currency = getCurrency(loc);
        String currencyColor = currency.equals("lunacy") ? "&#FF0000" : "&#FFE5A0";
        TextDisplay td = loc.getWorld().spawn(above, TextDisplay.class, e -> {
            e.setText(plugin.color("&#FFFFFF[" + currencyColor + name + "&#FFFFFF]"));
            e.setBillboard(org.bukkit.entity.Display.Billboard.CENTER);
            e.setTransformation(new Transformation(
                new Vector3f(0, 0, 0),
                new AxisAngle4f(0, 0, 0, 1),
                new Vector3f(1.2f, 1.2f, 1.2f),
                new AxisAngle4f(0, 0, 0, 1)
            ));
            e.setPersistent(false);
        });
        textDisplays.put(key, td.getUniqueId());
    }

    private void removeDisplay(String key) {
        UUID uid = textDisplays.remove(key);
        if (uid == null) return;
        Entity e = Bukkit.getEntity(uid);
        if (e != null) e.remove();
    }

    private void respawnAllDisplays() {
        for (Location loc : getAllLocations()) spawnDisplay(loc);
    }

    // ── Chunk 索引（供 ChunkLoadEvent 補生懸浮字）─────────────────────────────

    private static String chunkKey(String world, int cx, int cz) {
        return world + ":" + cx + ":" + cz;
    }

    private void rebuildChunkIndex() {
        byChunk.clear();
        for (Location loc : getAllLocations()) {
            String key = chunkKey(loc.getWorld().getName(), loc.getBlockX() >> 4, loc.getBlockZ() >> 4);
            byChunk.computeIfAbsent(key, k -> new ArrayList<>()).add(loc);
        }
    }

    /** chunk 載入時補生該 chunk 內的懸浮字。無箱子的 chunk 只做一次 map 查詢。 */
    public void respawnDisplaysInChunk(org.bukkit.Chunk chunk) {
        List<Location> locs = byChunk.get(chunkKey(chunk.getWorld().getName(), chunk.getX(), chunk.getZ()));
        if (locs == null) return;
        // 延後 1 tick，避免在 chunk 載入事件中直接生成實體
        Bukkit.getScheduler().runTask(plugin.getPlugin(), () -> {
            for (Location loc : locs) spawnDisplay(loc);
        });
    }

    // ── 工具 ─────────────────────────────────────────────────────────────────

    private String locKey(Location loc) {
        return loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
    }

    private Location keyToLoc(String key) {
        String[] p = key.split(",");
        if (p.length != 4) return null;
        World w = Bukkit.getWorld(p[0]);
        if (w == null) return null;
        try {
            return new Location(w, Integer.parseInt(p[1]), Integer.parseInt(p[2]), Integer.parseInt(p[3]));
        } catch (NumberFormatException e) { return null; }
    }

    public void reload() {
        onDisable();
        textDisplays.clear();
        load();
    }

    public void onDisable() {
        textDisplays.forEach((k, uid) -> {
            Entity e = Bukkit.getEntity(uid);
            if (e != null) e.remove();
        });
    }
}
