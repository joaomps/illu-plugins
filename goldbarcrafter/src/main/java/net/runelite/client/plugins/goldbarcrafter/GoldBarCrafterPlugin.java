/*
 * Copyright (c) 2018, SomeoneWithAnInternetConnection
 * Copyright (c) 2018, oplosthee <https://github.com/oplosthee>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.goldbarcrafter;

import com.google.inject.Provides;
import com.owain.chinbreakhandler.ChinBreakHandler;

import java.awt.*;
import java.time.Instant;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.events.ConfigButtonClicked;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.plugins.iutils.*;
import net.runelite.client.ui.overlay.OverlayManager;
import org.pf4j.Extension;
import static net.runelite.client.plugins.goldbarcrafter.GoldBarCrafterState.*;

@Extension
@PluginDependency(iUtils.class)
@PluginDescriptor(
	name = "GoldBarCrafter",
	enabledByDefault = false,
	description = "Nox - Gold Bar Crafter",
	tags = {"nox", "crafting", "bot"},
	type = PluginType.SKILLING
)
@Slf4j
public class GoldBarCrafterPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private iUtils utils;

	@Inject
	private NPCUtils npc;

	@Inject
	private InventoryUtils inventory;

	@Inject
	private BankUtils bank;

	@Inject
	private CalculationUtils calc;

	@Inject
	private MenuUtils menu;

	@Inject
	private MouseUtils mouse;

	@Inject
	private ObjectUtils object;

	@Inject
	private PlayerUtils playerUtils;

	@Inject
	private InterfaceUtils interfaceUtils;

	@Inject
	private GoldBarCrafterConfig config;

	@Inject
	PluginManager pluginManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private GoldBarCrafterOverlay overlay;

	@Inject
	public ChinBreakHandler chinBreakHandler;

	GoldBarCrafterState state;
	MenuEntry targetMenu;
	Instant botTimer;
	Player player;
	Items item;
	int timeout = 0;
	long sleepLength = 0;
	String status;
	boolean startBot;

	@Provides
	GoldBarCrafterConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(GoldBarCrafterConfig.class);
	}

	@Subscribe
	private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked)
	{
		if (!configButtonClicked.getGroup().equalsIgnoreCase("GoldBarCrafter"))
		{
			return;
		}
		log.info("button {} pressed!", configButtonClicked.getKey());
		switch (configButtonClicked.getKey()) {
			case "startButton":
				if (!startBot) {
					startBot = true;
					chinBreakHandler.startPlugin(this);
					botTimer = Instant.now();
					status = "Starting..";
					state = null;
					targetMenu = null;
					timeout = 0;
					initVals();
					overlayManager.add(overlay);
				}
				else
				{
					resetVals();
				}
		}
	}

	@Subscribe
	private void onConfigChanged(ConfigChanged event)
	{
		if (event.getGroup() != "GoldBarCrafter")
		{
			return;
		}
		switch (event.getKey())
		{
			case "getItem":
				item = config.getItem();
				log.debug("Item set to {}", item.getName());
				break;
		}
	}

	@Override
	protected void startUp()
	{
		chinBreakHandler.registerPlugin(this);
	}

	@Override
	protected void shutDown()
	{
		resetVals();
		chinBreakHandler.unregisterPlugin(this);
	}

	public void initVals()
	{
		item = config.getItem();
	}

	public void resetVals()
	{
		overlayManager.remove(overlay);
		chinBreakHandler.stopPlugin(this);
		startBot = false;
		botTimer = null;
		item = null;
		timeout = 0;
		targetMenu = null;
	}

	public long sleepDelay()
	{
		sleepLength = calc.randomDelay(config.sleepWeightedDistribution(), config.sleepMin(), config.sleepMax(), config.sleepDeviation(), config.sleepTarget());
		return sleepLength;
	}

	private int tickDelay() {
		int tickLength = (int) calc.randomDelay(config.tickDelayWeightedDistribution(), config.tickDelayMin(), config.tickDelayMax(), config.tickDelayDeviation(), config.tickDelayTarget());
		return tickLength;
	}

	private void clickWidget()
	{
		Widget craftwidget = client.getWidget(446,0);
		if (craftwidget != null)
		{
			targetMenu = new MenuEntry(item.getMakeString(),"", 1 , MenuOpcode.CC_OP.getId(),
					-1, item.getOptionId(),false);
			menu.setEntry(targetMenu);
			utils.doActionMsTime(targetMenu,new Point(0,0),sleepDelay());
		}
		else
		{
			utils.sendGameMessage("Widget not found.");
			startBot = false;
		}
	}

	private void clickFurnace()
	{
//		playerUtils.enableRun(client.getWidget(WidgetInfo.MINIMAP_RUN_ORB).getBounds());
		utils.sleep(200,450);
		GameObject furnace = object.findNearestGameObject(ObjectID.FURNACE_16469);
		if (furnace !=null)
		{
			targetMenu = new MenuEntry("","", furnace.getId(), MenuOpcode.GAME_OBJECT_SECOND_OPTION.getId(),
					furnace.getLocalLocation().getSceneX(), furnace.getLocalLocation().getSceneY(),true);
			menu.setEntry(targetMenu);
			mouse.delayMouseClick(furnace.getConvexHull().getBounds(),sleepDelay());
		}
		else
		{
			utils.sendGameMessage("Furnace not found.");
			startBot = false;
		}
	}

	private void openBank()
	{
//		playerUtils.enableRun(client.getWidget(WidgetInfo.MINIMAP_RUN_ORB).getBounds());
		utils.sleep(200,450);
		GameObject bank = object.findNearestGameObject(10355);
		if (bank != null)
		{
			status = "Opening bank";
			targetMenu = new MenuEntry("", "", bank.getId(), MenuOpcode.GAME_OBJECT_SECOND_OPTION.getId(),
					bank.getSceneMinLocation().getX(), bank.getSceneMinLocation().getY(), false);
			Rectangle rectangle = (bank.getConvexHull() != null) ? bank.getConvexHull().getBounds() :
					new Rectangle(client.getCenterX() - 50, client.getCenterY() - 50, 100, 100);
			;
			utils.doActionMsTime(targetMenu, rectangle, sleepDelay());
		}
		else
		{
			utils.sendGameMessage("Bank not found");
			startBot = false;

		}
	}

	private void withdrawBars()
	{
		bank.withdrawAllItem(ItemID.GOLD_BAR);
	}

	private void withdrawMould()
	{
		bank.withdrawItemAmount(item.getMould(),1);
	}

	private void depositItems()
	{
		bank.depositAllExcept(List.of(item.getMould()));
	}

	public GoldBarCrafterState getState()
	{
		if (timeout > 0)
		{
			return TIMEOUT;
		}

		if (player.getPoseAnimation() == 819 || (player.getPoseAnimation() == 824) ||
				(player.getPoseAnimation() == 1205) || (player.getPoseAnimation() == 1210))
		{
			status = "Walking to furnace";
			return MOVING;
		}

		if (player.getAnimation() == 899 || player.getAnimation() == 827) {
			status = "Crafting";
			return CRAFTING;
		}

		if (bank.isOpen())
		{
			if (!inventory.containsItem(item.getMould()))
			{
				status = "Getting a mould";
				return WITHDRAW_MOULD;
			}
			if (!bank.containsAnyOf(ItemID.GOLD_BAR) && !inventory.containsItem(ItemID.GOLD_BAR))
			{
				status = "Out of bars, logging out";
				return OUT_OF_BARS;
			}
			if (inventory.containsItem(item.getItemId()))
			{
				status = "Depositing items";
				return DEPOSIT_ITEMS;
			}
			if (chinBreakHandler.shouldBreak(this) && !inventory.containsItem(item.getItemId()))
			{
				status = "Breaking";
				return HANDLE_BREAK;
			}

			if (bank.containsAnyOf(ItemID.GOLD_BAR) && !inventory.isFull())
			{
				status = "Withdrawing bars";
				return WITHDRAW_BAR;
			}
			if (inventory.containsItem(ItemID.GOLD_BAR))
			{
				return CLOSE_BANK;
			}
		}

		Widget craftwidget = client.getWidget(446,0);
		if (craftwidget != null)
		{
			status = "Clicking crafting widget";
			return CRAFTING_WIDGET;
		}

		if (inventory.containsItem(ItemID.GOLD_BAR))
		{
			status = "Walking to furnace";
			return FURNACE;
		}

		Widget lvlup = client.getWidget(WidgetInfo.LEVEL_UP_SKILL);
		if (lvlup != null)
		{
			if (inventory.containsItem(ItemID.GOLD_BAR))
			{
				return FURNACE;
			}
			else
			{
				return OPEN_BANK;
			}
		}


		if ((player.getPoseAnimation() == 813 || (player.getPoseAnimation() == 808)) && (inventory.isEmpty() || !inventory.containsItem(ItemID.GOLD_BAR)))
		{
			status = "Walking to bank";
			return OPEN_BANK;
		}

		status = "Idling";
		return IDLING;
	}

	@Subscribe
	private void onGameTick(GameTick event)
	{
		if (!startBot || chinBreakHandler.isBreakActive(this))
		{
			return;
		}
		player = client.getLocalPlayer();
		if (client != null && player != null && client.getGameState() == GameState.LOGGED_IN) {
			if (!client.isResized()) {
				utils.sendGameMessage("c17 - client must be set to resizable");
				startBot = false;
				return;
			}
			state = getState();
			switch (state)
			{
				case TIMEOUT:
					timeout--;
					break;
				case ITERATING:
					break;
				case IDLING:
					timeout = 1;
					break;
				case MOVING:
					timeout = 1;
					break;
				case CRAFTING:
					timeout = 3;
					break;
				case OPEN_BANK:
					openBank();
					timeout = 0 + tickDelay();
					break;
				case CLOSE_BANK:
					bank.close();
					timeout = 0 + tickDelay();
					break;
				case DEPOSIT_ITEMS:
					depositItems();
					timeout = 0 + tickDelay();
					break;
				case WITHDRAW_BAR:
					withdrawBars();
					timeout = 0 + tickDelay();
					break;
				case WITHDRAW_MOULD:
					withdrawMould();
					timeout = 0 + tickDelay();
					break;
				case FURNACE:
					clickFurnace();
					timeout = 0 + tickDelay();
					break;
				case CRAFTING_WIDGET:
					clickWidget();
					timeout = 0 + tickDelay();
					break;
				case OUT_OF_BARS:
					if (config.logout())
					{
						interfaceUtils.logout();
					}
					startBot = false;
					resetVals();
					break;
				case HANDLE_BREAK:
					chinBreakHandler.startBreak(this);
					timeout = 8;
			}
		}
	}
}