/*
 * Copyright (c) 2014-2023 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.hack.Hack;

@SearchTags({"true sight"})
public final class TrueSightHack extends Hack
{
	public TrueSightHack()
	{
		super("TrueSight", "无视隐身");
		setCategory(Category.RENDER);
	}

	// See LivingEntityRendererMixin
}
