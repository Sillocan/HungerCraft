/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ablbebxb.hungercraft;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import ablbebxb.hungercraftperms.PermissionsChangeEvent;
import ablbebxb.hungercraftperms.HungerCraftPermissions;

/**
 *
 * @author Alex
 */
public class myPlayerListener implements Listener
{
    HungerCraft plugin;

    public myPlayerListener(HungerCraft plugin)
    {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;

    }

    //Determines the visibility of the player based on
    //Spectator, crew, or competitor status
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        //make all invisible players invisible for this player
        for(String a : plugin.invis)
        {
            //if the player is currently on, hide him from this player
            if(plugin.getServer().getPlayer(a) != null)
                hidePlayer(plugin.getServer().getPlayer(a));
        }


        Player noob = event.getPlayer();

        noob.setPlayerListName("HIII");

        //plugin.log.info("Load Player: " + noob.getName());
        //plugin.log.info("  has admin: " + noob.hasPermission("admin"));
        //plugin.log.info("  has combatant: " + noob.hasPermission("combatant"));

        if(noob.hasPermission("combatant"))
        {
            noob.setAllowFlight(false);

            plugin.playerState.put(noob.getName(), combState.ALIVE);

            plugin.getServer().broadcastMessage(noob.getName() + ", a Combatant on team " + plugin.getTeam(noob) + ", has joined the game");
        }
        else if(noob.hasPermission("admin"))
        {

            hidePlayer(noob);

            noob.setAllowFlight(true);
            noob.sendMessage("Welcome to HungerCraft, Mr. Administrator");
            
        }
        else if (noob.hasPermission("crew"))
        {

            hidePlayer(noob);

            noob.setAllowFlight(true);
            noob.sendMessage("Welcome to HungerCraft, thanks for crewing");
        }
        else
        {

            hidePlayer(noob);

            noob.setAllowFlight(true);
            noob.sendMessage("Welcome to HungerCraft, You are currently spectating");
        }
    }

    @EventHandler
    public void onPlayerExit(PlayerQuitEvent event)
    {
        
    }

    private void hidePlayer(Player player)
    {
        for(Player a : plugin.getServer().getOnlinePlayers())
           {
                a.hidePlayer(player);
           }
        
    }

    private void showPlayer(Player player)
    {
        for(Player a : plugin.getServer().getOnlinePlayers())
           {
                a.showPlayer(player);
           }

    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event)
    {

        if(event.getTarget() instanceof Player)
        {
            Player a = (Player)event.getTarget();
            if(!a.hasPermission("combatant") || !HungerCraftPermissions.useDeaths)
            {
                event.setTarget(null);
            }
        }
    }

    @EventHandler
    public void onItemPickup(PlayerPickupItemEvent event)
    {
        if(!event.getPlayer().hasPermission("combatant") || !HungerCraftPermissions.useDeaths)
        {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        if((!event.getPlayer().hasPermission("combatant") && !event.getPlayer().hasPermission("admin")) || !HungerCraftPermissions.useDeaths)
        {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onNaturalDamage(EntityDamageByBlockEvent event)
    {
        if(event.getEntity() instanceof Player)
        {
            Player p = (Player)event.getEntity();
            if(!p.hasPermission("combatant") || !HungerCraftPermissions.useDeaths)
            {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event)
    {
        //if dieer is a player combatant, set his state to DECEACED
        if(event.getEntity() instanceof Player && ((Player)event.getEntity()).hasPermission("combatant"))
        {
            plugin.playerState.put(((Player)event.getEntity()).getName(), combState.DECEASED);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event)
    {
        if(event.getDamager() instanceof Player)
        {
            Player p = (Player)event.getDamager();
            if(!p.hasPermission("combatant") || !HungerCraftPermissions.useDeaths)
            {
                event.setCancelled(true);
            }
        }
    }

   
    @EventHandler
    public void onPermissionChange(PermissionsChangeEvent event)
    {
        Player noob = event.getPlayer();

        //plugin.log.info("Load Player: " + noob.getName());
        //plugin.log.info("  has admin: " + noob.hasPermission("admin"));
        //plugin.log.info("  has combatant: " + noob.hasPermission("combatant"));

        if(noob.hasPermission("combatant"))
        {
            noob.setAllowFlight(false);
            showPlayer(noob);

            plugin.playerState.put(noob.getName(), combState.ALIVE);

            plugin.getServer().broadcastMessage(noob.getName() + " has been made a combatant " + plugin.getTeam(noob));
        }
        else if(noob.hasPermission("admin"))
        {
            plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new delaySetAttr(noob), 25);
            hidePlayer(noob);

            noob.setAllowFlight(true);
            noob.sendMessage("Welcome to HungerCraft, Mr. Administrator");

        }
        else if (noob.hasPermission("crew"))
        {
            plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new delaySetAttr(noob), 25);

            hidePlayer(noob);

            noob.setAllowFlight(true);
            noob.sendMessage("Welcome to HungerCraft, thanks for crewing");
        }
        else
        {
            plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new delaySetAttr(noob), 25);

            hidePlayer(noob);

            noob.setAllowFlight(true);
            noob.sendMessage("Welcome to HungerCraft, You are currently spectating");
        }
    }

    //runs a setting of its player's flight and visibility attributes
    private class delaySetAttr implements Runnable
    {
        //player to set attributes of
        private Player player;

        public delaySetAttr(Player player)
        {
            this.player = player;
        }

        public void run()
        {
            hidePlayer(player);
            player.setAllowFlight(true);
        }
    }
}
