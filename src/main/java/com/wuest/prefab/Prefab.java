package com.wuest.prefab;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.google.common.collect.ImmutableList;
import com.wuest.prefab.Proxy.CommonProxy;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.RegistryEvent.MissingMappings;
import net.minecraftforge.event.RegistryEvent.MissingMappings.Mapping;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;

/**
 * The starting point to load all of the blocks, items and other objects
 * associated with this mod.
 * 
 * @author WuestMan
 *
 */
@Mod(modid = Prefab.MODID, version = Prefab.VERSION, acceptedMinecraftVersions = "[1.12]", guiFactory = "com.wuest.prefab.Gui.ConfigGuiFactory", updateJSON = "https://raw.githubusercontent.com/Brian-Wuest/MC-Prefab/master/changeLog.json")
public class Prefab
{
	/**
	 * This is the ModID
	 */
	public static final String MODID = "prefab";

	/**
	 * This is the current mod version.
	 */
	public static final String VERSION = "@VERSION@";

	/**
	 * This is used to determine if the mod is currently being debugged.
	 */
	public static boolean isDebug = false;

	/**
	 * This is the static instance of this class.
	 */
	@Instance(value = Prefab.MODID)
	public static Prefab instance;

	/**
	 * Says where the client and server 'proxy' code is loaded.
	 */
	@SidedProxy(clientSide = "com.wuest.prefab.Proxy.ClientProxy", serverSide = "com.wuest.prefab.Proxy.CommonProxy")
	public static CommonProxy proxy;

	/**
	 * The network class used to send messages.
	 */
	public static SimpleNetworkWrapper network;
	
	/**
	 * This is the configuration of the mod.
	 */
	public static Configuration config;

	static
	{
		Prefab.isDebug = java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments().toString().contains("-agentlib:jdwp");
	}

	/**
	 * The pre-initialization event.
	 * @param event The event from forge.
	 */
	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		Prefab.proxy.preInit(event);
	}

	/**
	 * The initialization event.
	 * @param event The event from forge.
	 */
	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		Prefab.proxy.init(event);
	}

	/**
	 * The post-initialization event.
	 * @param event The event from forge.
	 */
	@EventHandler
	public void postinit(FMLPostInitializationEvent event)
	{
		Prefab.proxy.postinit(event);
	}
	
	@EventHandler
	public void OnMissingBlockMapping(MissingMappings<Block> event) 
	{
		for (MissingMappings.Mapping<Block> entry : event.getMappings())
		{
				Block mappedBlock = null;
				
				switch (entry.key.getResourcePath())
				{
					case "blockcompressedstone":
					case "blockCompressedStone":
					{
						mappedBlock = ModRegistry.CompressedStoneBlock();
					}
				}
				
				if (mappedBlock != null)
				{
					entry.remap(mappedBlock);
				}
		}
	}
	
	@EventHandler
	public void OnMissingMapping(MissingMappings<Item> event) 
	{
		ImmutableList missingMappings = event.getMappings();
		
		for (MissingMappings.Mapping<Item> mapping : event.getMappings())
		{
			Item mappedItem = null;
			
			switch (mapping.key.getResourcePath())
			{
				case "blockcompressedstone":
				case "blockCompressedStone":
				{
					mappedItem = ModRegistry.ModItems.stream().filter(item -> item.getRegistryName().getResourcePath().equals("block_compressed_stone")).findFirst().get();
					break;
				}
				
				case "itemproducefarm" :
				case "itemProduceFarm" :
				{
					mappedItem = ModRegistry.ProduceFarm();
					break;
				}
				
				case "itempileofbricks" :
				case "itemPileOfBricks" :
				{
					mappedItem = ModRegistry.PileOfBricks();
					break;
				}
				
				case "itemhorsestable" :
				case "itemHorseStable" :
				{
					mappedItem = ModRegistry.HorseStable();
					break;
				}
				
				case "itemnethergate" :
				case "itemNetherGate" :
				{
					mappedItem = ModRegistry.NetherGate();
					break;
				}
				
				case "itemwarehouseupgrade" :
				case "itemWareHouseUpgrade" :
				{
					mappedItem = ModRegistry.WareHouseUpgrade();
					break;
				}
				
				case "itemchickencoop" :
				case "itemChickenCoop" :
				{
					mappedItem = ModRegistry.ChickenCoop();
					break;
				}
				
				case "itemtreefarm" :
				case "itemTreeFarm" :
				{
					mappedItem = ModRegistry.TreeFarm();
					break;
				}
				
				case "itemcompressedchest" :
				case "itemCompressedChest" :
				{
					mappedItem = ModRegistry.CompressedChestItem();
					break;
				}
				
				case "itembundleoftimber" :
				case "itemBundleOfTimber" :
				{
					mappedItem = ModRegistry.BundleOfTimber();
					break;
				}
				
				case "itemwarehouse" :
				case "itemWareHouse" :
				{
					mappedItem = ModRegistry.WareHouse();
					break;
				}
				
				case "itempalletofbricks" :
				case "itemPalletOfBricks" :
				{
					mappedItem = ModRegistry.PalletOfBricks();
					break;
				}
				
				case "itemfishpond" :
				case "itemFishPond" :
				{
					mappedItem = ModRegistry.FishPond();
					break;
				}
				
				case "itemmonstermasher" :
				case "itemMonsterMasher" :
				{
					mappedItem = ModRegistry.MonsterMasher();
					break;
				}
				
				case "itemstarthouse" :
				case "itemStartHouse" :
				{
					mappedItem = ModRegistry.StartHouse();
					break;
				}
				
				case "itemadvancedwarehouse" :
				case "itemAdvancedWareHouse" :
				{
					mappedItem = ModRegistry.AdvancedWareHouse();
					break;
				}
			}
			
			if (mappedItem != null)
			{
				mapping.remap(mappedItem);
			}
		}
	}
}
