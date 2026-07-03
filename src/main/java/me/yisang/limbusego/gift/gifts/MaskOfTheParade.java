package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import me.yisang.limbusego.status.StatusEffect;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
public class MaskOfTheParade extends BaseAccessory {
    public MaskOfTheParade(GiftsModule plugin) {
        super(plugin, "mask_of_the_parade", "遊行的面具",
                "&#9928BB", "\"只要稍微支付一點血液，就能將這一瞬間永遠儲存下來！\"",
                "被動：潛行時持續獲得隱身｜攻擊：施加流血 3·3｜擊殺：對 5 格內敵人擴散流血 2·2");
    }
    @Override public void onPassiveTick(Player player) {
        if (player.isSneaking()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 30, 0, true, false));
        } else {
            player.removePotionEffect(PotionEffectType.INVISIBILITY);
        }
    }
    @Override public void onAttack(EntityDamageByEntityEvent event, Player attacker) {
        LivingEntity target = victimOf(event);
        if (target == null) return;
        applyScaled(target, StatusEffect.BLEED, 3, 3, attacker);
    }
    @Override public void onKill(EntityDeathEvent event, Player killer) {
        double m = plugin.getUpgradeMultiplier(killer, getId());
        double r = 5 * m;
        for (var e : event.getEntity().getLocation().getNearbyLivingEntities(r)) {
            if (e.equals(killer) || e instanceof Player) continue;
            apply(e, StatusEffect.BLEED, 2, 2, killer);
        }
    }
}
