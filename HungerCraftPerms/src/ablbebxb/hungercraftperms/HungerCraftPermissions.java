/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ablbebxb.hungercraftperms;

import org.bukkit.event.EventHandler;
//import org.bukkit.event.EventPriority;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.bukkit.entity.Player;
import java.util.HashSet;
import java.util.TreeSet;
import org.bukkit.permissions.Permission;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;


/**
 *
 * @author Alex
 */
public class HungerCraftPermissions extends JavaPlugin implements Listener
{

    //if true, people go to thier classes death class when they die
    public static boolean useDeaths;

    Logger log;//ouput log

    FileConfiguration config;//default plugin config file
    Map<String, PermissionAttachment> perms;//maps usernames to thier permission attachment while users are online
    Map<String, String> usersGroups;//maps usernames to the string representing thier permission usersGroups
    Map<String, List<Permission>> groupPerms;//maps groups to thier permissions
    Map<String, String> deathGroups;//maps groups to the groups thier members should be assigned to on death, if a group is not in this list, then its death group is itself

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
    }

    @Override
    public void onEnable()
    {
        HungerCraftPermissions.useDeaths = false;

        //get logger
        log = getLogger();

        //get config file
        config = getConfig();

        //set to load defaults
        config.options().copyDefaults(true);

        //instatiate maps
        perms = new HashMap<String, PermissionAttachment>();
        usersGroups = new HashMap<String, String>();
        groupPerms = new HashMap<String, List<Permission>>();
        deathGroups = new HashMap<String, String>();


        //Sets of strings to contain base level keys in config file
        Set<String> keys = new TreeSet<String>();
        Set<String> groupNames = new TreeSet<String>();

        /*
         * get user groups
         */
        //get all the sections in the yaml file
        keys = config.getKeys(false);

        //find the usersGroups section and get the group names
        for(String a : keys)
        {
            if(a.equalsIgnoreCase("groups"))
            {
                //log.info("found group section");
                groupNames = config.getConfigurationSection(a).getKeys(false);
            }
        }

        //log.info("------load user groups-------");

        //loop through every group
        for(String a : groupNames)
        {
            //log.info("load group: " + a);
            //get every user in the group
            List<String> usersInGroup = config.getStringList("groups." + a);

            //loop through every user in the group
            for(String b : usersInGroup)
            {
                //log.info("  add to group: " + b);
                //map the group to the user
                usersGroups.put(b, a);
                
            }
        }
        
        //if default perms are available, set them
        if(config.getKeys(false).contains("default"))
        {
            //get default permission names
            List<String> a = config.getStringList("default.permissions");

            List<Permission> tempPerm = new ArrayList<Permission>();

            //iterate through permission names and add permissions to defalt map value
            for(String b : a)
            {
                tempPerm.add(new Permission(b));
            }
            groupPerms.put("default", tempPerm);
        }

        //log.info("-------load group permissions-------");

        /*
         *get group permissions
         */
        //loop through the base level set of keys to find
        //any groups listed
        for(String a : keys)
        {
            //log.info("load group: " + a);
            if(groupNames.contains(a) && config.contains(a + ".permissions"))
            {
                //create temporary lists of permission string names
                //and permission objects
                List<Permission> tempPerms = new ArrayList<Permission>();
                List<String> tempPermNames = config.getStringList(a + ".permissions");

                //make one Permission for every permission name
                //and add them to the tempPerm Permission list
                for(String b : tempPermNames)
                {
                    Permission c = new Permission(b);
                    tempPerms.add(c);
                    //log.info("  add perm: " + c.getName());
                }
                groupPerms.put(a, tempPerms);
            }
        }

        /*
         * get group deathgroups
         */
        //loop through the list of groups to find each groups deathGroup
        for(String a : groupNames)
        {
            //get death group name
            String dGroup = config.getString(a + ".dgroup", "");
            //if the deathgroup is defined, add it, otherwise ignore it
            if(!dGroup.equals(""))
            {
                //log.info("Deathgroup: " + a + " - " + dGroup);
                deathGroups.put(a, dGroup);
            }
        }
        

        /*
         * register listeners
         */
        getServer().getPluginManager().registerEvents(this, this);


        //log.info("HungerCraftPermissions v 0.1 Enabled");
    }

    @Override
    public void onDisable()
    {
        //maps groups to thier users
        Map<String, List<String>> groupsUsers = new HashMap<String, List<String>>();

        //loop through users groups and store each user to the group map
        for(Entry<String, String> a : usersGroups.entrySet())
        {
            //if the list is null, set it to a blank arraylist
            if (groupsUsers.get(a.getValue()) == null)
                groupsUsers.put(a.getValue(), new ArrayList<String>());

            groupsUsers.get(a.getValue()).add(a.getKey());
        }

        //now save each groups users
        for(Entry<String, List<String>> a : groupsUsers.entrySet())
        {
            config.set("groups." + a.getKey(), a.getValue());
        }


        saveConfig();
        //log.info("HungerCraftPermissions v 0.1 Disabled");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, java.lang.String label, java.lang.String[] args)
    {
        if(!sender.hasPermission(cmd.getPermission()) && sender instanceof Player)
        {
            sender.sendMessage(cmd.getPermissionMessage());
            return true;
        }

        //changes a given user's group
        if(cmd.getName().equalsIgnoreCase("changegroup"))
        {
            if(args.length == 2)
            {
                if(!groupPerms.containsKey(args[1]))
                {
                    sender.sendMessage("sorry, this group does not exist");
                    return true;
                }

                usersGroups.put(args[0], args[1]);
                if(getServer().getPlayer(args[0]) != null)
                {
                    giveGroupPermissions(getServer().getPlayer(args[0]));
                    PermissionsChangeEvent event = new PermissionsChangeEvent(getServer().getPlayer(args[0]));
                    getServer().getPluginManager().callEvent(event);
                }
                return true;
            }

        }
        else if(cmd.getName().equalsIgnoreCase("massgroup"))
        {
            if(args.length >= 2)
            {
                String groupName = args[0];
                if(groupPerms.containsKey(groupName))
                {
                    for(int i = 1; i < args.length; i++)
                    {
                        usersGroups.put(args[i], groupName);
                        if(getServer().getPlayer(args[i]) != null)
                        {
                            giveGroupPermissions(getServer().getPlayer(args[i]));
                            PermissionsChangeEvent event = new PermissionsChangeEvent(getServer().getPlayer(args[i]));
                            getServer().getPluginManager().callEvent(event);
                        }
                    }
                }
                else
                {
                    sender.sendMessage("This group does not exist");
                }
                return true;
            }
        }

        return false;
    }

    //meant to handle player deaths, but no event for this is provided
    //on any death, set the player's perms to default
    @EventHandler
    public void onEntityDeath(PlayerRespawnEvent event)
    {
            //go ahead and exit if deathgroups are not in use
            if(!HungerCraftPermissions.useDeaths)
                return;

            //get the instanceof player into an actual player variable
            Player player = (Player)event.getPlayer();

            //filter default users
            if(!usersGroups.containsKey(player.getName()))
                return;

            //get user's current group
            String cGroup = usersGroups.get(player.getName());

            //filter groups that lack deathGroups
            if(!deathGroups.containsKey(cGroup))
                return;

            //get death group
            String dGroup = deathGroups.get(cGroup);

            //set user's current group to the death group
            usersGroups.put(player.getName(), dGroup);
            
            //give user current group permissions (PermissionsChangeEvent)
            giveGroupPermissions(player);
            PermissionsChangeEvent evnt = new PermissionsChangeEvent(player);
            getServer().getPluginManager().callEvent(evnt);
    }

    /*
     *Assigns permissions to pleyers as they join
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {

        giveGroupPermissions(event.getPlayer());
    }

    private void giveGroupPermissions(Player player)
    {
        //the name of the player loging in
        String playerName = player.getName();

        //log.info("Modifying Player: " + playerName);

        //permission attachment in place for the player
        PermissionAttachment attch = perms.get(playerName);

        //if no attachment already in place from map, then give the player a new attachment and proceed
        if(attch == null)
           attch = player.addAttachment(this);

        //begin by unsetting any previous permissions
        for(PermissionAttachmentInfo a : player.getEffectivePermissions())
        {
                attch.setPermission(a.getPermission(), false);
                //log.info("  Permission Removed: " + a.getPermission());
        }

        //log.info("-----user to group map-----");

        boolean contained = false;

        for(Entry<String, String> a : usersGroups.entrySet())
        {
           // log.info("  " + a.getKey() + " - " + a.getValue());
            if(a.getKey() == playerName)
                contained = true;
        }

        //log.info("  " + contained);

        //if the user is registered to any groups
        //then assign the user the permissions
        //for those groups
        if(usersGroups.containsKey(playerName))
        {
            //the player's group
            String group = usersGroups.get(playerName);

            //log.info("  Group: " + group);

            //the permission attachment being created
            //for the player
            //note: attachment added to the player via
            //this constructor
            //PermissionAttachment attch = player.addAttachment(this);

            //get user's
            //group's permissions, add these to the user's
            //permission attachment
           // log.info(group);

            //a list of permissions associated with the group
            List<Permission> prms = groupPerms.get(group);

            //loop through the permissions to add them all
            //to the permissions attachment
                for(Permission b : prms)
                {
                    attch.setPermission(b, true);
                    //debug code commented out
                    //log.info("Permission added:   " + b.getName());
                }


            perms.put(playerName, attch);

        }
        //if the user does not already have a group, give him default permissions
        else
        {
            //log.info("  Group: default");
            //PermissionAttachment attch = player.addAttachment(this);
            //loop through default permissions
            for(Permission a : groupPerms.get("default"))
            {
                attch.setPermission(a, true);
            }

            //add perms reference
            perms.put(playerName, attch);
            
        }
    }
}
