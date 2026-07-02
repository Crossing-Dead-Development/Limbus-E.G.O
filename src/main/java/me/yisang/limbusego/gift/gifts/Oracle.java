package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.Objects;
public class Oracle extends BaseAccessory {
    public Oracle(GiftsModule plugin) {
        super(plugin, "oracle", "神諭",
                "&#B3F2F9", "舉刀對笑，如落葉之香氣般對瀑布哭泣。",
                "被動：血量低於 40% 時使 10 格內生物顯現");
    }
    @Override public void onPassiveTick(Player player) {
        double max = Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH)).getValue();
        if (player.getHealth() < max * 0.4) {
            for (Entity e : player.getNearbyEntities(10, 10, 10)) {
                if (e instanceof LivingEntity living && !(e instanceof Player)) {
                    living.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 40, 0, true, false));
                }
            }
        }
    }
}
