/*
 * Copyright (c) 2014-2023 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.other_features;

import java.awt.Color;
import java.util.function.BooleanSupplier;

import net.wurstclient.DontBlock;
import net.wurstclient.SearchTags;
import net.wurstclient.other_feature.OtherFeature;
import net.wurstclient.settings.ColorSetting;
import net.wurstclient.settings.EnumSetting;

@SearchTags({"wurst logo", "top left corner"})
@DontBlock
public final class WurstLogoOtf extends OtherFeature
{
	private final ColorSetting bgColor = new ColorSetting("背景",
		"背景颜色。",
		Color.WHITE);

	private final ColorSetting txtColor =
		new ColorSetting("字体", "字体颜色。", Color.BLACK);

	private final EnumSetting<Visibility> visibility =
		new EnumSetting<>("可见性", Visibility.values(), Visibility.ALWAYS);

	public WurstLogoOtf()
	{
		super("Logo", "在屏幕上显示 Wurst logo和版本。");
		addSetting(bgColor);
		addSetting(txtColor);
		addSetting(visibility);
	}

	public boolean isVisible()
	{
		return visibility.getSelected().isVisible();
	}

	public float[] getBackgroundColor()
	{
		return bgColor.getColorF();
	}

	public int getTextColor()
	{
		return txtColor.getColorI();
	}

	public static enum Visibility
	{
		ALWAYS("显示", () -> true),

		ONLY_OUTDATED("隐藏",
			() -> WURST.getUpdater().isOutdated());

		private final String name;
		private final BooleanSupplier visible;

		private Visibility(String name, BooleanSupplier visible)
		{
			this.name = name;
			this.visible = visible;
		}

		public boolean isVisible()
		{
			return visible.getAsBoolean();
		}

		@Override
		public String toString()
		{
			return name;
		}
	}
}
