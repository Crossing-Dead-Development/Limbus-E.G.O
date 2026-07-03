package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
public class SomeonesDevice extends BaseAccessory {
    public SomeonesDevice(GiftsModule plugin) {
        super(plugin, "someones_device", "某人的裝置", "&7被動：吸引附近掉落物與經驗球");
    }
    @Override public void onPassiveTick(Player player) {
        double m = plugin.getUpgradeMultiplier(player, getId());
        int radius = (int) Math.round(6 * m);
        for (org.bukkit.entity.Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof Item || entity instanceof ExperienceOrb) {
                entity.teleport(player.getLocation());
            }
        }
    }
}
