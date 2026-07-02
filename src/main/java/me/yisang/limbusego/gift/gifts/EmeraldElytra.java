package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
public class EmeraldElytra extends BaseAccessory {
    public EmeraldElytra(GiftsModule plugin) {
        super(plugin, "emerald_elytra", "綠色鞘翅",
                "&#16B569", "走到傘底下，等待雨停。",
                "被動：緩降，跳躍提升 I");
    }
    @Override public void onPassiveTick(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 30, 0, true, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 30, 0, true, false));
    }
}
