package net.runelite.client.plugins.chocogrinder.tasks;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Player;
import net.runelite.api.events.GameTick;
import net.runelite.client.plugins.chocogrinder.Task;
import net.runelite.client.plugins.chocogrinder.ChocoGrinderPlugin;
import static net.runelite.client.plugins.chocogrinder.ChocoGrinderPlugin.beforeLoc;
import static net.runelite.client.plugins.chocogrinder.ChocoGrinderPlugin.timeout;

@Slf4j
public class MovingTask extends Task
{

	@Override
	public boolean validate()
	{
		return playerUtils.isMoving(beforeLoc);
	}

	@Override
	public String getTaskDescription()
	{
		return ChocoGrinderPlugin.status;
	}

	@Override
	public void onGameTick(GameTick event)
	{
		Player player = client.getLocalPlayer();
		if (player != null)
		{
			playerUtils.handleRun(20, 30);
			timeout = tickDelay();
		}
	}
}