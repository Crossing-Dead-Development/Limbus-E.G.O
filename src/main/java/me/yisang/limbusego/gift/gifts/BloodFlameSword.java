package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import me.yisang.limbusego.status.StatusEffect;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
public class BloodFlameSword extends BaseAccessory {
    public BloodFlameSword(GiftsModule plugin) {
        super(plugin, "bloodflame_sword", "血炎刀", "&7攻擊：施加燒傷 3·2｜獲得 1 點 SAN");
    }
    @Override public void onAttack(EntityDamageByEntityEvent event, Player attacker) {
        LivingEntity target = victimOf(event);
        if (target == null) return;
        applyScaled(target, StatusEffect.BURN, 3, 2, attacker);
        plugin.sanity().gainSan(attacker, 1);
    }
}
