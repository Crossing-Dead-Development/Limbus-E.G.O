package me.yisang.limbusego;

import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** /limbusego 統一指令樹：weapon | gift | chest | reload | language，含全層 Tab 補完。 */
public class CommandRouter implements TabExecutor {

    private final LimbusEGO plugin;

    private static final List<String> ROOT = List.of("weapon", "gift", "chest", "reload", "language");
    private static final List<String> WEAPON_SUB = List.of("give", "catalog", "admin");
    private static final List<String> GIFT_SUB = List.of("menu", "give", "category");
    private static final List<String> CHEST_SUB = List.of("gacha", "thread", "shop");
    private static final List<String> SET_REMOVE = List.of("set", "remove");
    private static final List<String> CURRENCIES = List.of("thread", "lunacy");

    public CommandRouter(LimbusEGO plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        GiftsModule gifts = plugin.getGifts();
        if (args.length == 0) { sender.sendMessage(plugin.msg("cmd.usage_root")); return true; }
        String[] rest = Arrays.copyOfRange(args, 1, args.length);
        switch (args[0].toLowerCase()) {
            case "weapon" -> plugin.handleWeaponCommand(sender, rest);
            case "gift" -> {
                if (rest.length == 0) { sender.sendMessage(plugin.msg("cmd.usage_root")); return true; }
                String[] rest2 = Arrays.copyOfRange(rest, 1, rest.length);
                switch (rest[0].toLowerCase()) {
                    case "menu" -> { if (sender instanceof Player p) gifts.openMenu(p); }
                    case "give" -> gifts.onGetGift(sender, cmd, label, rest2);
                    case "category" -> gifts.onEgoGift(sender, cmd, label, new String[]{"category"});
                    default -> sender.sendMessage(plugin.msg("cmd.usage_root"));
                }
            }
            case "chest" -> {
                if (rest.length == 0) { sender.sendMessage(plugin.msg("cmd.usage_root")); return true; }
                String[] rest2 = Arrays.copyOfRange(rest, 1, rest.length);
                switch (rest[0].toLowerCase()) {
                    case "gacha" -> gifts.onGachaChest(sender, cmd, label, rest2);
                    case "thread" -> gifts.onThreadChest(sender, cmd, label, rest2);
                    case "shop" -> gifts.onShopChest(sender, cmd, label, rest2);
                    default -> sender.sendMessage(plugin.msg("cmd.usage_root"));
                }
            }
            case "reload" -> {
                if (!sender.hasPermission("limbus.admin") && !(sender instanceof ConsoleCommandSender)) return true;
                plugin.getLang().reload();
                gifts.getLang().reload();
                sender.sendMessage(plugin.msg("cmd.reload_success"));
            }
            case "language", "lang" -> {
                if (!sender.hasPermission("limbus.admin") && !(sender instanceof ConsoleCommandSender)) return true;
                if (rest.length == 0) {
                    sender.sendMessage(plugin.msg("cmd.language_current", plugin.getLang().getCurrentLang()));
                    return true;
                }
                String code = rest[0];
                if (!plugin.getLang().hasLang(code)) {
                    sender.sendMessage(plugin.msg("cmd.language_invalid", code, String.join(", ", plugin.getLang().getAvailableLangs())));
                    return true;
                }
                plugin.getLang().setLanguage(code);
                gifts.getLang().setLanguage(code);
                sender.sendMessage(plugin.msg("cmd.language_set", code));
            }
            default -> sender.sendMessage(plugin.msg("cmd.usage_root"));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        GiftsModule gifts = plugin.getGifts();
        if (args.length == 1) return filter(ROOT, args[0]);
        switch (args[0].toLowerCase()) {
            case "weapon" -> {
                if (args.length == 2) {
                    List<String> opts = new ArrayList<>(WEAPON_SUB);
                    opts.addAll(plugin.getWeaponGiveIds());
                    return filter(opts, args[1]);
                }
                if ("give".equalsIgnoreCase(args[1])) {
                    if (args.length == 3) return null; // null = 交給 Bukkit 補完線上玩家名
                    if (args.length == 4) return filter(plugin.getWeaponGiveIds(), args[3]);
                }
            }
            case "gift" -> {
                if (args.length == 2) return filter(GIFT_SUB, args[1]);
                if (args.length == 3 && "give".equalsIgnoreCase(args[1])) return filter(giftIds(gifts), args[2]);
            }
            case "chest" -> {
                if (args.length == 2) return filter(CHEST_SUB, args[1]);
                if (args.length == 3) return filter(SET_REMOVE, args[2]);
                if (args.length == 5 && "set".equalsIgnoreCase(args[2])
                        && ("thread".equalsIgnoreCase(args[1]) || "shop".equalsIgnoreCase(args[1]))) {
                    return filter(CURRENCIES, args[4]);
                }
            }
            case "language", "lang" -> {
                if (args.length == 2) return filter(plugin.getLang().getAvailableLangs(), args[1]);
            }
        }
        return List.of();
    }

    private static List<String> giftIds(GiftsModule gifts) {
        List<String> ids = new ArrayList<>();
        for (var acc : gifts.getAllAccessories()) ids.add(acc.getId());
        return ids;
    }

    private static List<String> filter(List<String> options, String prefix) {
        String p = prefix.toLowerCase();
        return options.stream().filter(s -> s.toLowerCase().startsWith(p)).sorted().toList();
    }
}
