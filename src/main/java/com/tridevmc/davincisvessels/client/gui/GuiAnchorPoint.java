package com.tridevmc.davincisvessels.client.gui;

import com.tridevmc.davincisvessels.DavincisVesselsMod;
import com.tridevmc.davincisvessels.common.LanguageEntries;
import com.tridevmc.davincisvessels.common.network.message.AnchorPointMessage;
import com.tridevmc.davincisvessels.common.tileentity.BlockLocation;
import com.tridevmc.davincisvessels.common.tileentity.TileAnchorPoint;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.Map;
import java.util.UUID;


public class GuiAnchorPoint extends GuiContainer {

    public static final ResourceLocation GUI_TEXTURES = new ResourceLocation("davincisvessels", "textures/gui/anchorPoint.png");
    public TileAnchorPoint anchorPoint;
    private int selectedRelation;
    private String[] relations;
    private GuiButtonHooked btnLink, btnSwitch, btnNextRelation, btnPrevRelation;

    public GuiAnchorPoint(TileAnchorPoint te, EntityPlayer entityplayer) {
        super(new ContainerAnchorPoint(te, entityplayer));
        this.anchorPoint = te;

        xSize = 256;
        ySize = 220;
    }

    @Override
    public void initGui() {
        super.initGui();

        buttons.clear();
        FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
        int linkWidth = fontRenderer.getStringWidth(I18n.format(LanguageEntries.GUI_ANCHOR_LINK)) + 6;
        int switchWidth = fontRenderer.getStringWidth(I18n.format(LanguageEntries.GUI_ANCHOR_SWITCH)) + 6;
        int width = linkWidth > switchWidth ? linkWidth : switchWidth;

        int linkX = guiLeft + 83;
        int linkY = guiTop + 98;

        btnLink = new GuiButtonHooked(1, linkX, linkY,
                width, 20, I18n.format(LanguageEntries.GUI_ANCHOR_LINK));
        btnLink.addHook(((mX, mY) -> new AnchorPointMessage(anchorPoint, TileAnchorPoint.AnchorPointAction.LINK).sendToServer()));
        btnLink.enabled = anchorPoint.content != null;

        int switchX = guiLeft + 86 + width;
        int switchY = guiTop + 98;

        btnSwitch = new GuiButtonHooked(2, switchX, switchY,
                width, 20, I18n.format(LanguageEntries.GUI_ANCHOR_SWITCH));
        btnSwitch.addHook(((mX, mY) -> new AnchorPointMessage(anchorPoint, TileAnchorPoint.AnchorPointAction.LINK).sendToServer()));

        btnPrevRelation = new LongNarrowButton(3, guiLeft + 70, guiTop + 73, true);
        btnPrevRelation.addHook((mX, mY) -> {
            if (selectedRelation > relations.length - 1) {
                selectedRelation--;
            } else if (selectedRelation == 0) {
                selectedRelation = relations.length - 1;
            }
        });
        btnNextRelation = new LongNarrowButton(4, guiLeft + 70, guiTop + 50, false);
        btnNextRelation.addHook((mX, mY) -> {
            if (selectedRelation < relations.length - 1) {
                selectedRelation++;
            } else if (selectedRelation == relations.length - 1) {
                selectedRelation = 0;
            }
        });

        addButton(btnLink);
        addButton(btnSwitch);
        addButton(btnPrevRelation);
        addButton(btnNextRelation);

        relations = new String[anchorPoint.getInstance().getRelatedAnchors().size()];

        int index = 0;
        for (Map.Entry<UUID, BlockLocation> e : anchorPoint.getInstance().getRelatedAnchors().entrySet()) {
            relations[index] = I18n.format(LanguageEntries.GUI_ANCHOR_RELATED, e.getValue().getPos().toString().substring(9)
                    .replace("}", "").replaceAll("=", ":"));
            index++;
        }

        if (relations.length == 0) {
            relations = new String[]{I18n.format(LanguageEntries.GUI_ANCHOR_NORELATIONS)};
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        GlStateManager.pushMatrix();
        GlStateManager.rotated(3, 0, 1, 0);
        GlStateManager.scaled(2.75, 2.75, 2.75);
        GlStateManager.translated(-0.5, 9, 0);
        GlStateManager.enableRescaleNormal();
        RenderHelper.enableGUIStandardItemLighting();
        itemRender.renderItemIntoGUI(new ItemStack(DavincisVesselsMod.CONTENT.blockAnchorPoint, 1), 0, 0);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();

        fontRenderer.drawString(I18n.format(LanguageEntries.GUI_ANCHOR_POS, anchorPoint.getPos()
                .toString().substring(9).replace("}", "").replaceAll("=", ":")), 78, 30 - 10, 0);
        fontRenderer.drawString(I18n.format(LanguageEntries.GUI_ANCHOR_TYPE, anchorPoint.getInstance().getType()
                .toString()), 78, 45 - 10, 0);
        fontRenderer.drawString(relations[selectedRelation],
                156 - (fontRenderer.getStringWidth(relations[selectedRelation]) / 2), 64, 0);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
        GL11.glColor4f(1F, 1F, 1F, 1F);
        mc.textureManager.bindTexture(GUI_TEXTURES);
        int x = (width - xSize) / 2;
        int y = (height - ySize) / 2;
        drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int state) {
        btnLink.enabled = anchorPoint.content != null;
        return super.mouseReleased(mouseX, mouseY, state);
    }

    public class LongNarrowButton extends GuiButtonHooked {

        private final boolean down;

        public LongNarrowButton(int buttonId, int x, int y, boolean down) {
            super(buttonId, x, y, "");

            this.down = down;
            width = 175;
            height = 12;
        }

        @Override
        public void render(int mouseX, int mouseY, float partialTicks) {
            if (this.visible) {
                mc.getTextureManager().bindTexture(GUI_TEXTURES);
                GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                boolean mouseOver = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
                int yOffset = 220 + (enabled ? 12 : 0);
                int xOffset = 0;

                if (enabled) {
                    if (mouseOver) {
                        yOffset += 12;
                    }
                }

                this.drawTexturedModalRect(this.x, this.y, xOffset, yOffset, this.width, this.height);
                int arrowX = 175 + (down ? 32 : 0);
                if (mouseOver) {
                    this.drawTexturedModalRect(this.x + (width / 2) - 8, this.y - 1, arrowX + 16, 220, 16, 12);
                } else {
                    this.drawTexturedModalRect(this.x + (width / 2) - 8, this.y - 1, arrowX, 220, 16, 12);
                }
            }
        }
    }

}
