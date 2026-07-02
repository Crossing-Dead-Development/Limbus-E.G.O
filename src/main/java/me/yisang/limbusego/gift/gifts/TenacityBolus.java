package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
public class TenacityBolus extends BaseAccessory {
    public TenacityBolus(GiftsModule plugin) {
        super(plugin, "tenacity_bolus", "強韌丸",
                "&#0C440C", "孔家滅門之日。",
                "被動：抗性提升 I，生命提升 I");
    }
    @Override public void onPassiveTick(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 30, 0, true, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, 30, 0, true, false));
    }
}
