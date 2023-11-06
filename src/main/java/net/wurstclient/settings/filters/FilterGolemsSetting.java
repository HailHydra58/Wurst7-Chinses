/*
 * Copyright (c) 2014-2023 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.settings.filters;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.GolemEntity;

public final class FilterGolemsSetting extends EntityFilterCheckbox
{
	public FilterGolemsSetting(String description, boolean checked)
	{
		super("选择傀儡", description, checked);
	}

	@Override
	public boolean test(Entity e)
	{
		return !(e instanceof GolemEntity);
	}

	public static FilterGolemsSetting genericCombat(boolean checked)
	{
		return new FilterGolemsSetting(
			"不会攻击铁傀儡、雪傀儡和潜影贝。", checked);
	}
}
