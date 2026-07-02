package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
public class Rags extends BaseAccessory {
    public Rags(GiftsModule plugin) {
        super(plugin, "rags", "破布",
                "&#755E42", "它們不再能阻止下落的雨水。",
                "受傷時：將 30% 傷害反彈給攻擊者");
    }
    @Override public void onDamaged(EntityDamageByEntityEvent event, Player victim) {
        if (event.getDamager() instanceof LivingEntity attacker) {
            attacker.damage(event.getFinalDamage() * 0.3);
        }
    }
}
