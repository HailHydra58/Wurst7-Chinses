/*
 * Copyright (c) 2014-2023 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.other_features;

import java.awt.Color;
import java.util.Comparator;

import net.wurstclient.DontBlock;
import net.wurstclient.SearchTags;
import net.wurstclient.WurstClient;
import net.wurstclient.hack.Hack;
import net.wurstclient.other_feature.OtherFeature;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.ColorSetting;
import net.wurstclient.settings.EnumSetting;

@SearchTags({"ArrayList", "ModList", "CheatList", "mod list", "array list",
	"hack list", "cheat list"})
@DontBlock
public final class HackListOtf extends OtherFeature
{
	private final EnumSetting<Mode> mode = new EnumSetting<>("模式",
		"\u00a7l列表\u00a7r 将整个列表拟合到屏幕上。\n"
			+ "\u00a7l计数\u00a7r 仅呈现激活外挂的数量。\n"
			+ "\u00a7l隐藏\u00a7r 不显示。",
		Mode.values(), Mode.AUTO);

	private final EnumSetting<Position> position = new EnumSetting<>("定位",
		"屏幕的哪一侧应显示黑色列表。",
		Position.values(), Position.LEFT);

	private final ColorSetting color = new ColorSetting("颜色",
		"列表的字体的颜色。",
		Color.WHITE);

	private final EnumSetting<SortBy> sortBy = new EnumSetting<>("排序方式",
		"对外挂列表进行排序。",
		SortBy.values(), SortBy.NAME);

	private final CheckboxSetting revSort =
		new CheckboxSetting("反向排序", false);

	private final CheckboxSetting animations = new CheckboxSetting("动画",
		"启用后，当启用和禁用外挂时，条目滑入和退出列表。",
		true);

	private SortBy prevSortBy;
	private Boolean prevRevSort;

	public HackListOtf()
	{
		super("外挂列表", "在屏幕上显示激活的外挂的列表。");

		addSetting(mode);
		addSetting(position);
		addSetting(color);
		addSetting(sortBy);
		addSetting(revSort);
		addSetting(animations);
	}

	public Mode getMode()
	{
		return mode.getSelected();
	}

	public Position getPosition()
	{
		return position.getSelected();
	}

	public boolean isAnimations()
	{
		return animations.isChecked();
	}

	public Comparator<Hack> getComparator()
	{
		if(revSort.isChecked())
			return sortBy.getSelected().comparator.reversed();

		return sortBy.getSelected().comparator;
	}

	public boolean shouldSort()
	{
		try
		{
			// width of a renderName could change at any time
			// must sort the HackList every tick
			if(sortBy.getSelected() == SortBy.WIDTH)
				return true;

			if(sortBy.getSelected() != prevSortBy)
				return true;

			if(!Boolean.valueOf(revSort.isChecked()).equals(prevRevSort))
				return true;

			return false;

		}finally
		{
			prevSortBy = sortBy.getSelected();
			prevRevSort = revSort.isChecked();
		}
	}

	public int getColor()
	{
		return color.getColorI() & 0x00FFFFFF;
	}

	public static enum Mode
	{
		AUTO("列表"),

		COUNT("计数"),

		HIDDEN("隐藏");

		private final String name;

		private Mode(String name)
		{
			this.name = name;
		}

		@Override
		public String toString()
		{
			return name;
		}
	}

	public static enum Position
	{
		LEFT("Left"),

		RIGHT("Right");

		private final String name;

		private Position(String name)
		{
			this.name = name;
		}

		@Override
		public String toString()
		{
			return name;
		}
	}

	public static enum SortBy
	{
		NAME("Name", (a, b) -> a.getName().compareToIgnoreCase(b.getName())),

		WIDTH("Width", Comparator.comparingInt(
			h -> WurstClient.MC.textRenderer.getWidth(h.getRenderName())));

		private final String name;
		private final Comparator<Hack> comparator;

		private SortBy(String name, Comparator<Hack> comparator)
		{
			this.name = name;
			this.comparator = comparator;
		}

		@Override
		public String toString()
		{
			return name;
		}
	}
}
