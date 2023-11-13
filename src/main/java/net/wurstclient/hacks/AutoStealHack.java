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
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;

@SearchTags({"auto steal", "ChestStealer", "chest stealer",
        "steal store buttons", "Steal/Store buttons"})
public final class AutoStealHack extends Hack {
    private final SliderSetting delay = new SliderSetting("延迟",
            "移动物品堆栈之间的延迟。\n"
                    + "对于 NoCheat+ 服务器，应至少为 70ms。",
            100, 0, 500, 10, ValueDisplay.INTEGER.withSuffix("ms"));

    private final CheckboxSetting buttons =
            new CheckboxSetting("偷取/存储按钮", true);

    public AutoStealHack() {
        super("AutoSteal", "自动盗窃");
        setCategory(Category.ITEMS);
        addSetting(buttons);
        addSetting(delay);
    }

    public boolean areButtonsVisible() {
        return buttons.isChecked();
    }

    public long getDelay() {
        return delay.getValueI();
    }

    // See ContainerScreen54Mixin and ShulkerBoxScreenMixin
}
