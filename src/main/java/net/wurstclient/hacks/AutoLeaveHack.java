/*
 * Copyright (c) 2014-2023 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.EnumSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;

@SearchTags({"auto leave", "AutoDisconnect", "auto disconnect", "AutoQuit",
        "auto quit"})
public final class AutoLeaveHack extends Hack implements UpdateListener {
    private final SliderSetting health = new SliderSetting("生命值",
            "当您的生命值达到此数值或低于它时离开服务器。",
            4, 0.5, 9.5, 0.5, ValueDisplay.DECIMAL.withSuffix(" hearts"));

    public final EnumSetting<Mode> mode = new EnumSetting<>("模式",
            "\u00a7l退出\u00a7r模式只是正常退出游戏。\n"
                    + "可以绕过NoCheat+，但不能绕过CombatLog。\n\n"
                    + "\u00a7l字符\u00a7r模式发送一个特殊的聊天消息，导致服务器将你踢出。\n"
                    + "可以绕过NoCheat+和一些版本的CombatLog。\n\n"
                    + "\u00a7lTP\u00a7r模式将你传送到一个无效的位置，导致服务器将你踢出。\n"
                    + "可以绕过CombatLog，但不能绕过NoCheat+。\n\n"
                    + "\u00a7l自我伤害\u00a7r模式发送攻击另一个玩家的数据包，但攻击者和目标都是你自己。这会导致服务器将你踢出。\n"
                    + "可以绕过CombatLog和NoCheat+。",
            Mode.values(), Mode.QUIT);

    public AutoLeaveHack() {
        super("AutoLeave", "血量过低退服");

        setCategory(Category.COMBAT);
        addSetting(health);
        addSetting(mode);
    }

    @Override
    public String getRenderName() {
        return getName() + " [" + mode.getSelected() + "]";
    }

    @Override
    public void onEnable() {
        EVENTS.add(UpdateListener.class, this);
    }

    @Override
    public void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
    }

    @Override
    public void onUpdate() {
        // check gamemode
        if (MC.player.getAbilities().creativeMode)
            return;

        // check for other players
        if (MC.isInSingleplayer()
                && MC.player.networkHandler.getPlayerList().size() == 1)
            return;

        // check health
        if (MC.player.getHealth() > health.getValueF() * 2F)
            return;

        // leave server
        switch (mode.getSelected()) {
            case QUIT:
                MC.world.disconnect();
                break;

            case CHARS:
                MC.getNetworkHandler().sendChatMessage("\u00a7");
                break;

            case TELEPORT:
                MC.player.networkHandler
                        .sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(3.1e7,
                                100, 3.1e7, false));
                break;

            case SELFHURT:
                MC.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket
                        .attack(MC.player, MC.player.isSneaking()));
                break;
        }

        // disable
        setEnabled(false);
    }

    public static enum Mode {
        QUIT("Quit"),

        CHARS("Chars"),

        TELEPORT("TP"),

        SELFHURT("SelfHurt");

        private final String name;

        private Mode(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
