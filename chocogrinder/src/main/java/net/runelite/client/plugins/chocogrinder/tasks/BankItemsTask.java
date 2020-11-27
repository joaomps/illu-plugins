package net.runelite.client.plugins.chocogrinder.tasks;

import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ItemID;
import net.runelite.api.events.GameTick;
import net.runelite.client.plugins.chocogrinder.Task;
import net.runelite.client.plugins.chocogrinder.ChocoGrinderPlugin;
import static net.runelite.client.plugins.chocogrinder.ChocoGrinderPlugin.startBot;
import static net.runelite.client.plugins.chocogrinder.ChocoGrinderPlugin.status;
import net.runelite.client.plugins.iutils.ActionQueue;
import net.runelite.client.plugins.iutils.BankUtils;
import net.runelite.client.plugins.iutils.InventoryUtils;

import java.util.List;

@Slf4j
public class BankItemsTask extends Task
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
		return action.delayedActions.isEmpty() && !inventory.containsItem(ItemID.CHOCOLATE_BAR) &&
			bank.isOpen();
	}

	@Override
	public String getTaskDescription()
	{
		return ChocoGrinderPlugin.status;
	}

	@Override
	public void onGameTick(GameTick event)
	{
		long sleep = 0;
		if (inventory.getItemCount(ItemID.CHOCOLATE_DUST,false) == 27)
		{
			status = "Depositing items";
			bank.depositAllExcept(List.of(ItemID.KNIFE));
		}
		else
		{
			status = "Withdrawing items";
			if (bank.contains(ItemID.CHOCOLATE_BAR, 1))
			{
				bank.withdrawAllItem(ItemID.CHOCOLATE_BAR);
				bank.close();
			}
			else
			{
				status = "Out of chocolate bars to grind, stopping";
				utils.sendGameMessage(status);
				startBot = false;
			}
		}
		log.info(status);
	}
}