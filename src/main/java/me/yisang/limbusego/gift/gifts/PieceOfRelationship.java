package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
public class PieceOfRelationship extends BaseAccessory {
    public PieceOfRelationship(GiftsModule plugin) {
        super(plugin, "piece_of_relationship", "緣分殘片",
                "&#444444", "三生緣分，三千世界，三世因緣。",
                "被動：對 5 格內的玩家施加生命再生 I");
    }
    @Override public void onPassiveTick(Player player) {
        for (Player nearby : player.getLocation().getNearbyPlayers(5.0)) {
            if (!nearby.equals(player)) {
                nearby.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 30, 0, true, false));
            }
        }
    }
}
