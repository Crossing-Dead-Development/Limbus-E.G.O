package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
public class SomeonesDevice extends BaseAccessory {
    public SomeonesDevice(GiftsModule plugin) {
        super(plugin, "someones_device", "某人的裝置", "&7被動：ActionBar 顯示附近生物數量");
    }
    @Override public void onPassiveTick(Player player) {
        double m = plugin.getUpgradeMultiplier(player, getId());
        int radius = (int)(16 * m);
        long count = player.getNearbyEntities(radius, radius, radius).stream()
            .filter(e -> e instanceof LivingEntity && !(e instanceof Player)).count();
        player.sendActionBar(plugin.color("&#888888[裝置] 偵測到 &f" + count + " &#888888個生物"));
    }
}
