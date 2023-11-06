/*
 * Copyright (c) 2014-2023 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.other_features;

import net.wurstclient.DontBlock;
import net.wurstclient.SearchTags;
import net.wurstclient.options.KeybindManagerScreen;
import net.wurstclient.other_feature.OtherFeature;

@SearchTags({"KeybindManager", "keybind manager", "KeybindsManager",
	"keybinds manager"})
@DontBlock
public final class KeybindManagerOtf extends OtherFeature
{
	public KeybindManagerOtf()
	{
		super("快捷键绑定",
			"这只是一个快捷方式，可以让您从GUI内部打开快捷键绑定。 通常进去是Esc > Options > Keybinds。");
	}

	@Override
	public String getPrimaryAction()
	{
		return "Open Keybind Manager";
	}

	@Override
	public void doPrimaryAction()
	{
		MC.setScreen(new KeybindManagerScreen(MC.currentScreen));
	}
}
