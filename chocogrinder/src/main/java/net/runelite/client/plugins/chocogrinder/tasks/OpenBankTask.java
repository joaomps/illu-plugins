package net.runelite.client.plugins.chocogrinder.tasks;

import java.awt.Rectangle;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.GameTick;
import net.runelite.client.plugins.chocogrinder.Task;
import net.runelite.client.plugins.chocogrinder.ChocoGrinderPlugin;
import static net.runelite.client.plugins.chocogrinder.ChocoGrinderPlugin.status;
import net.runelite.client.plugins.iutils.ActionQueue;
import net.runelite.client.plugins.iutils.BankUtils;
import net.runelite.client.plugins.iutils.InventoryUtils;

@Slf4j
public class OpenBankTask extends Task
{

	@Inject
	ActionQueue action;

	@Inject
	InventoryUtils inventory;

	@Inject
	BankUtils bank;

	@Override
	public boolean validate()
	{
		return action.delayedActions.isEmpty() && inventory.containsItemAmount(ItemID.CHOCOLATE_DUST, 27,false,true) &&
			!bank.isOpen();
	}

	@Override
	public String getTaskDescription()
	{
		return ChocoGrinderPlugin.status;
	}

	@Override
	public void onGameTick(GameTick event)
	{
		WallObject bank = object.findNearestWallObject(config.bankID());
		if (bank != null)
		{
			status = "Opening bank";
			entry = new MenuEntry("Bank", "", bank.getId(), MenuOpcode.GAME_OBJECT_SECOND_OPTION.getId(),
				55, 49, false);
			Rectangle rectangle = (bank.getConvexHull() != null) ? bank.getConvexHull().getBounds() :
				new Rectangle(client.getCenterX() - 50, client.getCenterY() - 50, 100, 100);
			;
			utils.doActionMsTime(entry, rectangle, sleepDelay());
		}
		else
		{
			status = "Bank not found";
		}
		log.info(status);
	}
}