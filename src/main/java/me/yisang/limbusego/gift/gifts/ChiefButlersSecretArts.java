package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import me.yisang.limbusego.status.StatusEffect;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
public class ChiefButlersSecretArts extends BaseAccessory {
    public ChiefButlersSecretArts(GiftsModule plugin) {
        super(plugin, "chief_butlers_secret_arts", "首席管家的秘籍",
                "&7攻擊：施加束縛 2·2｜擊殺：回復 2 點生命");
    }
    @Override public void onAttack(EntityDamageByEntityEvent event, Player attacker) {
        LivingEntity target = victimOf(event);
        if (target == null) return;
        applyScaled(target, StatusEffect.BIND, 2, 2, attacker);
    }
    @Override public void onKill(EntityDeathEvent event, Player killer) {
        killer.setHealth(Math.min(killer.getAttribute(Attribute.MAX_HEALTH).getValue(), killer.getHealth() + 2.0));
    }
}
