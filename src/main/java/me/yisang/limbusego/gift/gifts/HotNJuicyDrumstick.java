package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
public class HotNJuicyDrumstick extends BaseAccessory {
    public HotNJuicyDrumstick(GiftsModule plugin) {
        super(plugin, "hot_n_juicy_drumstick", "火熱多汁枇杷腿",
                "&#D77F00", "雞肉就是雞肉囉。",
                "被動：飽食度不流失");
    }
    @Override public void onPassiveTick(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 30, 0, true, false));
    }
}
