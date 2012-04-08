/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ablbebxb.hungercraftperms;

/**
 *
 * @author Alex
 */
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.entity.Player;

public class PermissionsChangeEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Player player;

    public PermissionsChangeEvent(Player plr) {
        player = plr;

    }

    public Player getPlayer() {
        return player;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}