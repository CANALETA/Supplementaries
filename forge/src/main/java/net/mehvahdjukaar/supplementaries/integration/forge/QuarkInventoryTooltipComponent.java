package net.mehvahdjukaar.supplementaries.integration.forge;


import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SafeBlockTile;
import net.mehvahdjukaar.supplementaries.common.inventories.SackContainerMenu;
import net.mehvahdjukaar.supplementaries.common.items.tooltip_components.InventoryTooltip;
import net.mehvahdjukaar.supplementaries.common.utils.ItemsUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import vazkii.quark.content.client.module.ChestSearchingModule;
import vazkii.quark.content.client.tooltip.ShulkerBoxTooltips;

public class QuarkInventoryTooltipComponent implements ClientTooltipComponent {

    private static final int CORNER = 5;
    private static final int BUFFER = 1;
    private static final int EDGE = 18;

    private final CompoundTag tag;
    private final Item item;
    private final int[] dimensions;
    private final int size;
    private final boolean locked;

    public QuarkInventoryTooltipComponent(InventoryTooltip tooltip) {
        this.tag = tooltip.tag();
        this.item = tooltip.item();
        this.dimensions = SackContainerMenu.getRatio(tooltip.size());
        this.size = tooltip.size();
        BlockEntity te = ItemsUtil.loadBlockEntityFromItem(this.tag, this.item);
        if (te instanceof SafeBlockTile safe) {
            this.locked = !safe.canPlayerOpen(Minecraft.getInstance().player, false);
        } else {
            this.locked = false;
        }
    }

    @Override
    public void renderImage(Font font, int tooltipX, int tooltipY, GuiGraphics graphics) {
        if (locked) return;
        BlockEntity te = ItemsUtil.loadBlockEntityFromItem(this.tag, this.item);
        if (te != null) {
            if (te instanceof SafeBlockTile safe && !safe.canPlayerOpen(Minecraft.getInstance().player, false)) return;

            LazyOptional<IItemHandler> handler = te.getCapability(ForgeCapabilities.ITEM_HANDLER, null);
            handler.ifPresent((capability) -> {

                Minecraft mc = Minecraft.getInstance();

                int currentX = tooltipX;

                int texWidth = CORNER * 2 + EDGE * dimensions[0];

                int right = currentX + texWidth;
                Window window = mc.getWindow();
                if (right > window.getGuiScaledWidth()) {
                    currentX -= (right - window.getGuiScaledWidth());
                }

                PoseStack pose = graphics.pose();
                pose.pushPose();
                pose.translate(0.0D, 0.0D, 700.0D);

                int color = -1;

                ShulkerBoxTooltips.ShulkerComponent.renderTooltipBackground(mc, pose, currentX, tooltipY, dimensions[0], dimensions[1], color);

                for (int i = 0; i < size; i++) {
                    ItemStack itemstack = capability.getStackInSlot(i);
                    //fix 9->dimensions[0]
                    int xp = currentX + 6 + (i % dimensions[0]) * EDGE;
                    int yp = tooltipY + 6 + (i / dimensions[0]) * EDGE;

                    if (!itemstack.isEmpty()) {
                        graphics.renderFakeItem(itemstack, xp, yp);
                        graphics.renderItemDecorations(mc.font, itemstack, xp, yp);
                    }

                    if (!ChestSearchingModule.namesMatch(itemstack)) {
                        RenderSystem.disableDepthTest();
                        graphics.fill(xp, yp, xp + 16, yp + 16, 0xAA000000);
                    }
                }

                pose.popPose();
            });
        }
    }

    @Override
    public int getHeight() {
        return locked ? 0 : CORNER * 2 + EDGE * dimensions[1] + BUFFER;
    }

    @Override
    public int getWidth(Font font) {
        return locked ? 0 : CORNER * 2 + EDGE * dimensions[0];
    }
}
