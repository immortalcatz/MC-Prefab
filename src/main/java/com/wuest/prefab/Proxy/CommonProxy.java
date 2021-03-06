package com.wuest.prefab.Proxy;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;

import com.google.common.io.Files;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wuest.prefab.ModRegistry;
import com.wuest.prefab.Prefab;
import com.wuest.prefab.UpdateChecker;
import com.wuest.prefab.Config.ModConfiguration;
import com.wuest.prefab.Events.ModEventHandler;
import com.wuest.prefab.Gui.GuiCustomContainer;
import com.wuest.prefab.crafting.ShapedRecipeForFile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;

/**
 * This is the server side proxy.
 * @author WuestMan
 *
 */
public class CommonProxy implements IGuiHandler
{
	public static ModEventHandler eventHandler = new ModEventHandler();
	public static ModConfiguration proxyConfiguration;

	/*
	 * Methods for ClientProxy to Override
	 */
	public void registerRenderers()
	{
	}
	
	public void preInit(FMLPreInitializationEvent event)
	{
		Prefab.network = NetworkRegistry.INSTANCE.newSimpleChannel("PrefabChannel");
		Prefab.config = new Configuration(event.getSuggestedConfigurationFile());
		Prefab.config.load();
		ModConfiguration.syncConfig();
		
		// Register messages.
		ModRegistry.RegisterMessages();
		
		// Register the capabilities.
		ModRegistry.RegisterCapabilities();
		
		// Register the recipes here.
		//ModRegistry.RegisterRecipes();
		
		// Make sure that the mod configuration is re-synced after loading all of the recipes.
		ModConfiguration.syncConfig();
	}
	
	public void init(FMLInitializationEvent event)
	{
		NetworkRegistry.INSTANCE.registerGuiHandler(Prefab.instance, Prefab.proxy);
		this.UpdateRegisteredRecipes();
	}
	
	public void postinit(FMLPostInitializationEvent event)
	{
		if (this.proxyConfiguration.enableVersionCheckMessage)
		{
			UpdateChecker.checkVersion();
		}
	}
	
	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		if (ID == ModRegistry.GuiDrafter)
		{
			return new GuiCustomContainer();
		}
		
		return ModRegistry.GetModGuiByID(ID, x, y, z);
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		return ModRegistry.GetModGuiByID(ID, x, y, z);
	}

	public ModConfiguration getServerConfiguration()
	{
		return CommonProxy.proxyConfiguration;
	}
	
	public void UpdateRegisteredRecipes()
	{
		// Remove recipes which are configured to not be set.
		ForgeRegistry<IRecipe> registry = (ForgeRegistry<IRecipe>) ForgeRegistries.RECIPES;
		HashMap<String, Boolean> recipeConfiguration = Prefab.proxy.getServerConfiguration().recipeConfiguration;
		
		for (Entry<ResourceLocation, IRecipe> entry : registry.getEntries())
		{
			if (entry.getKey().getResourceDomain().equals(Prefab.MODID))
			{
				// Check for normal case or lower-case situations.
				ResourceLocation recipeKey = recipeConfiguration.containsKey(entry.getValue().getGroup()) ? entry.getKey() : null;
				boolean shouldRemove = false;
				
				if (recipeKey == null)
				{
					String modifiedGroupName = entry.getValue().getGroup().replaceAll("prefab:", "").replaceAll("_", "");
					
					for (String recipeConfigurationKey : recipeConfiguration.keySet())
					{
						if (recipeConfigurationKey.toLowerCase().equals(modifiedGroupName))
						{
							recipeKey = entry.getKey();
							shouldRemove = !recipeConfiguration.get(recipeConfigurationKey);
							break;
						}
					}
				}
				else
				{
					shouldRemove = !recipeConfiguration.get(entry.getValue().getGroup());
				}
				
				if (recipeKey != null && shouldRemove)
				{
					registry.remove(recipeKey);
				}
				
/*				if (entry.getValue().getClass() == ShapedRecipes.class && !entry.getValue().getGroup().equals("Bundle of Timber"))
				{
					//ExclusionStrategy strategy = new CustomExclusionStrategy();
					
					//Gson converter = new GsonBuilder().setExclusionStrategies(strategy).setPrettyPrinting().create();
					Gson converter = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();
					StringWriter stringWriter = new StringWriter();
					
					ShapedRecipeForFile fileShape = ShapedRecipeForFile.createFromShapedRecipe((ShapedRecipes)entry.getValue());
					converter.toJson(fileShape, stringWriter);
					
					
					String jsonValue = stringWriter.toString();
					String fileLocation = "C:\\Temp\\" + entry.getValue().getRegistryName().getResourcePath().toLowerCase().replaceAll(" ", "_") + ".json";
					
					try
					{
						Files.write(jsonValue, new File(fileLocation), Charset.defaultCharset());
					}
					catch (IOException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}*/
			}
		}
		
		FMLCommonHandler.instance().resetClientRecipeBook();
	}
	
	public class CustomExclusionStrategy implements ExclusionStrategy
	{

		private ArrayList<String> allowedNames = new ArrayList<String>(Arrays.asList("group", "recipeWidth", "recipeHeight", "recipeItems", "recipeOutput", "matchingStacks"));

		@Override
		public boolean shouldSkipField(FieldAttributes f)
		{
			if (this.allowedNames.contains(f.getName()))
			{
				return false;
			}
			
			return true;
		}

		@Override
		public boolean shouldSkipClass(Class<?> clazz)
		{
			if (clazz == EntityPlayer.class)
			{
				return true;
			}
			
			return false;
		}
		
	}
}
