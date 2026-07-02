package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.entity.Bee;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
public class RoyalJellyPerfume extends BaseAccessory {
    public RoyalJellyPerfume(GiftsModule plugin) {
        super(plugin, "royal_jelly_perfume", "蜂王漿香水", "&7被動：生命恢復 I；附近蜜蜂不攻擊");
    }
    @Override public void onPassiveTick(Player player) {
        double m = plugin.getUpgradeMultiplier(player, getId());
        int level = (int)(m) - 1;
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 30, Math.max(0, level), true, false));
        player.getNearbyEntities(8, 8, 8).forEach(e -> {
            if (e instanceof Bee bee && bee.getTarget() != null && bee.getTarget().equals(player))
                bee.setTarget(null);
        });
    }
}
