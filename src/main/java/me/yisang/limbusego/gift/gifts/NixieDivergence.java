package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.List;
import java.util.Random;
public class NixieDivergence extends BaseAccessory {
    private static final List<PotionEffectType> POOL = List.of(
        PotionEffectType.SPEED, PotionEffectType.STRENGTH, PotionEffectType.REGENERATION,
        PotionEffectType.NIGHT_VISION, PotionEffectType.FIRE_RESISTANCE,
        PotionEffectType.JUMP_BOOST, PotionEffectType.HASTE
    );
    private final Random rng = new Random();
    public NixieDivergence(GiftsModule plugin) {
        super(plugin, "nixie_divergence", "輝光變動儀", "&7攻擊時 15% 機率隨機獲得一種藥水效果");
    }
    @Override public void onAttack(EntityDamageByEntityEvent event, Player attacker) {
        double m = plugin.getUpgradeMultiplier(attacker, getId());
        if (rng.nextDouble() >= 0.15 * m) return;
        attacker.addPotionEffect(new PotionEffect(POOL.get(rng.nextInt(POOL.size())), 100, 0, true, true));
    }
}
