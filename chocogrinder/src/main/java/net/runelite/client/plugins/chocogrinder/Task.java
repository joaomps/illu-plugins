package net.runelite.client.plugins.chocogrinder;

import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.GameTick;
import static net.runelite.client.plugins.chocogrinder.ChocoGrinderPlugin.sleepLength;
import static net.runelite.client.plugins.chocogrinder.ChocoGrinderPlugin.tickLength;
import net.runelite.client.plugins.iutils.CalculationUtils;
import net.runelite.client.plugins.iutils.MenuUtils;
import net.runelite.client.plugins.iutils.MouseUtils;
import net.runelite.client.plugins.iutils.ObjectUtils;
import net.runelite.client.plugins.iutils.PlayerUtils;
import net.runelite.client.plugins.iutils.iUtils;

@Slf4j
public abstract class Task
{
	public Task()
	{
	}

	@Inject
	public Client client;

	@Inject
	public ChocoGrinderConfig config;

	@Inject
	public iUtils utils;

	@Inject
	public MenuUtils menu;

	@Inject
	public MouseUtils mouse;

	@Inject
	public CalculationUtils calc;

	@Inject
	public PlayerUtils playerUtils;

	@Inject
	public ObjectUtils object;

	public MenuEntry entry;

	public abstract boolean validate();

	public long sleepDelay()
	{
		sleepLength = calc.randomDelay(config.sleepWeightedDistribution(), config.sleepMin(), config.sleepMax(), config.sleepDeviation(), config.sleepTarget());
		return sleepLength;
	}

	public int tickDelay()
	{
		tickLength = (int) calc.randomDelay(config.tickDelayWeightedDistribution(), config.tickDelayMin(), config.tickDelayMax(), config.tickDelayDeviation(), config.tickDelayTarget());
		return tickLength;
	}

	public String getTaskDescription()
	{
		return this.getClass().getSimpleName();
	}

	public void onGameTick(GameTick event)
	{
		return;
	}

}
