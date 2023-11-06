/*
 * Copyright (c) 2014-2023 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.sound.SoundEvents;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.PacketInputListener;
import net.wurstclient.events.RenderListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.hacks.autofish.AutoFishDebugDraw;
import net.wurstclient.hacks.autofish.AutoFishRodSelector;
import net.wurstclient.hacks.autofish.ShallowWaterWarningCheckbox;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;
import net.wurstclient.util.ChatUtils;

@SearchTags({"AutoFishing", "auto fishing", "AutoFisher", "auto fisher",
	"AFKFishBot", "afk fish bot", "AFKFishingBot", "afk fishing bot",
	"AFKFisherBot", "afk fisher bot"})
public final class AutoFishHack extends Hack
	implements UpdateListener, PacketInputListener, RenderListener
{
	private final SliderSetting validRange = new SliderSetting("有效范围",
		"在此范围之外发生的任何叮咬都将被忽略。\n\n"
			+ "如果未检测到叮咬，请增加范围，如果发现其他人的叮咬为您的叮咬，请减少它。",
		1.5, 0.25, 8, 0.25, ValueDisplay.DECIMAL);

	private final SliderSetting catchDelay = new SliderSetting("提杆延迟",
		"在咬钩后等待多久再提杆。", 0, 0, 60,
		1, ValueDisplay.INTEGER.withSuffix(" ticks"));

	private final SliderSetting retryDelay = new SliderSetting("重试延迟",
		"如果投放或卷线钓竿失败，AutoFish 将等待多长时间再次尝试。",
		15, 0, 100, 1, ValueDisplay.INTEGER.withSuffix(" ticks"));

	private final SliderSetting patience = new SliderSetting("耐心",
		"如果 AutoFish 在没有咬钩的情况下等待多长时间再卷线。",
		60, 10, 120, 1, ValueDisplay.INTEGER.withSuffix("s"));

	private final CheckboxSetting stopWhenInvFull = new CheckboxSetting(
		"当背包已满时停止",
		"如果启用，当您的物品栏已满时，AutoFish 将自动关闭。",
		false);

	private final ShallowWaterWarningCheckbox shallowWaterWarning =
		new ShallowWaterWarningCheckbox();

	private final AutoFishDebugDraw debugDraw =
		new AutoFishDebugDraw(validRange);
	private final AutoFishRodSelector rodSelector =
		new AutoFishRodSelector(this);

	private int castRodTimer;
	private int reelInTimer;

	public AutoFishHack()
	{
		super("AutoFish", "自动钓鱼");
		setCategory(Category.OTHER);

		addSetting(validRange);
		addSetting(catchDelay);
		addSetting(retryDelay);
		addSetting(patience);
		debugDraw.getSettings().forEach(this::addSetting);
		rodSelector.getSettings().forEach(this::addSetting);
		addSetting(stopWhenInvFull);
		addSetting(shallowWaterWarning);
	}

	@Override
	public String getRenderName()
	{
		if(!rodSelector.hasARod())
			return getName() + " [out of rods]";

		return getName();
	}

	@Override
	public void onEnable()
	{
		WURST.getHax().airPlaceHack.setEnabled(false);

		castRodTimer = 0;
		reelInTimer = 0;
		rodSelector.reset();
		debugDraw.reset();
		shallowWaterWarning.reset();

		EVENTS.add(UpdateListener.class, this);
		EVENTS.add(PacketInputListener.class, this);
		EVENTS.add(RenderListener.class, this);
	}

	@Override
	public void onDisable()
	{
		EVENTS.remove(UpdateListener.class, this);
		EVENTS.remove(PacketInputListener.class, this);
		EVENTS.remove(RenderListener.class, this);
	}

	@Override
	public void onUpdate()
	{
		// update timers
		if(castRodTimer > 0)
			castRodTimer--;
		if(reelInTimer > 0)
			reelInTimer--;

		// check if inventory is full
		if(stopWhenInvFull.isChecked()
			&& MC.player.getInventory().getEmptySlot() == -1)
		{
			ChatUtils.message(
				"Autofish停止了，因为您的库存已满。");
			setEnabled(false);
			return;
		}

		// select fishing rod
		if(!rodSelector.isBestRodAlreadySelected())
		{
			rodSelector.selectBestRod();
			return;
		}

		// if not fishing, cast rod
		if(!isFishing())
		{
			if(castRodTimer > 0)
				return;

			IMC.rightClick();
			castRodTimer = retryDelay.getValueI();
			reelInTimer = 20 * patience.getValueI();
			return;
		}

		// otherwise, reel in when it's time
		if(reelInTimer == 0)
		{
			IMC.rightClick();
			reelInTimer = retryDelay.getValueI();
			castRodTimer = retryDelay.getValueI();
		}
	}

	@Override
	public void onReceivedPacket(PacketInputEvent event)
	{
		// check packet type
		if(!(event.getPacket() instanceof PlaySoundS2CPacket sound))
			return;

		// check sound type
		if(!SoundEvents.ENTITY_FISHING_BOBBER_SPLASH
			.equals(sound.getSound().value()))
			return;

		// check if player is fishing
		if(!isFishing())
			return;

		// check if player is holding a fishing rod
		ClientPlayerEntity player = MC.player;
		if(!player.getMainHandStack().isOf(Items.FISHING_ROD))
			return;

		debugDraw.updateSoundPos(sound);

		// check sound position
		FishingBobberEntity bobber = player.fishHook;
		if(Math.abs(sound.getX() - bobber.getX()) > validRange.getValue()
			|| Math.abs(sound.getZ() - bobber.getZ()) > validRange.getValue())
			return;

		shallowWaterWarning.checkWaterAround(bobber);

		// catch fish
		reelInTimer = catchDelay.getValueI();
	}

	@Override
	public void onRender(MatrixStack matrixStack, float partialTicks)
	{
		debugDraw.render(matrixStack, partialTicks);
	}

	private boolean isFishing()
	{
		ClientPlayerEntity player = MC.player;
		return player != null && player.fishHook != null
			&& !player.fishHook.isRemoved();
	}
}
