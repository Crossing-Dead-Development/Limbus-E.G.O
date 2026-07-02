package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
public class Sunshower extends BaseAccessory {
    public Sunshower(GiftsModule plugin) {
        super(plugin, "sunshower", "狐雨", "&7雨天回血；晴天速度提升");
    }
    @Override public void onPassiveTick(Player player) {
        double m = plugin.getUpgradeMultiplier(player, getId());
        boolean raining = player.getWorld().hasStorm() || player.getWorld().isThundering();
        if (raining)
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 30, (int)(m - 1), true, false));
        else
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 30, (int)(m - 1), true, false));
    }
}
