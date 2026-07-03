package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
public class TrialPlanGuide extends BaseAccessory {
    public TrialPlanGuide(GiftsModule plugin) {
        super(plugin, "trial_plan_guide", "試用規劃指南",
                "&7擊殺時：經驗 +50%｜被動：村莊英雄 I");
    }
    @Override public void onPassiveTick(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, 120, 0, true, false));
    }
    @Override public void onKill(EntityDeathEvent event, Player killer) {
        event.setDroppedExp((int) (event.getDroppedExp() * 1.5));
    }
}
