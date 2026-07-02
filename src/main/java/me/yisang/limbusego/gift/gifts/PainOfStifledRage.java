package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
public class PainOfStifledRage extends BaseAccessory {
    public PainOfStifledRage(GiftsModule plugin) {
        super(plugin, "pain_of_stifled_rage", "鬱火",
                "&#DA3D24", "公牛絕望地咆哮著……",
                "受傷時：獲得力量 I 5 秒");
    }
    @Override public void onDamaged(EntityDamageByEntityEvent event, Player victim) {
        victim.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 100, 0, true, true));
    }
}
