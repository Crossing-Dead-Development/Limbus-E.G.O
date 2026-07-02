package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
public class ChiefButlersSecretArts extends BaseAccessory {
    public ChiefButlersSecretArts(GiftsModule plugin) {
        super(plugin, "chief_butlers_secret_arts", "首席管家的秘籍", "&7被動：抗性提升 I，緩降");
    }
    @Override public void onPassiveTick(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 30, 0, true, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 30, 0, true, false));
    }
}