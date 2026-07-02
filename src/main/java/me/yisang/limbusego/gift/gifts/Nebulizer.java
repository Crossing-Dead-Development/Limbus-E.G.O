package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
public class Nebulizer extends BaseAccessory {
    public Nebulizer(GiftsModule plugin) {
        super(plugin, "nebulizer", "霧化吸入器", "&7被動：4 格內敵人持續中毒");
    }
    @Override public void onPassiveTick(Player player) {
        double m = plugin.getUpgradeMultiplier(player, getId());
        int radius = (int)(4 * m);
        player.getNearbyEntities(radius, radius, radius).forEach(e -> {
            if (e instanceof LivingEntity le && !(e instanceof Player))
                le.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 40, 0, true, false));
        });
    }
}
