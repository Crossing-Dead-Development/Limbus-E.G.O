package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import me.yisang.limbusego.status.StatusEffect;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
public class MentalCorruptionBoostingGas extends BaseAccessory {
    public MentalCorruptionBoostingGas(GiftsModule plugin) {
        super(plugin, "mental_corruption_boosting_gas", "精神汙染加速氣體",
                "&7攻擊：施加沉淪 2·2；目標為玩家時額外 -1 SAN");
    }
    @Override public void onAttack(EntityDamageByEntityEvent event, Player attacker) {
        LivingEntity target = victimOf(event);
        if (target == null) return;
        applyScaled(target, StatusEffect.SINKING, 2, 2, attacker);
        if (target instanceof Player pv) {
            plugin.sanity().dropSan(pv, 1);
        }
    }
}
