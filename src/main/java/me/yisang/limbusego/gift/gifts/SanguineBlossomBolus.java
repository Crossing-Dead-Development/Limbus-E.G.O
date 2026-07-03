package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import me.yisang.limbusego.status.StatusEffect;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
public class SanguineBlossomBolus extends BaseAccessory {
    private final Map<UUID, Long> lastDamaged = new HashMap<>();
    public SanguineBlossomBolus(GiftsModule plugin) {
        super(plugin, "sanguine_blossom_bolus", "血花丸",
                "&7非戰鬥時持續緩慢回血｜攻擊：施加流血 2·2");
    }
    @Override public void onAnyDamage(EntityDamageEvent event, Player victim) {
        lastDamaged.put(victim.getUniqueId(), System.currentTimeMillis());
    }
    @Override public void onPassiveTick(Player player) {
        long last = lastDamaged.getOrDefault(player.getUniqueId(), 0L);
        if (System.currentTimeMillis() - last < 5000) return;
        double m = plugin.getUpgradeMultiplier(player, getId());
        int level = m >= 2.0 ? 1 : 0;
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 30, level, true, false));
    }
    @Override public void onAttack(EntityDamageByEntityEvent event, Player attacker) {
        LivingEntity target = victimOf(event);
        if (target == null) return;
        applyScaled(target, StatusEffect.BLEED, 2, 2, attacker);
    }
    @Override public void onQuit(Player player) {
        lastDamaged.remove(player.getUniqueId());
        super.onQuit(player);
    }
}
