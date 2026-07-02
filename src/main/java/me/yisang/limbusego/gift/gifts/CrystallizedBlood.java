package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
public class CrystallizedBlood extends BaseAccessory {
    public CrystallizedBlood(GiftsModule plugin) {
        super(plugin, "crystallized_blood", "血液結晶",
                "&#FF0000", "無盡的遊行。",
                "受傷時：獲得力量 I 3 秒");
    }
    @Override public void onDamaged(EntityDamageByEntityEvent event, Player victim) {
        victim.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 60, 0, true, true));
    }
}
