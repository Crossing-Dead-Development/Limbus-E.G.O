package me.yisang.limbusego.lang;

import me.yisang.limbusego.LimbusEGO;
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

    private final LimbusEGO plugin;
    private String currentLang = DEFAULT_LANG;
    private YamlConfiguration messages = new YamlConfiguration();
    private YamlConfiguration fallback = new YamlConfiguration();

    public LangManager(LimbusEGO plugin) {
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
        File langDir = new File(plugin.getDataFolder(), "lang/weapons");
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
        File f = new File(plugin.getDataFolder(), "lang/weapons/" + lang + ".yml");
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
        File file = new File(plugin.getDataFolder(), "lang/weapons/" + lang + ".yml");
        if (file.isFile()) {
            try {
                return YamlConfiguration.loadConfiguration(
                        new InputStreamReader(Files.newInputStream(file.toPath()), StandardCharsets.UTF_8));
            } catch (IOException e) {
                plugin.getLogger().warning("[Lang] Failed to read " + file.getName() + ": " + e.getMessage());
            }
        }
        // Fallback to bundled resource
        try (InputStream in = plugin.getResource("lang/weapons/" + lang + ".yml")) {
            if (in != null) {
                return YamlConfiguration.loadConfiguration(new InputStreamReader(in, StandardCharsets.UTF_8));
            }
        } catch (IOException ignored) {}
        return new YamlConfiguration();
    }

    private void saveDefaultConfigAndLangFiles() {
        // config.yml
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.isFile()) plugin.saveResource("config.yml", false);
        // lang/*.yml
        for (String lang : BUILT_IN) {
            File out = new File(plugin.getDataFolder(), "lang/weapons/" + lang + ".yml");
            if (!out.isFile()) {
                try {
                    plugin.saveResource("lang/weapons/" + lang + ".yml", false);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("[Lang] Missing bundled resource lang/weapons/" + lang + ".yml");
                }
            }
        }
    }

    public String get(String key) {
        String v = messages.getString(key);
        if (v == null) v = fallback.getString(key);
        return v == null ? key : v;
    }

    public String get(String key, Object... args) {
        String base = get(key);
        for (int i = 0; i < args.length; i++) {
            base = base.replace("{" + i + "}", String.valueOf(args[i]));
        }
        return base;
    }

    public List<String> getList(String key) {
        List<String> v = messages.getStringList(key);
        if (v == null || v.isEmpty()) v = fallback.getStringList(key);
        if (v == null) return Collections.emptyList();
        return v;
    }

    public List<String> getList(String key, Object... args) {
        List<String> raw = getList(key);
        List<String> out = new ArrayList<>(raw.size());
        for (String line : raw) {
            for (int i = 0; i < args.length; i++) {
                line = line.replace("{" + i + "}", String.valueOf(args[i]));
            }
            out.add(line);
        }
        return out;
    }
}
