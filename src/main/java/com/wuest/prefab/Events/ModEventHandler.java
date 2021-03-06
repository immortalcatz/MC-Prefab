package com.wuest.prefab.Events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import com.wuest.prefab.BuildingMethods;
import com.wuest.prefab.ModRegistry;
import com.wuest.prefab.Prefab;
import com.wuest.prefab.Capabilities.StructureConfigurationCapability;
import com.wuest.prefab.Capabilities.StructureConfigurationProvider;
import com.wuest.prefab.Config.EntityPlayerConfiguration;
import com.wuest.prefab.Config.ModConfiguration;
import com.wuest.prefab.Items.Structures.ItemBasicStructure;
import com.wuest.prefab.Proxy.ClientProxy;
import com.wuest.prefab.Proxy.Messages.ConfigSyncMessage;
import com.wuest.prefab.Proxy.Messages.PlayerEntityTagMessage;
import com.wuest.prefab.StructureGen.BuildBlock;
import com.wuest.prefab.StructureGen.Structure;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IForgeRegistry;

/**
 * This is the server side event hander.
 * @author WuestMan
 */
@EventBusSubscriber(value = {Side.SERVER, Side.CLIENT })
public class ModEventHandler
{
	/**
	 * Contains a hashmap for the structures to build and for whom.
	 */
	public static HashMap<EntityPlayer, ArrayList<Structure>> structuresToBuild = new HashMap<EntityPlayer, ArrayList<Structure>>();
	
	static
	{
		ModEventHandler.RedstoneAffectedBlockPositions = new ArrayList<BlockPos>();
	}
	
	/**
	 * Determines the affected blocks by redstone power.
	 */
	public static ArrayList<BlockPos> RedstoneAffectedBlockPositions = new ArrayList<BlockPos>();
	
	/**
	 * This event is used to determine if the player should be given the starting house item when they log in.
	 * @param event The event object.
	 */
	@SubscribeEvent
	public static void PlayerJoinedWorld(EntityJoinWorldEvent event)
	{
		if (!event.getWorld().isRemote && event.getEntity() instanceof EntityPlayerMP) 
		{
			System.out.println("Player joined world, checking to see if the house builder should be provided.");

			EntityPlayerMP player = (EntityPlayerMP)event.getEntity();
			EntityPlayerConfiguration playerConfig = EntityPlayerConfiguration.loadFromEntityData((EntityPlayerMP)event.getEntity());
			
			if (!playerConfig.givenHouseBuilder && Prefab.proxy.proxyConfiguration.addHouseItem)
			{
				ItemStack stack = new ItemStack(ModRegistry.StartHouse());
				player.inventory.addItemStackToInventory(stack);
				player.inventoryContainer.detectAndSendChanges();

				// Make sure to set the tag for this player so they don't get the item again.
				playerConfig.givenHouseBuilder = true;
				playerConfig.saveToPlayer(player);
			}
			
			// Send the persist tag to the client.
			Prefab.network.sendTo(new PlayerEntityTagMessage(playerConfig.getModIsPlayerNewTag(player)), player);
		}
	}
	
	/**
	 * Attaches the structure configuration capability to itemstacks.
	 * @param event The event object.
	 */
	@SubscribeEvent
	public static void AttachItemStackCapabilities(AttachCapabilitiesEvent<ItemStack> event)
	{
		if (event.getObject().getItem() instanceof ItemBasicStructure)
		{
			event.addCapability(new ResourceLocation(Prefab.MODID, "structuresconfiguration"), new StructureConfigurationProvider(new StructureConfigurationCapability()));
		}
	}
	
	/**
	 * This event is primarily used to build 100 blocks for any queued structures for all players.
	 * @param event The event object.
	 */
	@SubscribeEvent
	public static void onServerTick(ServerTickEvent event)
	{
		ArrayList<EntityPlayer> playersToRemove = new ArrayList<EntityPlayer>();
		
		for (Entry<EntityPlayer, ArrayList<Structure>> entry : ModEventHandler.structuresToBuild.entrySet())
		{
			ArrayList<Structure> structuresToRemove = new ArrayList<Structure>();
			
			// Build the first 100 blocks of each structure for this player.
			for (Structure structure : entry.getValue())
			{
				for (int i = 0; i < 100; i++)
				{
					// Structure clearing happens before anything else.
					if (structure.clearedBlockPos.size() > 0)
					{
						BlockPos currentPos = structure.clearedBlockPos.get(0);
						structure.clearedBlockPos.remove(0);
						
						IBlockState clearBlockState = structure.world.getBlockState(currentPos);
						
						// If this block is not specifically air then set it to air.
						// This will also break other mod's logic blocks but they would probably be broken due to structure generation anyways.
						if (clearBlockState.getBlock() != Blocks.AIR)
						{
							structure.world.setBlockToAir(currentPos);
						}
						else
						{
							// This is just an air block, move onto the next block don't need to wait for the next tick.
							i--;
						}
						
						continue;
					}
					
					BuildBlock currentBlock = null;
					
					if (structure.priorityOneBlocks.size() > 0)
					{
						currentBlock = structure.priorityOneBlocks.get(0);
						structure.priorityOneBlocks.remove(0);
					}
					else if (structure.priorityTwoBlocks.size() > 0)
					{
						currentBlock = structure.priorityTwoBlocks.get(0);
						structure.priorityTwoBlocks.remove(0);
					}
					else if (structure.priorityThreeBlocks.size() > 0)
					{
						currentBlock = structure.priorityThreeBlocks.get(0);
						structure.priorityThreeBlocks.remove(0);
					}
					else
					{
						// There are no more blocks to set.
						structuresToRemove.add(structure);
						break;
					}
					
					IBlockState state = currentBlock.getBlockState();
					
					BuildingMethods.ReplaceBlock(structure.world, currentBlock.getStartingPosition().getRelativePosition(structure.originalPos, structure.configuration.houseFacing), state);
					
					// After placing the initial block, set the sub-block. This needs to happen as the list isn't always in the correct order.
					if (currentBlock.getSubBlock() != null)
					{
						BuildBlock subBlock = currentBlock.getSubBlock();
						
						BuildingMethods.ReplaceBlock(structure.world, subBlock.getStartingPosition().getRelativePosition(structure.originalPos, structure.configuration.houseFacing), subBlock.getBlockState());
					}
				}
			}
			
			for (Structure structure : structuresToRemove)
			{
				// This structure is done building. Do any post-building operations.
				structure.AfterBuilding(structure.configuration, structure.world, structure.originalPos, structure.assumedNorth, entry.getKey());
				entry.getValue().remove(structure);
			}
			
			if (entry.getValue().size() == 0)
			{
				playersToRemove.add(entry.getKey());
			}
		}
		
		// Remove each player that has their structure's built.
		for (EntityPlayer player : playersToRemove)
		{
			ModEventHandler.structuresToBuild.remove(player);
		}
	}
	
	/**
	 * This event occurs when a player logs in. This is used to send server configuration to the client.
	 * @param event The event object.
	 */
	@SubscribeEvent
	public static void onPlayerLoginEvent(PlayerLoggedInEvent event)
	{
		if(!event.player.world.isRemote)
		{
			NBTTagCompound tag = Prefab.proxy.proxyConfiguration.ToNBTTagCompound();
			Prefab.network.sendTo(new ConfigSyncMessage(tag), (EntityPlayerMP)event.player);
			System.out.println("Sent config to '" + event.player.getDisplayNameString() + ".'");
		}
	}
	
	/**
	 * This event is used to clear out the server configuration for clients that log off the server.
	 * @param event The event object.
	 */
	@SubscribeEvent
	public static void onPlayerLoggedOutEvent(PlayerLoggedOutEvent event)
	{
		// When the player logs out, make sure to re-set the server configuration. 
		// This is so a new configuration can be successfully loaded when they switch servers or worlds (on single player.
		if (event.player.world.isRemote)
		{
			// Make sure to null out the server configuration from the client.
			((ClientProxy)Prefab.proxy).serverConfiguration = null;
		}
	}
	
	/**
	 * This occurs when a player dies and is used to make sure that a player does not get a duplicate starting house.
	 * @param event
	 */
	@SubscribeEvent
	public static void onClone(PlayerEvent.Clone event) 
	{
		if (event.getEntityPlayer() instanceof EntityPlayerMP)
		{
			// Don't add the tag unless the house item was added. This way it can be added if the feature is turned on.
			// When the player is cloned, make sure to copy the tag. If this is not done the item can be given to the player again if they die before the log out and log back in.
			NBTTagCompound originalTag = event.getOriginal().getEntityData();
	
			// Use the server configuration to determine if the house should be added for this player.
			if (Prefab.proxy.proxyConfiguration.addHouseItem)
			{
				if (originalTag.hasKey(EntityPlayerConfiguration.PLAYER_ENTITY_TAG))
				{
					NBTTagCompound newPlayerTag = event.getEntityPlayer().getEntityData();
					newPlayerTag.setTag(EntityPlayerConfiguration.PLAYER_ENTITY_TAG, originalTag.getTag(EntityPlayerConfiguration.PLAYER_ENTITY_TAG));
					
					// Send the persist tag to the client.
					Prefab.network.sendTo(new PlayerEntityTagMessage(originalTag.getCompoundTag(EntityPlayerConfiguration.PLAYER_ENTITY_TAG)), (EntityPlayerMP)event.getEntityPlayer());
				}
			}
		}
	}

	/**
	 * This is used to sync up the configuration when it's change by the user.
	 * @param onConfigChangedEvent The event object.
	 */
	@SubscribeEvent
	public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent onConfigChangedEvent)
	{
		if(onConfigChangedEvent.getModID().equals(Prefab.MODID))
		{
			ModConfiguration.syncConfig();
		}
	}


	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event)
	{	
		event.getRegistry().registerAll(ModRegistry.ModBlocks.toArray(new Block[ModRegistry.ModBlocks.size()]));
	}
	
	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event)
	{	
		event.getRegistry().registerAll(ModRegistry.ModItems.toArray(new Item[ModRegistry.ModItems.size()]));
	}
	
	@SubscribeEvent
	public static void registerRecipes(RegistryEvent.Register<IRecipe> event)
	{		
		// Only register capability recipes here.
		ModRegistry.RegisterRecipes();
	}
}