package com.wuest.prefab;

import com.wuest.prefab.Items.Structures.ItemBasicStructure;
import com.wuest.prefab.Render.PrefabModelMesher;

import net.minecraft.client.Minecraft;

/**
 * This class is used as the registry class for client-side only items.
 * @author WuestMan
 *
 */
public class ClientModRegistry extends ModRegistry
{
	/**
	 * This method is used to register model meshers for specific items.
	 */
	public static void RegisterModelMeshers()
	{
		ItemBasicStructure item = ModRegistry.BasicStructure();
		
		// Register the model mesher for this item.
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, new PrefabModelMesher());
	}
}
