/*
 * Copyright (c) 2014-2023 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.block.entity.*;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.ChestBoatEntity;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.entity.vehicle.HopperMinecartEntity;
import net.wurstclient.Category;
import net.wurstclient.events.CameraTransformViewBobbingListener;
import net.wurstclient.events.RenderListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.hacks.chestesp.ChestEspBlockGroup;
import net.wurstclient.hacks.chestesp.ChestEspEntityGroup;
import net.wurstclient.hacks.chestesp.ChestEspGroup;
import net.wurstclient.hacks.chestesp.ChestEspRenderer;
import net.wurstclient.hacks.chestesp.ChestEspStyle;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.ColorSetting;
import net.wurstclient.settings.EnumSetting;
import net.wurstclient.util.ChunkUtils;
import net.wurstclient.util.RenderUtils;

public class ChestEspHack extends Hack implements UpdateListener,
        CameraTransformViewBobbingListener, RenderListener {
    private final EnumSetting<ChestEspStyle> style =
            new EnumSetting<>("Style", ChestEspStyle.values(), ChestEspStyle.BOXES);

    private final ChestEspBlockGroup basicChests = new ChestEspBlockGroup(
            new ColorSetting("普通箱子",
                    "普通箱子将以此颜色突出显示。", Color.GREEN),
            null);

    private final ChestEspBlockGroup trapChests = new ChestEspBlockGroup(
            new ColorSetting("陷阱储物箱",
                    "陷阱箱子将以此颜色突出显示。",
                    new Color(0xFF8000)),
            new CheckboxSetting("Include trap chests", true));

    private final ChestEspBlockGroup enderChests = new ChestEspBlockGroup(
            new ColorSetting("末影箱",
                    "末影箱将以此颜色突出显示。", Color.CYAN),
            new CheckboxSetting("Include ender chests", true));

    private final ChestEspEntityGroup chestCarts =
            new ChestEspEntityGroup(
                    new ColorSetting("储物箱矿车",
                            "带有箱子的矿车将以此颜色突出显示。",
                            Color.YELLOW),
                    new CheckboxSetting("Include chest carts", true));

    private final ChestEspEntityGroup chestBoats =
            new ChestEspEntityGroup(
                    new ColorSetting("储物箱船",
                            "带有箱子的船只将以此颜色突出显示。",
                            Color.YELLOW),
                    new CheckboxSetting("Include chest boats", true));

    private final ChestEspBlockGroup barrels = new ChestEspBlockGroup(
            new ColorSetting("木桶",
                    "木桶将以此颜色突出显示。", Color.GREEN),
            new CheckboxSetting("Include barrels", true));

    private final ChestEspBlockGroup shulkerBoxes = new ChestEspBlockGroup(
            new ColorSetting("潜影盒",
                    "潜影盒将以此颜色突出显示。", Color.MAGENTA),
            new CheckboxSetting("Include shulkers", true));

    private final ChestEspBlockGroup hoppers = new ChestEspBlockGroup(
            new ColorSetting("漏斗",
                    "漏斗将以此颜色突出显示。", Color.WHITE),
            new CheckboxSetting("Include hoppers", false));

    private final ChestEspEntityGroup hopperCarts =
            new ChestEspEntityGroup(
                    new ColorSetting("漏斗矿车",
                            "带有漏斗的矿车将以此颜色突出显示。",
                            Color.YELLOW),
                    new CheckboxSetting("Include hopper carts", false));

    private final ChestEspBlockGroup droppers = new ChestEspBlockGroup(
            new ColorSetting("投掷器",
                    "投掷器将以此颜色突出显示。", Color.WHITE),
            new CheckboxSetting("Include droppers", false));

    private final ChestEspBlockGroup dispensers = new ChestEspBlockGroup(
            new ColorSetting("发射器",
                    "发射器将以此颜色突出显示。",
                    new Color(0xFF8000)),
            new CheckboxSetting("Include dispensers", false));

    private final ChestEspBlockGroup furnaces =
            new ChestEspBlockGroup(new ColorSetting("熔炉",
                    "熔炉、烟熏炉和高炉将以此颜色突出显示。",
                    Color.RED), new CheckboxSetting("Include furnaces", false));

    private final List<ChestEspGroup> groups = Arrays.asList(basicChests,
            trapChests, enderChests, chestCarts, chestBoats, barrels, shulkerBoxes,
            hoppers, hopperCarts, droppers, dispensers, furnaces);

    private final List<ChestEspEntityGroup> entityGroups =
            Arrays.asList(chestCarts, chestBoats, hopperCarts);

    public ChestEspHack() {
        super("ChestESP", "高亮箱子");
        setCategory(Category.RENDER);

        addSetting(style);
        groups.stream().flatMap(ChestEspGroup::getSettings)
                .forEach(this::addSetting);
    }

    @Override
    protected void onEnable() {
        EVENTS.add(UpdateListener.class, this);
        EVENTS.add(CameraTransformViewBobbingListener.class, this);
        EVENTS.add(RenderListener.class, this);

        ChestEspRenderer.prepareBuffers();
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
        EVENTS.remove(CameraTransformViewBobbingListener.class, this);
        EVENTS.remove(RenderListener.class, this);

        groups.forEach(ChestEspGroup::clear);
        ChestEspRenderer.closeBuffers();
    }

    @Override
    public void onUpdate() {
        groups.forEach(ChestEspGroup::clear);

        ArrayList<BlockEntity> blockEntities =
                ChunkUtils.getLoadedBlockEntities()
                        .collect(Collectors.toCollection(ArrayList::new));

        for (BlockEntity blockEntity : blockEntities)
            if (blockEntity instanceof TrappedChestBlockEntity)
                trapChests.add(blockEntity);
            else if (blockEntity instanceof ChestBlockEntity)
                basicChests.add(blockEntity);
            else if (blockEntity instanceof EnderChestBlockEntity)
                enderChests.add(blockEntity);
            else if (blockEntity instanceof ShulkerBoxBlockEntity)
                shulkerBoxes.add(blockEntity);
            else if (blockEntity instanceof BarrelBlockEntity)
                barrels.add(blockEntity);
            else if (blockEntity instanceof HopperBlockEntity)
                hoppers.add(blockEntity);
            else if (blockEntity instanceof DropperBlockEntity)
                droppers.add(blockEntity);
            else if (blockEntity instanceof DispenserBlockEntity)
                dispensers.add(blockEntity);
            else if (blockEntity instanceof AbstractFurnaceBlockEntity)
                furnaces.add(blockEntity);

        for (Entity entity : MC.world.getEntities())
            if (entity instanceof ChestMinecartEntity)
                chestCarts.add(entity);
            else if (entity instanceof HopperMinecartEntity)
                hopperCarts.add(entity);
            else if (entity instanceof ChestBoatEntity)
                chestBoats.add(entity);
    }

    @Override
    public void onCameraTransformViewBobbing(
            CameraTransformViewBobbingEvent event) {
        if (style.getSelected().hasLines())
            event.cancel();
    }

    @Override
    public void onRender(MatrixStack matrixStack, float partialTicks) {
        // GL settings
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        matrixStack.push();
        RenderUtils.applyRegionalRenderOffset(matrixStack);

        entityGroups.stream().filter(ChestEspGroup::isEnabled)
                .forEach(g -> g.updateBoxes(partialTicks));

        ChestEspRenderer espRenderer = new ChestEspRenderer(matrixStack);

        if (style.getSelected().hasBoxes()) {
            RenderSystem.setShader(GameRenderer::getPositionProgram);
            groups.stream().filter(ChestEspGroup::isEnabled)
                    .forEach(espRenderer::renderBoxes);
        }

        if (style.getSelected().hasLines()) {
            RenderSystem.setShader(GameRenderer::getPositionProgram);
            groups.stream().filter(ChestEspGroup::isEnabled)
                    .forEach(espRenderer::renderLines);
        }

        matrixStack.pop();

        // GL resets
        RenderSystem.setShaderColor(1, 1, 1, 1);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_BLEND);
    }
}
