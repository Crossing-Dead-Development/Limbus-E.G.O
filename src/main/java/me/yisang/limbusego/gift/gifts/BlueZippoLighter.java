package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import me.yisang.limbusego.status.StatusEffect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
public class BlueZippoLighter extends BaseAccessory {
    public BlueZippoLighter(GiftsModule plugin) {
        super(plugin, "blue_zippo_lighter", "藍色Zippo牌打火機",
                "&#44D8DB", "那是翼發放給員工的供給品。",
                "攻擊：20% 機率施加燒傷 2·2｜右鍵：每 8 秒點燃目標或方塊");
    }
    @Override public void onAttack(EntityDamageByEntityEvent event, Player attacker) {
        double m = plugin.getUpgradeMultiplier(attacker, getId());
        if (Math.random() < 0.20 * m && event.getEntity() instanceof LivingEntity target) {
            apply(target, StatusEffect.BURN, 2, 2, attacker);
        }
    }
    @Override public void onInteract(PlayerInteractEvent event, Player player) {
        double m = plugin.getUpgradeMultiplier(player, getId());
        if (!gate(player, (long) (8000 / m))) return;
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
            Block above = event.getClickedBlock().getRelative(0, 1, 0);
            if (above.getType() == Material.AIR) above.setType(Material.FIRE);
            return;
        }
        for (org.bukkit.entity.Entity entity : player.getNearbyEntities(4, 4, 4)) {
            if (entity instanceof LivingEntity target && !target.equals(player)) {
                target.setFireTicks(100);
                return;
            }
        }
    }
}
