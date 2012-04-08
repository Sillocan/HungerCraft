/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ablbebxb.hungercraftthirst;




import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;
import java.util.logging.Logger;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.Inventory;
import org.bukkit.event.EventPriority;
import java.lang.Runnable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.HashMap;
import java.util.logging.Level;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.Event.Result;
import org.bukkit.inventory.ItemStack;
import ablbebxb.hungercraftperms.PermissionsChangeEvent;
import ablbebxb.hungercraftperms.HungerCraftPermissions;



/**
 *
 * @author Alex
 */
public class HungerCraftThirst extends JavaPlugin implements Listener, Runnable{

    //list of players who can thirst
    private Map<Player, Integer> players;

    //delay between thirst checks
    private long delay;

    //damage dealt when dehydrated
    private int damage;

    //amount thirst level drops
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
        delay = config.getLong("delay", 250);

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
        if(!sender.hasPermission("thirster"))
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
        if(cmd.getName().equalsIgnoreCase("checkthirst"))
        {
            if(players.containsKey(player))
            {
                sender.sendMessage("Thirst Level: " + players.get(player));
                return true;
            }
            else
            {
                sender.sendMessage("ERROR:  JUST NOW SETTING THIRST, PLEASE ALERT YOUR ADMINISTRATOR");
                log.log(Level.SEVERE, "Thirst has only just been set for " + sender.getName() + " the cause should be investigated");
                players.put(player, 100);
                return true;
            }
        }
        
        //shows the sender its level of thirst
        if(cmd.getName().equalsIgnoreCase("thirst"))
        {
            if(players.containsKey(player))
            {
            	String thirst = "" + ChatColor.BLUE;
            	for(int i = 0; i < 20; i++)
            	{
            		if(i > players.get(player) / 5)
            			thirst += "" + ChatColor.RED;
            		
            		thirst += "|";
            	}
            		
                sender.sendMessage("Thirst Level: " + thirst);
                return true;
            }
            else
            {
                sender.sendMessage("ERROR:  JUST NOW SETTING THIRST, PLEASE ALERT YOUR ADMINISTRATOR");
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
        //if a player has thirster permissions, then
        //add him to the player list for this thirst
        //mod
        if(event.getPlayer().hasPermission("thirster"))
        {
            players.put(event.getPlayer(), 100);
            //event.getPlayer().getInventory().addItem(new ItemStack(373, 64));
        }
    }


    @EventHandler (priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent event)
    {
        //if the player is a thirster, then proceed
        if(event.getPlayer().hasPermission("thirster"))
        {

            //log.info("block: " + event.getClickedBlock().getRelative(0, 1, 0).getType().name());
            //if the player clicked water with a bowl, then relieve
            //his thirst
            if ((event.getItem() != null && event.getItem().getTypeId() == 373 && (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK))) || (event.getPlayer().getLocation().getBlock().getType().equals(Material.WATER) || event.getPlayer().getLocation().getBlock().getType().equals(Material.STATIONARY_WATER)))
            {
                players.put(event.getPlayer(), 100);
                event.getPlayer().sendMessage("You quench your thirst");
                
                //get player inventory
                Inventory inv = event.getPlayer().getInventory();

                
                //place an empty bottle in the players inventory
                if(event.getItem() != null && event.getItem().getTypeId() == 373)
                {
                    event.setUseItemInHand(Result.DENY);


                    //if the player already has an empty bottle, put the new bottle
                    //on that stack
                   /* if(inv.contains(374))
                    {
                        //get the id of the first location of an emptyt water bottle
                        //stack that isnt full
                        int putId = 0;

                        int oldStack = 0;
                        

                        //get a map of all water bottles for editing
                        HashMap<Integer, ? extends ItemStack> allIds = inv.all(374);

                        //iterator
                        Iterator count = allIds.entrySet().iterator();

                        for(int i = 0; oldStack+1 > 64 && count.hasNext(); i++)
                        {
                           Entry<Integer, ? extends ItemStack> temp = (Entry<Integer, ? extends ItemStack>)count.next();

                           //get the number of bottles in the stack of empty water bottles in the first location
                           oldStack = temp.getValue().getAmount();

                           putId = temp.getKey();
                           log.info("id: " + temp.getKey());
                        }



                        ItemStack newStack = new ItemStack(374, oldStack + 1);

                        inv.setItem(putId, newStack);
                    }
                    //if no other empty bottle stacks, then get the first empty slot and
                    //put the bottle there
                    else if(!inv.contains(374))
                    {*/
                        ItemStack bottle = new ItemStack(374, 1);
                        inv.addItem(bottle);
                   // }

                      //remove the old bottle
                     //bottle stack id
                     if(inv.contains(373))
                    {
                        int removeId = inv.first(373);

                        int oldQuantity = inv.getItem(removeId).getAmount();

                        if(oldQuantity - 1 > 0)
                            inv.setItem(removeId, new ItemStack(373, oldQuantity-1));
                        else
                            inv.setItem(removeId, new ItemStack(0));
                     }
                }


            }
        }
    }

    //make people who change to a combatant mid-game get thirsty
    @EventHandler
    public void onPermChange(PermissionsChangeEvent event)
    {
        if(event.getPlayer().hasPermission("thirster"))
        {
            players.put(event.getPlayer(), 100);
            //event.getPlayer().getInventory().addItem(new ItemStack(373, 64));
        }
    }

    //run method
    public void run()
    {
        //exit if not using death classes
        if(!HungerCraftPermissions.useDeaths)
        {
            getServer().getScheduler().scheduleSyncDelayedTask(this, this, delay);
            return;
        }

        /**
         * loop through all thirsters and make them more thirsty
         */
         for(Entry<Player, Integer> a : players.entrySet())
         {
                if(a.getKey().hasPermission("thirster"))
                {
                    //drop the water level by 25
                    a.setValue(a.getValue() - decrement);

                    //if the water level is empty then hurt the player
                    if(a.getValue() <= 0)
                    {
                        //set the water level to 0, just in case it somehow
                        //went under
                        a.setValue(0);
                        a.getKey().damage(damage);
                        a.getKey().sendMessage("You are dehydrated! Drink water!");
                    }
                    else
                    {
                        if((a.getValue() + decrement > 75 && a.getValue() <= 75) || (a.getValue() + decrement > 50 && a.getValue() <= 50) || (a.getValue() + decrement > 25 && a.getValue() <= 25) || (a.getValue() < 10))
                        a.getKey().sendMessage("Thirst Level: " + a.getValue());
                    }
                }
         }
         
         //set this to run again after another section of the day
         getServer().getScheduler().scheduleSyncDelayedTask(this, this, delay);

    }
}