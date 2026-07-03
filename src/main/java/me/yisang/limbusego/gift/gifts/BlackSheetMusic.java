package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import me.yisang.limbusego.status.StatusEffect;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
public class BlackSheetMusic extends BaseAccessory {
    public BlackSheetMusic(GiftsModule plugin) {
        super(plugin, "black_sheet_music", "黑色樂譜",
                "&#FFFFFF", "這首樂章將會穿透你的靈魂。",
                "攻擊：施加沉淪 3·3｜攻擊抑鬱或沉淪≥4目標：+25% 傷害");
    }
    @Override public void onAttack(EntityDamageByEntityEvent event, Player attacker) {
        LivingEntity target = victimOf(event);
        if (target == null) return;
        if (plugin.sanity().isDepressed(target) || pot(target, StatusEffect.SINKING) >= 4) {
            event.setDamage(event.getDamage() * 1.25);
        }
        applyScaled(target, StatusEffect.SINKING, 3, 3, attacker);
    }
}
