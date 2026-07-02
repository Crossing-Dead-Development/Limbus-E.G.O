package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
public class EndlessHunger extends BaseAccessory {
    public EndlessHunger(GiftsModule plugin) {
        super(plugin, "endless_hunger", "無盡的飢餓", "&7飢餓不虛弱；攻擊力隨飽食度成正比");
    }
    @Override public void onPassiveTick(Player player) {
        player.removePotionEffect(PotionEffectType.WEAKNESS);
        double m = plugin.getUpgradeMultiplier(player, getId());
        int food = player.getFoodLevel();
        if (food >= 16)      player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 30, (int)(1 * m), true, false));
        else if (food >= 8)  player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 30, 0, true, false));
    }
    @Override public void onAnyDamage(EntityDamageEvent event, Player victim) {
        if (event.getCause() == EntityDamageEvent.DamageCause.STARVATION)
            event.setCancelled(true);
    }
}
