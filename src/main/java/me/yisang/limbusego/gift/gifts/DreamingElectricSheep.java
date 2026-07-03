package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import me.yisang.limbusego.status.StatusEffect;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
public class DreamingElectricSheep extends BaseAccessory {
    public DreamingElectricSheep(GiftsModule plugin) {
        super(plugin, "dreaming_electric_sheep", "夢中的電子羊",
                "&#9863E7", "你的是什麼顏色？",
                "被動：緩降｜擊殺：獲得強壯 2·3");
    }
    @Override public void onPassiveTick(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 30, 0, true, false));
    }
    @Override public void onKill(EntityDeathEvent event, Player killer) {
        applyScaled(killer, StatusEffect.POWER, 2, 3, killer);
    }
}
