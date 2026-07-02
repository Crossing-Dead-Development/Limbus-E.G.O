package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.entity.Player;
public class ArtisticSense extends BaseAccessory {
    public ArtisticSense(GiftsModule plugin) {
        super(plugin, "artistic_sense", "美感", "&7附近有玩家或命名生物時發出感知提示");
    }
    @Override public void onPassiveTick(Player player) {
        double m = plugin.getUpgradeMultiplier(player, getId());
        int radius = (int)(16 * m);
        boolean detected = player.getNearbyEntities(radius, radius, radius).stream().anyMatch(e ->
            (e instanceof Player && !e.equals(player)) ||
            (e instanceof LivingEntity le && !(e instanceof Player) && le.customName() != null)
        );
        if (detected)
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.4f, 1.5f);
    }
}
