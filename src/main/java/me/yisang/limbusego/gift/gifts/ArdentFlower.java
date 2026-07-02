package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
public class ArdentFlower extends BaseAccessory {
    public ArdentFlower(GiftsModule plugin) {
        super(plugin, "ardent_flower", "火光花",
                "&#FF7000", "煉獄炎蝶之夢。",
                "被動：免疫火焰傷害");
    }
    @Override public void onPassiveTick(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 30, 0, true, false));
    }
}
