package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
public class GreenSpirit extends BaseAccessory {
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private static final long CD = 30_000L;
    public GreenSpirit(GiftsModule plugin) {
        super(plugin, "green_spirit", "綠光果實", "&7右鍵：速度 II + 力量 I 5 秒，之後暈眩（冷卻 30 秒）");
    }
    @Override public void onInteract(PlayerInteractEvent event, Player player) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        long now = System.currentTimeMillis();
        if (cooldowns.getOrDefault(player.getUniqueId(), 0L) > now) return;
        cooldowns.put(player.getUniqueId(), now + CD);
        double m = plugin.getUpgradeMultiplier(player, getId());
        int ticks = (int)(100 * m);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, ticks, 1, true, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, ticks, 0, true, true));
        Bukkit.getScheduler().runTaskLater(plugin.getPlugin(), () -> {
            if (player.isOnline())
                player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 60, 0, true, true));
        }, (long) ticks);
    }
    @Override public void onQuit(Player player) { cooldowns.remove(player.getUniqueId()); }
}
