package me.yisang.limbusego;

import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** /limbusego 統一指令樹：weapon | gift | chest | status | reload | language，含全層 Tab 補完。 */
public class CommandRouter implements TabExecutor {

    private final LimbusEGO plugin;

    private static final List<String> ROOT = List.of("weapon", "gift", "chest", "status", "reload", "language");
    private static final List<String> STATUS_SUB = List.of("apply", "get");
    private static final List<String> WEAPON_SUB = List.of("give", "catalog", "admin");
    private static final List<String> GIFT_SUB = List.of("menu", "give", "category", "admin");
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
                    case "give" -> {
                        // C1：/getgift 舊版靠 plugin.yml permission 整條擋，新版 limbusego 無宣告，
                        // 內層分支（menu / 自給飾品 id）本身沒有權限檢查，需在此補一道閘門。
                        if (!hasGiftGivePermission(sender)) return true;
                        // 相容舊 /getgift 兩種語法：/gift give <玩家> <id|menu|thread|lunacy> [n]（給人，需 args[0]=="give"）
                        // 與 /gift give <id>（自給）。用 rest2[0] 是否為線上玩家名判斷要不要補回 "give" 前綴。
                        boolean handled;
                        if (rest2.length >= 2 && Bukkit.getPlayerExact(rest2[0]) != null) {
                            String[] giveArgs = new String[rest2.length + 1];
                            giveArgs[0] = "give";
                            System.arraycopy(rest2, 0, giveArgs, 1, rest2.length);
                            handled = gifts.onGetGift(sender, cmd, label, giveArgs);
                        } else {
                            handled = gifts.onGetGift(sender, cmd, label, rest2);
                        }
                        // handler 回 false = 參數不足/未知子指令，不能靜默吞掉
                        if (!handled) sender.sendMessage(plugin.msg("cmd.usage_root"));
                    }
                    case "category" -> { if (sender instanceof Player p) gifts.openCatalog(p); }
                    case "admin" -> {
                        // 與 weapon admin 對齊的正式入口（舊 gift give admin 路徑仍相容）
                        if (sender instanceof Player p && hasGiftGivePermission(sender)) gifts.openAdminGUI(p);
                    }
                    default -> sender.sendMessage(plugin.msg("cmd.usage_root"));
                }
            }
            case "chest" -> {
                if (rest.length == 0) { sender.sendMessage(plugin.msg("cmd.usage_root")); return true; }
                String[] rest2 = Arrays.copyOfRange(rest, 1, rest.length);
                // handler 回 false = 參數不足/未知子指令，統一回 usage 而非靜默
                boolean handled = switch (rest[0].toLowerCase()) {
                    case "gacha" -> gifts.onGachaChest(sender, cmd, label, rest2);
                    case "thread" -> gifts.onThreadChest(sender, cmd, label, rest2);
                    case "shop" -> gifts.onShopChest(sender, cmd, label, rest2);
                    default -> false;
                };
                if (!handled) sender.sendMessage(plugin.msg("cmd.usage_root"));
            }
            case "status" -> {
                if (!sender.hasPermission("limbus.admin") && !(sender instanceof ConsoleCommandSender)) return true;
                handleStatusCommand(sender, rest);
            }
            case "reload" -> {
                if (!sender.hasPermission("limbus.admin") && !(sender instanceof ConsoleCommandSender)) return true;
                plugin.getLang().reload();
                if (gifts != null && gifts.getLang() != null) gifts.getLang().reload();
                sender.sendMessage(plugin.msg("cmd.reload_success"));
            }
            case "language", "lang" -> {
                if (!sender.hasPermission("limbus.admin") && !(sender instanceof ConsoleCommandSender)) return true;
                if (rest.length == 0) {
                    sender.sendMessage(plugin.msg("cmd.language_current", plugin.getLang().getCurrentLang()));
                    sender.sendMessage(plugin.msg("cmd.usage_language"));
                    return true;
                }
                String code = rest[0];
                if (!plugin.getLang().hasLang(code)) {
                    sender.sendMessage(plugin.msg("cmd.language_invalid", code, String.join(", ", plugin.getLang().getAvailableLangs())));
                    return true;
                }
                plugin.getLang().setLanguage(code);
                if (gifts != null && gifts.getLang() != null) gifts.getLang().setLanguage(code);
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
                // C1：gift give 需要 limbus.admin，無權限者的補完不應洩漏候選清單
                if (args.length == 2) {
                    // admin/give 只對有權限者補完
                    List<String> opts = hasGiftGivePermission(sender)
                            ? GIFT_SUB : List.of("menu", "category");
                    return filter(opts, args[1]);
                }
                if (!hasGiftGivePermission(sender)) return List.of();
                if (args.length == 3 && "give".equalsIgnoreCase(args[1])) {
                    // 第 3 參可能是「自給的飾品 id」或「給人語法的玩家名」，兩種候選合併補完
                    List<String> opts = new ArrayList<>(giftIds(gifts));
                    for (Player p : Bukkit.getOnlinePlayers()) opts.add(p.getName());
                    return filter(opts, args[2]);
                }
                if (args.length == 4 && "give".equalsIgnoreCase(args[1]) && Bukkit.getPlayerExact(args[2]) != null) {
                    List<String> opts = new ArrayList<>(giftIds(gifts));
                    opts.addAll(CURRENCIES);
                    opts.add("menu");
                    return filter(opts, args[3]);
                }
            }
            case "chest" -> {
                if (args.length == 2) return filter(CHEST_SUB, args[1]);
                if (args.length == 3) return filter(SET_REMOVE, args[2]);
                if (args.length == 5 && "set".equalsIgnoreCase(args[2])
                        && ("thread".equalsIgnoreCase(args[1]) || "shop".equalsIgnoreCase(args[1]))) {
                    return filter(CURRENCIES, args[4]);
                }
            }
            case "status" -> {
                if (!sender.hasPermission("limbus.admin") && !(sender instanceof ConsoleCommandSender)) return List.of();
                if (args.length == 2) return filter(STATUS_SUB, args[1]);
                if (args.length == 3) return null; // 交給 Bukkit 補完線上玩家名
                if (args.length == 4 && "apply".equalsIgnoreCase(args[1])) {
                    List<String> effects = new ArrayList<>();
                    for (var e : me.yisang.limbusego.status.StatusEffect.values()) effects.add(e.name().toLowerCase());
                    return filter(effects, args[3]);
                }
            }
            case "language", "lang" -> {
                if (args.length == 2) return filter(plugin.getLang().getAvailableLangs(), args[1]);
            }
        }
        return List.of();
    }

    /**
     * /limbusego status apply <選擇器|玩家名> <效果> <potency> <count>
     * /limbusego status get <選擇器|玩家名>
     * 管理/除錯用：從控制台或遊戲內直接施加/查詢狀態（走正常 apply 路徑，含冷卻與上限）。
     */
    private void handleStatusCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§7用法: /limbusego status apply <目標> <效果> <potency> <count> | status get <目標>");
            return;
        }
        List<org.bukkit.entity.Entity> targets;
        try {
            targets = Bukkit.selectEntities(sender, args[1]);
        } catch (IllegalArgumentException ex) {
            sender.sendMessage("§c無效的目標選擇器: " + args[1]);
            return;
        }
        if (targets.isEmpty()) { sender.sendMessage("§c找不到目標: " + args[1]); return; }

        switch (args[0].toLowerCase()) {
            case "apply" -> {
                if (args.length < 5) { sender.sendMessage("§7用法: /limbusego status apply <目標> <效果> <potency> <count>"); return; }
                me.yisang.limbusego.status.StatusEffect eff;
                try {
                    eff = me.yisang.limbusego.status.StatusEffect.valueOf(args[2].toUpperCase());
                } catch (IllegalArgumentException ex) {
                    sender.sendMessage("§c未知效果: " + args[2]);
                    return;
                }
                int potency, count;
                try {
                    potency = Integer.parseInt(args[3]);
                    count = Integer.parseInt(args[4]);
                } catch (NumberFormatException ex) {
                    sender.sendMessage("§cpotency/count 必須是整數");
                    return;
                }
                int applied = 0;
                for (org.bukkit.entity.Entity e : targets) {
                    if (e instanceof org.bukkit.entity.LivingEntity le) {
                        plugin.getStatusManager().apply(le, eff, potency, count,
                                sender instanceof Player p ? p : null);
                        applied++;
                    }
                }
                sender.sendMessage("§a已對 " + applied + " 個目標施加 " + eff.name() + " " + potency + "·" + count + "（冷卻中的施加會被忽略）");
            }
            case "get" -> {
                for (org.bukkit.entity.Entity e : targets) {
                    if (!(e instanceof org.bukkit.entity.LivingEntity le)) continue;
                    var state = plugin.getStatusManager().get(le);
                    StringBuilder sb = new StringBuilder("§b" + e.getName() + " §7[" + e.getUniqueId().toString().substring(0, 8) + "]§f: ");
                    if (state == null || state.isEmpty()) {
                        sb.append("§7(無狀態)");
                    } else {
                        for (var en : state.snapshot().entrySet()) {
                            sb.append(en.getKey().color).append(en.getKey().name())
                              .append(" ").append(en.getValue()[0]).append("·").append(en.getValue()[1]).append("§f ");
                        }
                    }
                    sender.sendMessage(sb.toString());
                }
            }
            default -> sender.sendMessage("§7用法: /limbusego status apply <目標> <效果> <potency> <count> | status get <目標>");
        }
    }

    /** C1：/limbusego gift give 的權限判斷，指令分派與 Tab 補完共用同一條件。 */
    private static boolean hasGiftGivePermission(CommandSender sender) {
        return sender.hasPermission("limbus.admin") || sender instanceof ConsoleCommandSender
                || (sender instanceof Player pp && pp.isOp());
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
