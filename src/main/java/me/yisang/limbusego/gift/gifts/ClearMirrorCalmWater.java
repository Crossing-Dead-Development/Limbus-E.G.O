package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import me.yisang.limbusego.status.StatusEffect;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
public class ClearMirrorCalmWater extends BaseAccessory {
    public ClearMirrorCalmWater(GiftsModule plugin) {
        super(plugin, "clear_mirror_calm_water", "明鏡止水",
                "&#56BBDB", "人莫鑑於流水，而鑑於止水。",
                "攻擊：獲得呼吸法 3·2｜擊殺：獲得強壯 3·2");
    }
    @Override public void onAttack(EntityDamageByEntityEvent event, Player attacker) {
        applyScaled(attacker, StatusEffect.POISE, 3, 2, attacker);
    }
    @Override public void onKill(EntityDeathEvent event, Player killer) {
        applyScaled(killer, StatusEffect.POWER, 3, 2, killer);
    }
}
