package com.wuest.prefab.Proxy.Messages;

import com.wuest.prefab.Config.*;
import com.wuest.prefab.Config.Structures.BasicStructureConfiguration;
import com.wuest.prefab.Config.Structures.ChickenCoopConfiguration;
import com.wuest.prefab.Config.Structures.FishPondConfiguration;
import com.wuest.prefab.Config.Structures.HorseStableConfiguration;
import com.wuest.prefab.Config.Structures.HouseConfiguration;
import com.wuest.prefab.Config.Structures.ModularHouseConfiguration;
import com.wuest.prefab.Config.Structures.MonsterMasherConfiguration;
import com.wuest.prefab.Config.Structures.NetherGateConfiguration;
import com.wuest.prefab.Config.Structures.ProduceFarmConfiguration;
import com.wuest.prefab.Config.Structures.StructureConfiguration;
import com.wuest.prefab.Config.Structures.TreeFarmConfiguration;
import com.wuest.prefab.Config.Structures.VillagerHouseConfiguration;
import com.wuest.prefab.Config.Structures.WareHouseConfiguration;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.EnumConnectionState;
import net.minecraftforge.fml.common.network.ByteBufUtils;

/**
 * 
 * @author WuestMan
 *
 */
public class StructureTagMessage extends TagMessage
{
	protected EnumStructureConfiguration structureConfig;
	
	/**
	 * Initializes a new instance of the StructureTagMessage class.
	 */
	public StructureTagMessage()
	{
	}

	/**
	 * Initializes a new instance of the StructureTagMessage class.
	 * @param tagMessage The message to send.
	 */
	public StructureTagMessage(NBTTagCompound tagMessage, EnumStructureConfiguration structureConfig)
	{
		super(tagMessage);
		
		this.structureConfig = structureConfig;
	}
	
	public EnumStructureConfiguration getStructureConfig()
	{
		return this.structureConfig;
	}
	
	public void setStructureConfig(EnumStructureConfiguration value)
	{
		this.structureConfig = value;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) 
	{
		// This class is very useful in general for writing more complex objects.
		NBTTagCompound tag = ByteBufUtils.readTag(buf);
		
		this.structureConfig = EnumStructureConfiguration.getFromIdentifier(tag.getInteger("config"));
		
		this.tagMessage = tag.getCompoundTag("dataTag");
	}

	@Override
	public void toBytes(ByteBuf buf) 
	{
		NBTTagCompound tag = new NBTTagCompound();
		tag.setInteger("config", this.structureConfig.identifier);
		tag.setTag("dataTag", this.tagMessage);
		
		ByteBufUtils.writeTag(buf, tag);
	}
	
	/**
	 * This enum is used to contain the structures which will be used in message handling.
	 * @author WuestMan
	 *
	 */
	public enum EnumStructureConfiguration
	{
		Basic(0, new BasicStructureConfiguration()),
		ChickenCoop(1, new ChickenCoopConfiguration()),
		AdvancedWareHouse(2, new WareHouseConfiguration()),
		FishPond(3, new FishPondConfiguration()),
		HorseStable(4, new HorseStableConfiguration()),
		ModularHouse(5, new ModularHouseConfiguration()),
		MonsterMasher(6, new MonsterMasherConfiguration()),
		NetherGate(7, new NetherGateConfiguration()),
		ProduceFarm(8, new ProduceFarmConfiguration()),
		StartHouse(9, new HouseConfiguration()),
		TreeFarm(10, new TreeFarmConfiguration()),
		VillagerHouses(11, new VillagerHouseConfiguration()),
		WareHouse(12, new WareHouseConfiguration());
		
		private <T extends StructureConfiguration> EnumStructureConfiguration(int identifier, T structureConfig)
		{
			this.identifier = identifier;
			this.structureConfig = structureConfig;
		}
		
		private int identifier;
		public StructureConfiguration structureConfig;
		
		public static EnumStructureConfiguration getFromIdentifier(int identifier)
		{
			for (EnumStructureConfiguration config : EnumStructureConfiguration.values())
			{
				if (config.identifier == identifier)
				{
					return config;
				}
			}
			
			return EnumStructureConfiguration.Basic;
		}
	}
}