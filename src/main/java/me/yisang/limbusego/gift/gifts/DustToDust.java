package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
public class DustToDust extends BaseAccessory {
    public DustToDust(GiftsModule plugin) {
        super(plugin, "dust_to_dust", "土歸土",
                "&#9A9A9A", "那並不是霧。",
                "擊殺時：30% 機率獲得吸收 I 5 秒");
    }
    @Override public void onKill(EntityDeathEvent event, Player killer) {
        if (Math.random() < 0.30) {
            killer.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 100, 0, true, true));
        }
    }
}
