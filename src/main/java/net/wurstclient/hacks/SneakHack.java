/*
 * Copyright (c) 2014-2023 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.PostMotionListener;
import net.wurstclient.events.PreMotionListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.mixinterface.IKeyBinding;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.EnumSetting;

@SearchTags({"AutoSneaking"})
public final class SneakHack extends Hack
	implements PreMotionListener, PostMotionListener
{
	private final EnumSetting<SneakMode> mode = new EnumSetting<>("模式",
		"\u00a7lPacket\u00a7r 使您看起来像在悄悄行动，而不会减慢您的速度。\n"
			+ "\u00a7lLegit\u00a7r 实际上会让您悄悄行动。",
		SneakMode.values(), SneakMode.LEGIT);

	private final CheckboxSetting offWhileFlying =
		new CheckboxSetting("在飞行时关闭",
			"在您飞行或使用自由视角时，会自动禁用 Legit 悄悄行动，以免强制您下降。\n\n"
				+ "请注意，这也意味着在执行这些操作时，其他玩家将看到您。",
			false);

	public SneakHack()
	{
		super("Sneak", "保持潜行");
		setCategory(Category.MOVEMENT);
		addSetting(mode);
		addSetting(offWhileFlying);
	}

	@Override
	public String getRenderName()
	{
		return getName() + " [" + mode.getSelected() + "]";
	}

	@Override
	public void onEnable()
	{
		EVENTS.add(PreMotionListener.class, this);
		EVENTS.add(PostMotionListener.class, this);
	}

	@Override
	public void onDisable()
	{
		EVENTS.remove(PreMotionListener.class, this);
		EVENTS.remove(PostMotionListener.class, this);

		switch(mode.getSelected())
		{
			case LEGIT:
			IKeyBinding sneakKey = (IKeyBinding)MC.options.sneakKey;
			sneakKey.resetPressedState();
			break;

			case PACKET:
			sendSneakPacket(Mode.RELEASE_SHIFT_KEY);
			break;
		}
	}

	@Override
	public void onPreMotion()
	{
		KeyBinding sneakKey = MC.options.sneakKey;

		switch(mode.getSelected())
		{
			case LEGIT:
			if(offWhileFlying.isChecked() && isFlying())
				((IKeyBinding)sneakKey).resetPressedState();
			else
				sneakKey.setPressed(true);
			break;

			case PACKET:
			((IKeyBinding)sneakKey).resetPressedState();
			sendSneakPacket(Mode.PRESS_SHIFT_KEY);
			sendSneakPacket(Mode.RELEASE_SHIFT_KEY);
			break;
		}
	}

	@Override
	public void onPostMotion()
	{
		if(mode.getSelected() != SneakMode.PACKET)
			return;

		sendSneakPacket(Mode.RELEASE_SHIFT_KEY);
		sendSneakPacket(Mode.PRESS_SHIFT_KEY);
	}

	private boolean isFlying()
	{
		if(MC.player.getAbilities().flying)
			return true;

		if(WURST.getHax().flightHack.isEnabled())
			return true;

		if(WURST.getHax().freecamHack.isEnabled())
			return true;

		return false;
	}

	private void sendSneakPacket(Mode mode)
	{
		ClientPlayerEntity player = MC.player;
		ClientCommandC2SPacket packet =
			new ClientCommandC2SPacket(player, mode);
		player.networkHandler.sendPacket(packet);
	}

	private enum SneakMode
	{
		PACKET("Packet"),
		LEGIT("Legit");

		private final String name;

		private SneakMode(String name)
		{
			this.name = name;
		}

		@Override
		public String toString()
		{
			return name;
		}
	}
}
