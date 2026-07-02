package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
public class EbonyBrooch extends BaseAccessory {
    public EbonyBrooch(GiftsModule plugin) {
        super(plugin, "ebony_brooch", "黑檀胸針",
                "&#5B1365", "女王的蘋果。",
                "被動：夜視；夜晚獲得速度 I");
    }
    @Override public void onPassiveTick(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 30, 0, true, false));
        long time = player.getWorld().getTime();
        if (time >= 13000 && time <= 23000) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 30, 0, true, false));
        }
    }
}
