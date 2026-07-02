package me.yisang.limbusego.gift;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public interface Accessory {
    String getId();
    /** 已上色的顯示名稱（同 createItem 的物品名），供訊息顯示用，不必建整顆物品。 */
    String getDisplayName();
    ItemStack createItem();
    void give(Player player);

    default void onPassiveTick(Player player) {}
    default void onAttack(EntityDamageByEntityEvent event, Player attacker) {}
    default void onDamaged(EntityDamageByEntityEvent event, Player victim) {}
    default void onAnyDamage(EntityDamageEvent event, Player victim) {}
    default void onKill(EntityDeathEvent event, Player killer) {}
    default void onDeath(PlayerDeathEvent event, Player deceased) {}
    default void onInteract(PlayerInteractEvent event, Player player) {}
    /** 玩家退出時呼叫，供飾品清理自己的冷卻/計時 map，避免無上限累積。 */
    default void onQuit(Player player) {}
}
