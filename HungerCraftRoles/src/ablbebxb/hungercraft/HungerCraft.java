/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ablbebxb.hungercraft;

import org.bukkit.Server;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.logging.Logger;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
//import org.bukkit.OfflinePlayer;
//import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.HashMap;
import java.util.Set;
//import java.util.HashSet;
import java.util.ArrayList;
//import org.bukkit.permissions.Permission;
import org.bukkit.configuration.file.FileConfiguration;
//import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.block.Block;
import ablbebxb.hungercraftperms.HungerCraftPermissions;

/**
 *
 * @author Alex
 */
public class HungerCraft extends JavaPlugin
{
    Logger log;
    myPlayerListener listener;

    //Lists invisible players
    ArrayList<String> invis;

    //maps combatants to thier teams
    Map<String, String> teamData;

    //maps combatans to thier states
    Map<String, combState> playerState;

    //config file, used for storing team data
    FileConfiguration config;

    //block players are sent to on set
    Block startingBlock;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
    }

    @Override
    public void onEnable()
    {
        //set up output
        log = this.getLogger();

        //initialize teamData map
        teamData = new HashMap<String, String>();

        //initialize statedada
        playerState = new HashMap<String, combState>();

        //initialize invisible player name list
        invis = new ArrayList<String>();

        //initialize config
        config = getConfig();

        /**
         * read in team data from config
         */
        //contains all keys from config file
        //keys list users
        Set<String> users = config.getKeys(true);

        //loop through the users and get thier team
        for(String a : users)
        {
            teamData.put(a, config.getString(a));
        }

        //read staring block, if available
        if(config.contains("start.x"))
        {
            int x = config.getInt("start.x");
            int y = config.getInt("start.y");
            int z = config.getInt("start.z");
            Location loc = new Location(getServer().getWorld(config.getString("start.world")), x, y, z);
            startingBlock = loc.getBlock();
        }


        //set up listeners
        listener = new myPlayerListener(this);

        //announce success
    }

    @Override
    public void onDisable()
    {
       //save each player's teamname next to his username
       for(Entry<String, String> a : teamData.entrySet())
       {
            config.set(a.getKey(), a.getValue());
       }

       //save any defined starting block
       if(startingBlock != null)
       {
           config.set("start.x", startingBlock.getX());
           config.set("start.y", startingBlock.getY());
           config.set("start.z", startingBlock.getZ());
           config.set("start.world", startingBlock.getWorld().getName());
       }

       //save the config
       saveConfig();
    }


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, java.lang.String label, java.lang.String[] args)
    {

        Player player = null;//set to a player if sent by one

        //exit if player has bad permissions
        if(sender instanceof Player)
        {
            player = (Player) sender;
            if(!(player.hasPermission(cmd.getPermission())) && !(sender instanceof Server))
            {
                sender.sendMessage(cmd.getPermissionMessage());
                return true;
            }

        }

        if(cmd.getName().equalsIgnoreCase("GameStatus"))
        {
            //string to be sent
            String[] out = new String[teamData.size()+1];
            
            //string to be sent in arraylist form
            ArrayList<String> outL = new ArrayList<String>();
            outL.add("Team\tName\tStatus\n");

            int count = 1;


            //loop through each player and put the info into the string
            for(Entry<String, String> a : teamData.entrySet())
            {
                //find out if there is already an entry for this team
                int nextLine = -1;//flag to store index
                for(String b : outL)
                {
                    if(b.contains(a.getValue()))
                        nextLine = outL.indexOf(b);
                }

                //if there is already an entry for this team, put this one directly underneath it
                if(nextLine != -1)
                {
                    String teamSpace = "";
                    String nameSpace = "";
                    //loop through to add spaces, seeing as tabs don't work
                    for(int i = 0; i < (20-a.getValue().length()); i++)
                    {
                        teamSpace += " ";
                    }

                    //loop through to add spaces, seeing as tabs don't work
                    for(int i = 0; i < (20-a.getKey().length()); i++)
                    {
                        nameSpace += " ";
                    }


                    String newSection = String.format("%s%s%s%s%s%n", a.getValue(), teamSpace, a.getKey(), nameSpace, "active\n");
                    outL.add(nextLine, newSection);
                }
                else
                {
                    String teamSpace = "";
                    String nameSpace = "";
                    //loop through to add spaces, seeing as tabs don't work
                    for(int i = 0; i < (20-a.getValue().length()); i++)
                    {
                        teamSpace += " ";
                    }
                    
                    //loop through to add spaces, seeing as tabs don't work
                    for(int i = 0; i < (20-a.getKey().length()); i++)
                    {
                        nameSpace += " ";
                    }


                    String newSection = String.format("%s%s%s%s%s%n", a.getValue(), teamSpace, a.getKey(), nameSpace, "active\n");
                    outL.add(newSection);
                }
            }

            out = outL.toArray(out);
            sender.sendMessage(out);
            return true;
        }
        else if(cmd.getName().equalsIgnoreCase("setteam"))
        {
            if(args.length == 2)
            {
                teamData.put(args[0], args[1]);
                getServer().broadcastMessage(args[0] + " is now on team " + args[1]);
                return true;
            }
        }
        else if(cmd.getName().equalsIgnoreCase("set"))
        {


                //gives players an ordered team number
                int team = 1;

                //string holds all players to call command
                String commandString = "/massgroup competitor";

               //allows for specification of players to set
               if(args.length > 0)
               {
                   for(String a : args)
                   {
                        if(getServer().getPlayer(a) != null)
                        {
                            getServer().getPlayer(a).teleport(startingBlock.getRelative(0, 1, 0).getLocation());
                            teamData.put(a, "team" + Integer.toString(team/2));
                        }
                        else
                        {
                            sender.sendMessage(a + " is not online");
                        }
                        commandString = commandString + " " + a;
                   }
               }
                //set all defaults and competitors
               else
                {
                for(Player b : getServer().getOnlinePlayers())
                {
                    //do not mess with admin or crew
                    if(!(b.hasPermission("admin") || b.hasPermission("crew")))
                    {

                        //retrieve name
                        String a = b.getName();

                        if(getServer().getPlayer(a) != null)
                        {
                            getServer().getPlayer(a).teleport(startingBlock.getRelative(0, 1, 0).getLocation());
                            teamData.put(a, "team" + Integer.toString(team/2));
                        }
                        else
                        {
                            sender.sendMessage(a + " is not online");
                        }
                        commandString = commandString + " " + a;
                    }
                }
                }
                getServer().dispatchCommand(sender, "elevatordown main");
                getServer().dispatchCommand(sender, commandString);
                HungerCraftPermissions.useDeaths = false;
                return true;
            
        }
        else if(cmd.getName().equalsIgnoreCase("markstart"))
        {
            if(sender instanceof Player)
            {
                startingBlock = player.getLocation().getBlock();
                sender.sendMessage("Starting Block Marked");
            }
            return true;
        }
        else if (cmd.getName().equalsIgnoreCase("start"))
        {
            getServer().broadcastMessage("The Games Begin in 10!");
            for (int i = 9; i > 0 ; i--)
            {
                try{
                    Thread.sleep(1000);
                }catch(InterruptedException exep)
                {
                	
                }
                this.getServer().broadcast("" + i + "!", getServer().BROADCAST_CHANNEL_USERS);
            }
            try{
                Thread.sleep(1000);
            }catch(InterruptedException exep)
            {

            }
            getServer().broadcastMessage("BEGIN!");
            getServer().dispatchCommand(sender, "elevatorup main");
            HungerCraftPermissions.useDeaths = true;
            return true;
        }
        return false;
    }

    //returns the given player's team, blank string returned
    //if not assigned a team
    public String getTeam(Player player)
    {
        if(teamData.containsKey(player.getName()))
        {
            return teamData.get(player.getName());
        }
        return "";
    }

    private void hidePlayer(Player player)
    {
        for(Player a : getServer().getOnlinePlayers())
           {
                a.hidePlayer(player);
                invis.add(player.getName());
           }

    }

    private void showPlayer(Player player)
    {
        for(Player a : getServer().getOnlinePlayers())
           {
                a.showPlayer(player);
                if(invis.contains(a.getName()))
                    invis.remove(a.getName());
           }

    }

}

//when constructed, countsdown the given amount of times...
//Note: I really wish there was another way to do this, this seems really overblown
class counter implements Runnable
{
    private int count;

    public counter(int i)
    {
        
    }

    public void run()
    {
        
    }
}

enum combState
{
    ALIVE("Alive"),
    DECEASED("Deceased");

    combState(String name)
    {
        state = name;
    }

    private String state;

    @Override
    public String toString()
    {
        return state;
    }
}
