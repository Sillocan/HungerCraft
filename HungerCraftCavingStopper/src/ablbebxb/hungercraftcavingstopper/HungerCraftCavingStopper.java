/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ablbebxb.hungercraftcavingstopper;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;
import java.util.logging.Logger;
import org.bukkit.event.EventHandler;
import java.lang.Runnable;
//import java.util.List;
//import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.HashMap;
import java.util.logging.Level;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.entity.Player;
import org.bukkit.Material;
import ablbebxb.hungercraftperms.PermissionsChangeEvent;
import ablbebxb.hungercraftperms.HungerCraftPermissions;
import org.bukkit.block.Block;


/**
 *
 * @author Alex
 */
public class HungerCraftCavingStopper extends JavaPlugin implements Listener, Runnable{

    //list of players who can go insane
    private Map<Player, Integer> players;

    //delay between sanity checks
    private long delay;

    //damage dealt when insane
    private int damage;

    //amount sanity level drops
    private int decrement;


    static Logger log;

    FileConfiguration config;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        log.info("main works");
    }

    @Override
    public void onEnable()
    {
        //register this as a listener
        getServer().getPluginManager().registerEvents(this, this);

        //initialize lists
        players = new HashMap<Player, Integer>();

        //initialize log
        log = getLogger();

        config = getConfig();

        //set the delay for checks
        delay = config.getLong("delay", 25);

        //set damage
        damage = config.getInt("damage", 1);

        //set decrement
        decrement = config.getInt("decrement", 1);


        //set the plugin to run checks every so often
        getServer().getScheduler().scheduleSyncDelayedTask(this, this, delay);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        //check permissions
        if(!sender.hasPermission("noncaver"))
        {
            sender.sendMessage(cmd.getPermissionMessage());
            return true;
        }

        //if the sender is not a player, treat as if permissions
        //are wrong
        if(!(sender instanceof Player))
        {
            sender.sendMessage(cmd.getPermissionMessage());
            return true;
        }
        Player player = (Player)sender;

        //tells the sender its level of thirst
        if(cmd.getName().equalsIgnoreCase("cks"))
        {
            if(players.containsKey(player))
            {
                sender.sendMessage("Sanity Level: " + players.get(player));
                return true;
            }
            else
            {
                sender.sendMessage("ERROR:  JUST NOW SETTING SANITY, PLEASE ALERT YOUR ADMINISTRATOR");
                log.log(Level.SEVERE, "Thirst has only just been set for " + sender.getName() + " the cause should be investigated");
                players.put(player, 100);
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        //if a player has noncaver permissions, then
        //add him to the player list for this thirst
        //mod
        if(event.getPlayer().hasPermission("noncaver"))
        {
            players.put(event.getPlayer(), 100);
            //event.getPlayer().getInventory().addItem(new ItemStack(373, 64));
        }
    }

    //make people who change to a combatant mid-game go insane
    @EventHandler
    public void onPermChange(PermissionsChangeEvent event)
    {
        if(event.getPlayer().hasPermission("noncaver"))
        {
            players.put(event.getPlayer(), 100);
            //event.getPlayer().getInventory().addItem(new ItemStack(373, 64));
        }
    }

    //run method
    public void run()
    {
        //exit if before start
        if(!HungerCraftPermissions.useDeaths)
        {
            getServer().getScheduler().scheduleSyncDelayedTask(this, this, delay);
            return;
        }
        /**
         * loop through all noncavers and make them more insane if they are underground
         */
         for(Entry<Player, Integer> a : players.entrySet())
         {
                if(a.getKey().hasPermission("noncaver"))
                {
                    //amount of stone within 100 blocks directlyabove the player, if more than three, he is underground
                    int stoneabove = 0;
                    
                    for (Block i = a.getKey().getLocation().getBlock(); i.getLocation().getBlockY() < 100; i = i.getRelative(0, 1, 0))
                    {
                        if(i.getType().equals(Material.STONE))
                            stoneabove++;
                    }

                    //if level is less than 50, he is underground
                    int level = a.getKey().getLocation().getBlockY();


                    //drop the sanity level by decrement if underground, increase it by decrement otherwise
                    if(stoneabove >= 3 || level <= 50)
                    {
                        a.setValue(a.getValue() - decrement);
                        a.getKey().sendMessage("Sanity Level: " + a.getValue());
                    }
                    else if(a.getValue() < 100)
                        a.setValue(a.getValue() + decrement);

                    //if the sanity level is empty then hurt the player
                    if(a.getValue() <= 0)
                    {
                        //set the water level to 0, just in case it somehow
                        //went under
                        a.setValue(0);
                        a.getKey().damage(damage);
                        a.getKey().sendMessage("You are insane! Go outside!");
                    }
                    else
                    {
                        if((a.getValue() + decrement > 75 && a.getValue() <= 75) || (a.getValue() + decrement > 50 && a.getValue() <= 50) || (a.getValue() + decrement > 25 && a.getValue() <= 25) || (a.getValue() < 10))
                            a.getKey().sendMessage("Sanity Level: " + a.getValue());
                    }
                }
         }

         //set this to run again after another section of the day
         getServer().getScheduler().scheduleSyncDelayedTask(this, this, delay);
    }
}