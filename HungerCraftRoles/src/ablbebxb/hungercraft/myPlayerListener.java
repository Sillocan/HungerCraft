/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ablbebxb.hungercraft;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import ablbebxb.hungercraftperms.PermissionsChangeEvent;
import ablbebxb.hungercraftperms.HungerCraftPermissions;
import java.util.Map;
import java.util.HashMap;
import java.util.Queue;
import org.bukkit.ChatColor;
import org.bukkit.util.noise.SimplexOctaveGenerator;

/**
 *
 * @author Alex
 */
public class myPlayerListener implements Listener
{
    HungerCraft plugin;
    
    //maps each player to the last cause of damage to them
    Map<String, DamageCause> lastDmg;
    Queue<String> chatHist;
    
    int chatSize = 4;
    
    public myPlayerListener(HungerCraft plugin)
    {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;
        
        //initialize cause of damage map
        lastDmg = new HashMap<String, DamageCause>();
    }

    //Determines the visibility of the player based on
    //Spectator, crew, or competitor status
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event)
    {
    	 Player noob = event.getPlayer();
    	 noob.setLevel(0);
    	 noob.setExp(0.f);
         noob.setTotalExperience(0);
    	
        //make all invisible players invisible for this player
        for(String a : plugin.invis)
        {
            //if the player is currently on, hide him from this player
            if(plugin.getServer().getPlayer(a) != null)
            	//Updated this code, you only need to hide these players from the user logging in, not everyone.
                noob.hidePlayer(plugin.getServer().getPlayer(a));
        }


       



        //plugin.log.info("Load Player: " + noob.getName());
        //plugin.log.info("  has admin: " + noob.hasPermission("admin"));
        //plugin.log.info("  has combatant: " + noob.hasPermission("combatant"));
        
        //Player is a combatant
        if(noob.hasPermission("combatant"))
        {
            noob.setAllowFlight(false);

            noob.setPlayerListName(noob.getName() + "+");
            
            //plugin.playerState.put(noob.getName(), combState.ALIVE);

            plugin.getServer().broadcastMessage(noob.getName() + ", a Combatant on team " + plugin.getTeam(noob) + ", has joined the game");
        }
        //Player is an admin
        else if(noob.hasPermission("admin"))
        {

            hidePlayer(noob);

            noob.setPlayerListName("");
            
            noob.setAllowFlight(true);
            noob.sendMessage("Welcome to HungerCraft, Mr. Administrator");
            
        }
        //Player is a crew member
        else if (noob.hasPermission("crew"))
        {

            noob.setPlayerListName("");
            
            hidePlayer(noob);

            noob.setAllowFlight(true);
            noob.sendMessage("Welcome to HungerCraft, thanks for crewing");
        }
        //player is assumed to be spectator
        else
        {

            noob.setPlayerListName("");
            
            hidePlayer(noob);

            noob.setAllowFlight(true);
            noob.sendMessage("Welcome to HungerCraft, You are currently spectating");
        }
    }

    @EventHandler
    public void onPlayerExit(PlayerQuitEvent event)
    {
        
    }
    
    //Testing out chat history for a bit
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(PlayerChatEvent event)
    {
        Player sender = event.getPlayer();
        
    	//if(chatHist.size() >= chatSize)
    	//	chatHist.remove();
    	
    	//chatHist.offer(event.getMessage());
        
       
        /*
         * Send message only to locality if a combatant,
         * and only to non-combatants
         */
        //if competitor send local message
        if(sender.hasPermission("combatant"))
        {
            //loop through recipients and remove any competitors outside of the set range
            for(Player a : event.getRecipients())
            {
                if(a.hasPermission("combatant") && a.getLocation().distance(sender.getLocation()) > plugin.range)
                {
                    event.getRecipients().remove(a);
                }
            }
            //tell all that he is a competitor
            event.setMessage("[" + ChatColor.RED + "COMBATANT" + ChatColor.WHITE + "]" + event.getMessage());
           
        }
        //otherwise only send messages to non-competitors
        else
        {
            //loop through recipients and remove combatants
            for(Player a : event.getRecipients())
            {
                if(a.hasPermission("combatant"))
                {
                    event.getRecipients().remove(a);
                }
            }
        }
    }
    
    //Hides player from every online user
    private void hidePlayer(Player player)
    {
    	if(!plugin.invis.contains(player.getName()))
    		plugin.invis.add(player.getName());
    	
        for(Player a : plugin.getServer().getOnlinePlayers())
        {
            a.hidePlayer(player);
        }
        
        
    }

    //Shows player to every online user
    private void showPlayer(Player player)
    {
    	if(plugin.invis.contains(player.getName()))
            plugin.invis.remove(player.getName());
    	
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
        (new SimplexOctaveGenerator(event.getPlayer().getWorld(), 5)).noise(0, 0, 0, 50, 50000000);
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
            Player player = (Player)event.getEntity();
            String playerName = player.getName();
            //change player's name to reflect his state
            player.setPlayerListName(playerName + "-");
            
            /*
             * brodcast his death
             */
            //edit the string based on the last cause of damage to the player
            String methodOfDeath = " was killed";
            if(!lastDmg.containsKey(playerName))
                methodOfDeath = " died of unknown causes";
            else if(lastDmg.get(playerName).equals(DamageCause.BLOCK_EXPLOSION))
                methodOfDeath = " exploded";
            else  if(lastDmg.get(playerName).equals(DamageCause.CONTACT))
                methodOfDeath = " ran into a cactus";
            else  if(lastDmg.get(playerName).equals(DamageCause.DROWNING))
                methodOfDeath = " drowned";
            else  if(lastDmg.get(playerName).equals(DamageCause.ENTITY_ATTACK))
                methodOfDeath = " died in combat";
            else  if(lastDmg.get(playerName).equals(DamageCause.ENTITY_EXPLOSION))
                methodOfDeath = " was killed by a creeper";
            else  if(lastDmg.get(playerName).equals(DamageCause.FALL))
                methodOfDeath = " fell and died";
            else  if(lastDmg.get(playerName).equals(DamageCause.FIRE))
                methodOfDeath = " burned to death";
            else  if(lastDmg.get(playerName).equals(DamageCause.FIRE_TICK))
                methodOfDeath = " burned to death";
            else  if(lastDmg.get(playerName).equals(DamageCause.LAVA))
                methodOfDeath = " died in a Lava Pit";
            else  if(lastDmg.get(playerName).equals(DamageCause.LIGHTNING))
                methodOfDeath = " was killed by lightning";
            else  if(lastDmg.get(playerName).equals(DamageCause.MAGIC))
                methodOfDeath = " was cursed by magic and died";
            else  if(lastDmg.get(playerName).equals(DamageCause.MELTING))
                methodOfDeath = " melted to death";
            else  if(lastDmg.get(playerName).equals(DamageCause.POISON))
                methodOfDeath = " was killed by poison";
            else  if(lastDmg.get(playerName).equals(DamageCause.PROJECTILE))
                methodOfDeath = " was killed by an arrow";
            else  if(lastDmg.get(playerName).equals(DamageCause.SUFFOCATION))
                methodOfDeath = " suffocated";
            else  if(lastDmg.get(playerName).equals(DamageCause.SUICIDE))
                methodOfDeath = " commited suicide";
            else  if(lastDmg.get(playerName).equals(DamageCause.VOID))
                methodOfDeath = " fell into the void";
            
            plugin.getServer().broadcastMessage("Combatant " + player.getName() + methodOfDeath);
            
            //play a sound to signal the death to all player
            for(Player a : plugin.getServer().getOnlinePlayers())
            {
                //TODO find a better sound, not sure which one is best at this point
                //a.playEffect(a.getLocation(), Effect.BLAZE_SHOOT, 10);
            	a.getWorld().createExplosion(a.getLocation(), 0.f);
            	
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event)
    {
        
        //prevent non-competitors from damaging
        if(event.getDamager() instanceof Player)
        {
            Player p = (Player)event.getDamager();
            if(!p.hasPermission("combatant") || !HungerCraftPermissions.useDeaths)
            {
                event.setCancelled(true);
            }
        }
        
        //store the last damage caused to a competitor
        
        //if a player was damaged then store the player and check its permissions
        if(event.getEntity() instanceof Player)
        {
            Player player = (Player)event.getEntity();
            //only store competitor damage data
            if(player.hasPermission("competitor"))
            {
                lastDmg.put(player.getName(), event.getCause());
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

            noob.setPlayerListName(noob.getName() + "+");
            
            //plugin.playerState.put(noob.getName(), combState.ALIVE);

            plugin.getServer().broadcastMessage(noob.getName() + " has been made a combatant " + plugin.getTeam(noob));
            
            //Shows player if they are changed from a non-visble group
            if(plugin.invis.contains(noob.getName()))
            	showPlayer(noob);
            
        }
        else if(noob.hasPermission("admin"))
        {
            plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new delaySetAttr(noob), 25);
            
            //Hides player if they are changed from a visible group
            if(!plugin.invis.contains(noob.getName()))
            	hidePlayer(noob);

            //if the player's name does not appear to be set such that he just died as a competitor, then make his name blank
            if(!noob.getPlayerListName().equals(noob.getName() + "-"))
                noob.setPlayerListName("");
            
            noob.setAllowFlight(true);
            noob.sendMessage("Welcome to HungerCraft, Mr. Administrator");

        }
        else if (noob.hasPermission("crew"))
        {
            plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new delaySetAttr(noob), 25);

            //if the player's name does not appear to be set such that he just died as a competitor, then make his name blank
            if(!noob.getPlayerListName().equals(noob.getName() + "-"))
                noob.setPlayerListName("");
            
            //Hides player if they are changed from a visible group
            if(!plugin.invis.contains(noob.getName()))
            	hidePlayer(noob);

            noob.setAllowFlight(true);
            noob.sendMessage("Welcome to HungerCraft, thanks for crewing");
        }
        else
        {
        	//Does this run every 25 ticks?
            plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new delaySetAttr(noob), 25);

            //if the player's name does not appear to be set such that he just died as a competitor, then make his name blank
            if(!noob.getPlayerListName().equals(noob.getName() + "-"))
                noob.setPlayerListName("");
            
            //Hides player if they are changed from a visible group
            if(!plugin.invis.contains(noob.getName()))
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