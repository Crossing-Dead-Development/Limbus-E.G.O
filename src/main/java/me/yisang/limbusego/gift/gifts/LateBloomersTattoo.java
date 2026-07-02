package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
public class LateBloomersTattoo extends BaseAccessory {
    public LateBloomersTattoo(GiftsModule plugin) {
        super(plugin, "late_bloomers_tattoo", "刺青：大器晚成", "&7HP 越低攻防越強");
    }
    @Override public void onPassiveTick(Player player) {
        double m = plugin.getUpgradeMultiplier(player, getId());
        double ratio = player.getHealth() / player.getMaxHealth();
        if (ratio < 0.3 * m) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 30, 1, true, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 30, 0, true, false));
        } else if (ratio < 0.6 * m) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 30, 0, true, false));
        }
    }
}
