package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import me.yisang.limbusego.status.StatusEffect;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
public class StrangeGlyphInscriptions extends BaseAccessory {
    public StrangeGlyphInscriptions(GiftsModule plugin) {
        super(plugin, "strange_glyph_inscriptions", "篆刻的異文",
                "&7攻擊破裂中目標：+20% 傷害並延長破裂 1 層");
    }
    @Override public void onAttack(EntityDamageByEntityEvent event, Player attacker) {
        LivingEntity target = victimOf(event);
        if (target == null) return;
        if (has(target, StatusEffect.RUPTURE)) {
            double m = plugin.getUpgradeMultiplier(attacker, getId());
            event.setDamage(event.getDamage() * (1.0 + Math.min(0.30, 0.20 * m)));
            status().refresh(target, StatusEffect.RUPTURE, 1);
        }
    }
}
