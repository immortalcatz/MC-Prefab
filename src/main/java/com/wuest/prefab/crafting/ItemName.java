package com.wuest.prefab.crafting;

import com.google.gson.annotations.Expose;

/**
 * 
 * @author WuestMan
 *
 */
public class ItemName
{
	@Expose
	public String item;
	
	@Expose
	public Integer data;

	public ItemName()
	{
		this.item = "";
	}
}