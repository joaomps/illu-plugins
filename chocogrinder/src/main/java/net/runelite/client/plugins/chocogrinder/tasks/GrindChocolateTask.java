package net.runelite.client.plugins.chocogrinder.tasks;

import java.awt.Rectangle;
import java.util.List;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ItemID;
import net.runelite.api.MenuEntry;
import net.runelite.api.MenuOpcode;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.plugins.chocogrinder.Task;
import net.runelite.client.plugins.chocogrinder.ChocoGrinderPlugin;
import static net.runelite.client.plugins.chocogrinder.ChocoGrinderPlugin.status;
import net.runelite.client.plugins.iutils.ActionQueue;
import net.runelite.client.plugins.iutils.InventoryUtils;

@Slf4j
public class GrindChocolateTask extends Task
{

	@Inject
	ActionQueue action;

	@Inject
	InventoryUtils inventory;

	@Override
	public boolean validate()
	{
		return action.delayedActions.isEmpty() && !playerUtils.isAnimating() && inventory.containsAllOf(List.of(ItemID.KNIFE, ItemID.CHOCOLATE_BAR));
	}

	@Override
	public String getTaskDescription()
	{
		return ChocoGrinderPlugin.status;
	}

	@Override
	public void onGameTick(GameTick event)
	{
		status = "Starting grinding chocolate";
		if(ChocoGrinderPlugin.usedKnife && !playerUtils.isAnimating()){
			utils.sleep(200,600);
			entry = new MenuEntry("Use", "<col=ff9040>Knife<col=ffffff> -> <col=ffff>Chocolate bar",
					ItemID.CHOCOLATE_BAR, MenuOpcode.ITEM_USE_ON_WIDGET_ITEM.getId(), inventory.getWidgetItem(ItemID.CHOCOLATE_BAR).getIndex(),
					9764864, false);
			menu.setEntry(entry);
			mouse.delayMouseClick(inventory.getWidgetItem(ItemID.CHOCOLATE_BAR).getCanvasBounds(), sleepDelay());

			status = "Grinding";
			ChocoGrinderPlugin.usedKnife = false;
		}
		else
		{
			useKnife();;
		}


		log.info(status);
	}

	public void useKnife(){
		WidgetItem knife = inventory.getWidgetItem(ItemID.KNIFE);
		entry = new MenuEntry("Use", "Use", ItemID.KNIFE, MenuOpcode.ITEM_USE.getId(),
				knife.getIndex(), 9764864, false);
		menu.setEntry(entry);
		mouse.delayMouseClick(knife.getCanvasBounds(), sleepDelay());
		ChocoGrinderPlugin.usedKnife = true;
	}
}