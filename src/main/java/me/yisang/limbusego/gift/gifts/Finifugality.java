package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.Objects;
public class Finifugality extends BaseAccessory {
    public Finifugality(GiftsModule plugin) {
        super(plugin, "finifugality", "留戀", "&7受傷時：血量低於 30% 觸發速度 II 5 秒");
    }
    @Override public void onDamaged(EntityDamageByEntityEvent event, Player victim) {
        double max = Objects.requireNonNull(victim.getAttribute(Attribute.MAX_HEALTH)).getValue();
        if (victim.getHealth() < max * 0.3) {
            victim.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 1, true, true));
        }
    }
}
