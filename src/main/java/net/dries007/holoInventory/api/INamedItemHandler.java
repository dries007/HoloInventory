package net.dries007.holoInventory.api;

public interface INamedItemHandler {

	/*
	 * Option for other mods using IItemHandler to have their names properly detected
	 */
	String getItemHandlerName();
}