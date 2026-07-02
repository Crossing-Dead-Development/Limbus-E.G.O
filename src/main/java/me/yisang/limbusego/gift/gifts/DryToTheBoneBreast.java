package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
public class DryToTheBoneBreast extends BaseAccessory {
    public DryToTheBoneBreast(GiftsModule plugin) {
        super(plugin, "dry_to_the_bone_breast", "乾巴柴澀雞胸肉",
                "&#D77F00", "雞肉就是雞肉囉。",
                "被動：飽食度不流失；飽足時力量 I");
    }
    @Override public void onPassiveTick(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 30, 0, true, false));
        if (player.getFoodLevel() >= 20) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 30, 0, true, false));
        } else {
            player.removePotionEffect(PotionEffectType.STRENGTH);
        }
    }
}
