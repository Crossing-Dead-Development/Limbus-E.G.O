package me.yisang.limbusego.gift.lang;

import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class LangManager {
    private static final String DEFAULT_LANG = "zh_TW";
    private static final Set<String> BUILT_IN = new LinkedHashSet<>(List.of("zh_TW", "en_US"));

    private final GiftsModule plugin;
    private String currentLang = DEFAULT_LANG;
    private YamlConfiguration messages = new YamlConfiguration();
    private YamlConfiguration fallback = new YamlConfiguration();

    public LangManager(GiftsModule plugin) {
        this.plugin = plugin;
    }

    public void load() {
        saveDefaultConfigAndLangFiles();
        plugin.reloadConfig();
        String lang = plugin.getConfig().getString("language", DEFAULT_LANG);
        loadLang(lang);
    }

    public String getCurrentLang() { return currentLang; }

    public List<String> getAvailableLangs() {
        Set<String> langs = new LinkedHashSet<>(BUILT_IN);
        File langDir = new File(plugin.getDataFolder(), "lang/gifts");
        if (langDir.isDirectory()) {
            File[] files = langDir.listFiles((d, n) -> n.endsWith(".yml"));
            if (files != null) {
                for (File f : files) {
                    String name = f.getName();
                    langs.add(name.substring(0, name.length() - 4));
                }
            }
        }
        return new ArrayList<>(langs);
    }

    public boolean setLanguage(String lang) {
        if (!hasLang(lang)) return false;
        plugin.getConfig().set("language", lang);
        plugin.saveConfig();
        loadLang(lang);
        return true;
    }

    public boolean hasLang(String lang) {
        if (BUILT_IN.contains(lang)) return true;
        File f = new File(plugin.getDataFolder(), "lang/gifts/" + lang + ".yml");
        return f.isFile();
    }

    public void reload() {
        plugin.reloadConfig();
        String lang = plugin.getConfig().getString("language", DEFAULT_LANG);
        loadLang(lang);
    }

    private void loadLang(String lang) {
        this.fallback = readLang(DEFAULT_LANG);
        if (lang == null || lang.isEmpty()) lang = DEFAULT_LANG;
        this.messages = readLang(lang);
        this.currentLang = lang;
        plugin.getLogger().info("[Lang] Loaded language: " + lang);
    }

    private YamlConfiguration readLang(String lang) {
        File file = new File(plugin.getDataFolder(), "lang/gifts/" + lang + ".yml");
        if (file.isFile()) {
            try {
                return YamlConfiguration.loadConfiguration(
                        new InputStreamReader(Files.newInputStream(file.toPath()), StandardCharsets.UTF_8));
            } catch (IOException e) {
                plugin.getLogger().warning("[Lang] Failed to read " + file.getName() + ": " + e.getMessage());
            }
        }
        try (InputStream in = plugin.getResource("lang/gifts/" + lang + ".yml")) {
            if (in != null) {
                return YamlConfiguration.loadConfiguration(new InputStreamReader(in, StandardCharsets.UTF_8));
            }
        } catch (IOException ignored) {}
        return new YamlConfiguration();
    }

    private void saveDefaultConfigAndLangFiles() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.isFile()) plugin.saveResource("config.yml", false);
        for (String lang : BUILT_IN) {
            File out = new File(plugin.getDataFolder(), "lang/gifts/" + lang + ".yml");
            if (!out.isFile()) {
                try {
                    plugin.saveResource("lang/gifts/" + lang + ".yml", false);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("[Lang] Missing bundled resource lang/" + lang + ".yml");
                }
            }
        }
    }

    /** 有 key 就回值；沒有回 null（用來判斷是否有 lang 覆蓋）。 */
    public String getOrNull(String key) {
        String v = messages.getString(key);
        if (v == null) v = fallback.getString(key);
        return v;
    }

    public String get(String key) {
        String v = getOrNull(key);
        return v == null ? key : v;
    }

    public String get(String key, Object... args) {
        String base = get(key);
        for (int i = 0; i < args.length; i++) {
            base = base.replace("{" + i + "}", String.valueOf(args[i]));
        }
        return base;
    }

    public List<String> getListOrNull(String key) {
        if (messages.isList(key)) return messages.getStringList(key);
        if (fallback.isList(key)) return fallback.getStringList(key);
        return null;
    }

    public List<String> getList(String key) {
        List<String> v = getListOrNull(key);
        return v == null ? Collections.emptyList() : v;
    }
}
