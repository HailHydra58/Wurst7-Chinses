/*
 * Copyright (c) 2014-2023 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import net.minecraft.util.math.Box;
import net.wurstclient.Category;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;

public final class ParkourHack extends Hack implements UpdateListener
{
	private final SliderSetting minDepth = new SliderSetting("最小深度",
		"如果坑洞不至少有这么深，Parkour 将不会跳过它。\n"
			+ "增加以阻止 Parkour 跳下楼梯。\n"
			+ "减小以使 Parkour 在地毯边缘跳跃。",
		0.5, 0.05, 10, 0.05, ValueDisplay.DECIMAL.withSuffix("m"));

	private final SliderSetting edgeDistance =
		new SliderSetting("边缘距离",
			"在跳跃之前，Parkour 允许您离边缘有多近。",
			0.001, 0.001, 0.25, 0.001, ValueDisplay.DECIMAL.withSuffix("m"));

	public ParkourHack()
	{
		super("Parkour", "自动起跳");
		setCategory(Category.MOVEMENT);
		addSetting(minDepth);
		addSetting(edgeDistance);
	}

	@Override
	public void onEnable()
	{
		WURST.getHax().safeWalkHack.setEnabled(false);
		EVENTS.add(UpdateListener.class, this);
	}

	@Override
	public void onDisable()
	{
		EVENTS.remove(UpdateListener.class, this);
	}

	@Override
	public void onUpdate()
	{
		if(!MC.player.isOnGround() || MC.options.jumpKey.isPressed())
			return;

		if(MC.player.isSneaking() || MC.options.sneakKey.isPressed())
			return;

		Box box = MC.player.getBoundingBox();
		Box adjustedBox = box.stretch(0, -minDepth.getValue(), 0)
			.expand(-edgeDistance.getValue(), 0, -edgeDistance.getValue());

		if(!MC.world.isSpaceEmpty(MC.player, adjustedBox))
			return;

		MC.player.jump();
	}
}
