package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
public class Oracle extends BaseAccessory {
    public Oracle(GiftsModule plugin) {
        super(plugin, "oracle", "神諭",
                "&#B3F2F9", "舉刀對笑，如落葉之香氣般對瀑布哭泣。",
                "潛行時：每 10 秒使周圍生物發光 3 秒");
    }
    @Override public void onPassiveTick(Player player) {
        if (!player.isSneaking() || !gate(player, 10_000)) return;
        double m = plugin.getUpgradeMultiplier(player, getId());
        int radius = (int) Math.round(12 * m);
        for (Entity e : player.getNearbyEntities(radius, radius, radius)) {
            if (e instanceof LivingEntity living && !(e instanceof Player)) {
                living.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 60, 0, true, false));
            }
        }
    }
}
