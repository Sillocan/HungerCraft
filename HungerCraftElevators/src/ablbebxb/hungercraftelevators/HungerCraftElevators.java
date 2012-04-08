/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ablbebxb.hungercraftelevators;

import java.util.logging.Logger;
import java.util.logging.Level;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.block.Block;
//import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.Location;
import org.bukkit.World;
import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.IOException;
import java.util.Set;
//import org.bukkit.event.Listener;

/**
 *
 * @author Alex
 */
public class HungerCraftElevators extends JavaPlugin
{
    //list of all elevator blocks
    List<elevatorBlock> elevators;

    //elevator data file, used to store elevator data
    FileConfiguration elevData;

    //logger
    static Logger log;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        // TODO code application logic here
    }

    @Override
    public void onEnable()
    {
        //ititialize logger
        log = getLogger();

        //get elevator data
        elevData = getData();

        //initialize lists
        elevators = new ArrayList<elevatorBlock>();

        /**
         * get elevator data into elevators
         */

        //all the data keys...
        //practicaly speaking just a Set of
        //all the elevators data locations
        Set<String> keys = elevData.getKeys(false);

        //loop through the keys to get the data for each
        //elevator
        for(String a : keys)
        {
            int height;//int holding the floor height
            int x, y, z;//ints for the x, y, znd z coordinates
            Block block;//block to hold the elevator block
            String world;//String holding the name of the elevator's world
            String id;//id of the elevator

            //get floor height
            height = elevData.getInt(a + ".height");

            //get world name
            world = elevData.getString(a + ".world");

            //get the coordinate data
            x = elevData.getInt(a + ".xpos");
            y = elevData.getInt(a + ".ypos");
            z = elevData.getInt(a + ".zpos");

            //get the block at the coordinates from the data file
            block = getServer().getWorld(world).getBlockAt(x, y, z);

            //get the elevator id
            id = elevData.getString(a + ".id");

            //debug line
            //HungerCraftElevators.log.info("Height loaded: " + height);
            //HungerCraftElevators.log.info("Block loaded: " + block.getY());

            //create the elevator
            elevators.add(new elevatorBlock(block, id, height, this));

        }


        
    }

    @Override
    public void onDisable()
    {
        //store the elevators to elevData
        storeData(elevators, elevData);

        //save elevator data
        saveData(elevData);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, java.lang.String label, java.lang.String[] args)
    {
        if(sender.hasPermission(cmd.getPermission()))
        {
            //getcoord
            //gives user his position
            if(cmd.getName().equalsIgnoreCase("getcoord") && sender instanceof Player)
            {
                Player a = (Player)sender;
                sender.sendMessage("x: " + a.getLocation().getBlockX() + " y: " + a.getLocation().getBlockY() + " z: " + a.getLocation().getBlockZ());
                return true;
            }
            //markelevatorblock
            //marks the block as an elevator
            else if(cmd.getName().equalsIgnoreCase("markelevatorblock"))
            {
                //if there are no location arguments, mark the block below the sender
                if (args.length == 2 && sender instanceof Player)
                {
                    //get the player
                    Player a = (Player) sender;

                    //get the location underneath the player
                    Location subLocation = new Location(a.getWorld(), a.getLocation().getBlockX(), a.getLocation().getBlockY() - 1, a.getLocation().getBlockZ());

                    //get the block underneath the player
                    Block subBlock = a.getWorld().getBlockAt(subLocation);

                    //loop through all elevators, exit if any use the same block of space as this one
                    for(elevatorBlock b : elevators)
                    {
                        if(b.getBlock().equals(subBlock))
                            return true;
                    }

                    //create the elevatorblock object and add it to the list of elevators
                    elevatorBlock b = new elevatorBlock(subBlock, args[0], Integer.parseInt(args[1]), this);
                    elevators.add(b);
                    return true;
                }
                //otherwise, act on the location arguments
            }

            //moves up the elevators with a given id
            else if(cmd.getName().equalsIgnoreCase("elevatorup"))
            {
                //excecute only if the arguments are valid
                if(args.length == 1)
                {
                    //loop through all the elevators, and move the ones
                    //with the given id
                    for(elevatorBlock a : elevators)
                    {
                        //debug line
                        //HungerCraftElevators.log.info("elevator acessed: " + a.getId());

                        if(a.getId().equalsIgnoreCase(args[0]))
                            a.moveUpOneFloor();
                    }
                }
                return true;
            }

            //moves down the elevators with a given id
            else if(cmd.getName().equalsIgnoreCase("elevatordown"))
            {
                //excecute only if the arguments are valid
                if(args.length == 1)
                {
                    //loop through all the elevators, and move the ones
                    //with the given id
                    for(elevatorBlock a : elevators)
                    {
                        //debug line
                        //HungerCraftElevators.log.info("elevator acessed: " + a.getId());

                        if(a.getId().equalsIgnoreCase(args[0]))
                            a.moveDownOneFloor();
                    }
                }
                return true;
            }

            //removes all elevators with the given id
            else if(cmd.getName().equalsIgnoreCase("removeelevatorid"))
            {
                //check arguments
                if(args.length == 1)
                {
                    //loop through every elevator and remove the ones
                    //with the given id
                    for(int i = 0; i < elevators.size(); i++)
                    {
                        //get the elevator at the loop id
                        elevatorBlock a = elevators.get(i);
                        
                        //check the elevators id against the given id
                        //delete it if it matches
                        //garage collection should delete the elevator
                        //from memory
                        if(a.getId().equalsIgnoreCase(args[0]))
                            elevators.remove(a);

                        //sice the elevator at id i was previously deleted
                        //i must run through the id again to hit the elevator
                        //that was dropped back
                        i--;
                    }
                    return true;
                }
            }

            //removes the last elevator created
            else if(cmd.getName().equalsIgnoreCase("removelastelevator"))
            {
                //remove the last elevator in the list
                elevators.remove(elevators.size()-1);
                return true;
            }
            
            else if(cmd.getName().equalsIgnoreCase("removeelevatorcoord"))
            {
                //check arguments
                if(args.length == 3)
                {
                    //loop through every elevator and delete the elevators
                    //with the given coordinates
                    for(int i = 0; i < elevators.size(); i++)
                    {
                        //get the elevator at the loop id
                        elevatorBlock a = elevators.get(i);

                        //location to check against
                        if(a.getBlock().getX() == Integer.parseInt(args[0]) && a.getBlock().getY() == Integer.parseInt(args[1]) && a.getBlock().getZ() == Integer.parseInt(args[2]))
                        {
                            elevators.remove(i);
                            i--;
                        }
                    }
                    return true;
                }
            }

            //allelevatorsup
            //moves all elevators up one floor
            else if(cmd.getName().equalsIgnoreCase("allelevatorsup"))
            {
                //loop through all elevators, moving all up
                //by one floor
                for(elevatorBlock a : elevators)
                {
                    //debug line
                    //HungerCraftElevators.log.info("elevator acessed: " + a.getId());

                    a.moveUpOneFloor();
                }
                return true;
            }

            //allelevatorsdown
            //moves all elevators down one floor
            else if(cmd.getName().equalsIgnoreCase("allelevatorsdown"))
            {
                //loop through all elevators, moving all up
                //by one floor
                for(elevatorBlock a : elevators)
                {
                    //debug line
                    //HungerCraftElevators.log.info("elevator acessed: " + a.getId());

                    a.moveDownOneFloor();
                }
                return true;
            }
        }
        return false;
    }

    //custom data file methods

    //load and get data (i find it slightly odd that
    //JavaPlugin separates the two)
    public FileConfiguration getData()
    {
        //the YAML File that holds the data
        File dataFile = new File(getDataFolder(), "ElevatorData.yml");

        //get the file's data into a FileConfiguration
        FileConfiguration rit = YamlConfiguration.loadConfiguration(dataFile);

        return rit;
    }

    //save the given file configuration to the data file
    public void saveData(FileConfiguration data)
    {
        if(data == null)
            return;
        try
        {
            data.save(getDataFolder() + "/ElevatorData.yml");
        }
        catch(IOException exep)
        {
            log.log(Level.SEVERE, "Could not save elevator data, some data may be corrupted");
        }
    }

    //store elevator data to the data file config
    //overwrites existing data
    public void storeData(List<elevatorBlock> data, FileConfiguration fileData)
    {
        //loop through each elevatorBlock, storing
        //the data for each
        for(int i = 0; i < data.size(); i++)
        {
            //get the elevator block at the loop id
            elevatorBlock a = data.get(i);

            /**
            *store the elevator data
            */

            fileData.set(i + ".height", a.getFloorHeight());//store floor height
            fileData.set(i + ".id", a.getId());//store id
            fileData.set(i + ".xpos", a.getBlock().getX());
            fileData.set(i + ".ypos", a.getBlock().getY());
            fileData.set(i + ".zpos", a.getBlock().getZ());
            fileData.set(i + ".world", a.getBlock().getWorld().getName());
        }
    }

}

class elevatorBlock
{
    private Block block;
    private String id;
    private int floorHeight;

    //regular constructor
    public elevatorBlock(Block block, String id, int height, HungerCraftElevators papa)
    {
        //debug line
        //HungerCraftElevators.log.info("Elevator constructed with height: " + height);

        this.block = block;
        this.id = id;
        floorHeight = height;
    }

    //movement
    public void moveUpOneBlock()
    {
        //the world containing the elevator
        World world = block.getWorld();

        //nextLocation up to be moved to
        Location nextLocation = new Location(world, block.getLocation().getBlockX(), block.getLocation().getBlockY()+1, block.getLocation().getBlockZ());

        //next block to be "moved" to
        Block nextBlock = world.getBlockAt(nextLocation);

        //exit if the next block is not empty
        if(nextBlock.getTypeId() != 0 && nextLocation.add(0,1,0).getBlock().getTypeId() != 0)
            return;

        //search for any players standing on the elevator
        //block and move them up with the elevator
        for(Player a : world.getPlayers())
        {
            //Debug line commented out
            //HungerCraftElevators.log.info("Player to move: " + a.getName() + " - "  + (a.getLocation().getBlock().equals(nextBlock)));
            if(a.getLocation().getBlock().equals(nextBlock))
            {
                a.teleport(a.getLocation().add(0, 1, 0));
            }
        }


        //move block up one block
        nextBlock.setTypeId(block.getTypeId());       
        block.setTypeId(0);
        block = nextBlock;
    }

    public void moveDownOneBlock()
    {
        //the world containing the elevator
        World world = block.getWorld();

        //nextLocation up to be moved to
        Location nextLocation = new Location(world, block.getLocation().getBlockX(), block.getLocation().getBlockY()-1, block.getLocation().getBlockZ());

        //next block to be "moved" to
        Block nextBlock = world.getBlockAt(nextLocation);

        //exit if the next block is not empty
        if(nextBlock.getTypeId() != 0 && nextLocation.add(0,-1,0).getBlock().getTypeId() != 0)
            return;

        //search for any players standing on the elevator
        //block and move them up with the elevator
        for(Player a : world.getPlayers())
        {
            //Debug line commented out
            //HungerCraftElevators.log.info("Player to move: " + a.getName() + " - "  + (a.getLocation().getBlock().equals(nextBlock)));
            if(a.getLocation().getBlock().equals(nextBlock))
            {
                a.teleport(a.getLocation().add(0, -1, 0));
            }
        }


        //move block down one block
        nextBlock.setTypeId(block.getTypeId());
        block.setTypeId(0);
        block = nextBlock;
    }

    public void moveUpOneFloor()
    {
        //debug line
        //HungerCraftElevators.log.info("Moving up: " + floorHeight);

        //move block up one block floorheight amount of times
        //note: the moveuponeblock command will halt itself
        //if it runs into a block
        for(int i = 0; i <floorHeight; i++)
        {
            moveUpOneBlock();
        }
    }

    public void moveDownOneFloor()
    {
        //debug line
        //HungerCraftElevators.log.info("Moving up: " + floorHeight);

        //move block up one block floorheight amount of times
        //note: the moveuponeblock command will halt itself
        //if it runs into a block
        for(int i = 0; i <floorHeight; i++)
        {
            moveDownOneBlock();
        }
    }

    //basic member encompasing
    public Block getBlock()
    {
        return block;
    }

    public void setBlock(Block block)
    {
        this.block = block;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String a)
    {
        id = a;
    }

    public int getFloorHeight()
    {
        return floorHeight;
    }

    public void setFloorHeight(int a)
    {
        floorHeight = a;
    }
}

