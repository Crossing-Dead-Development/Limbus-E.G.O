package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
public class TheBookOfVengeance extends BaseAccessory {
    private final Map<UUID, Long> lastSlow = new HashMap<>();
    public TheBookOfVengeance(GiftsModule plugin) {
        super(plugin, "the_book_of_vengeance", "復仇帳簿",
                "&#B900FF", "加倍奉還！",
                "被動：生命再生 I；每 30 秒受緩慢 I 持續 1 秒");
    }
    @Override public void onPassiveTick(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 30, 0, true, false));
        long now = System.currentTimeMillis();
        if (now - lastSlow.getOrDefault(player.getUniqueId(), 0L) >= 30_000L) {
            lastSlow.put(player.getUniqueId(), now);
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20, 0, true, true));
        }
    }
    @Override public void onQuit(Player player) { lastSlow.remove(player.getUniqueId()); }
}
