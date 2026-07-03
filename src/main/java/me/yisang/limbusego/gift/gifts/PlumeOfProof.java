package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import me.yisang.limbusego.status.StatusEffect;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
public class PlumeOfProof extends BaseAccessory {
    public PlumeOfProof(GiftsModule plugin) {
        super(plugin, "plume_of_proof", "證明的羽飾",
                "&#4498DB", "向您致敬。",
                "攻擊：施加束縛 1·2 並獲得迅捷 1·2");
    }
    @Override public void onAttack(EntityDamageByEntityEvent event, Player attacker) {
        LivingEntity target = victimOf(event);
        if (target == null) return;
        applyScaled(target, StatusEffect.BIND, 1, 2, attacker);
        apply(attacker, StatusEffect.HASTE, 1, 2, attacker);
    }
}
