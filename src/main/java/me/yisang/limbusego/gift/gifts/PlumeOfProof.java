package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
public class PlumeOfProof extends BaseAccessory {
    public PlumeOfProof(GiftsModule plugin) {
        super(plugin, "plume_of_proof", "證明的羽飾",
                "&#4498DB", "向您致敬。",
                "擊殺時：獲得速度 II 5 秒");
    }
    @Override public void onKill(EntityDeathEvent event, Player killer) {
        killer.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 1, true, true));
    }
}
