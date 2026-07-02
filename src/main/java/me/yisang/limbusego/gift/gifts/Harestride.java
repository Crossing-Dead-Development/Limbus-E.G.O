package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
public class Harestride extends BaseAccessory {
    public Harestride(GiftsModule plugin) {
        super(plugin, "harestride", "卯足",
                "&#AAD179", "雲解顯現。",
                "被動：速度 II，跳躍提升 I");
    }
    @Override public void onPassiveTick(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 30, 1, true, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 30, 0, true, false));
    }
}
