package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.List;
import java.util.Random;
public class StrangeGlyphInscriptions extends BaseAccessory {
    private static final List<PotionEffectType> POSITIVE = List.of(
        PotionEffectType.STRENGTH, PotionEffectType.REGENERATION,
        PotionEffectType.SPEED, PotionEffectType.RESISTANCE
    );
    private static final List<PotionEffectType> NEGATIVE = List.of(
        PotionEffectType.WEAKNESS, PotionEffectType.SLOWNESS,
        PotionEffectType.NAUSEA, PotionEffectType.BLINDNESS
    );
    private final Random rng = new Random();
    public StrangeGlyphInscriptions(GiftsModule plugin) {
        super(plugin, "strange_glyph_inscriptions", "篆刻的異文", "&7攻擊時 20% 機率觸發隨機正面或負面效果");
    }
    @Override public void onAttack(EntityDamageByEntityEvent event, Player attacker) {
        double m = plugin.getUpgradeMultiplier(attacker, getId());
        if (rng.nextDouble() >= 0.20 * m) return;
        boolean positive = rng.nextBoolean();
        PotionEffectType type = positive
            ? POSITIVE.get(rng.nextInt(POSITIVE.size()))
            : NEGATIVE.get(rng.nextInt(NEGATIVE.size()));
        attacker.addPotionEffect(new PotionEffect(type, 100, 0, true, true));
    }
}
