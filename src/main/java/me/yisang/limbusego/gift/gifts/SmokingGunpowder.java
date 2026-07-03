package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import me.yisang.limbusego.status.StatusEffect;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
public class SmokingGunpowder extends BaseAccessory {
    public SmokingGunpowder(GiftsModule plugin) {
        super(plugin, "smoking_gunpowder", "有煙火藥",
                "&7攻擊：施加破裂 2·2，並獲得迅捷 1·2");
    }
    @Override public void onAttack(EntityDamageByEntityEvent event, Player attacker) {
        LivingEntity target = victimOf(event);
        if (target == null) return;
        applyScaled(target, StatusEffect.RUPTURE, 2, 2, attacker);
        apply(attacker, StatusEffect.HASTE, 1, 2, attacker);
    }
}
