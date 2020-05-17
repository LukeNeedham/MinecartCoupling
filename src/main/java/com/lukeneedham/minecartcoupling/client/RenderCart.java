package com.lukeneedham.minecartcoupling.client;

import com.lukeneedham.minecartcoupling.common.carts.coupling.CouplingsDao;
import com.lukeneedham.minecartcoupling.common.carts.coupling.CouplingsInProgressDao;
import com.lukeneedham.minecartcoupling.common.carts.coupling.ICouplingsDao;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderMinecart;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;

import javax.annotation.Nonnull;

public class RenderCart extends RenderMinecart<EntityMinecart> {

    static final float stringRed = 0.8F;
    static final float stringGreen = 0.8F;
    static final float stringBlue = 0.8F;

    public RenderCart(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntityMinecart minecart, double x, double y, double z, float yaw, float partialTicks) {
        super.doRender(minecart, x, y, z, yaw, partialTicks);

        drawExistingCouplings(minecart, x, y, z, yaw, partialTicks);
        drawCouplingInProgress(minecart, x, y, z, partialTicks);
    }

    private void drawCouplingBetweenMinecarts(EntityMinecart thisMinecart, EntityMinecart otherMinecart, double x, double y, double z, float yaw, float partialTicks) {
        // TODO: Draw this line :-)
    }

    private void drawExistingCouplings(EntityMinecart minecart, double x, double y, double z, float yaw, float partialTicks) {
        ICouplingsDao lm = CouplingsDao.CLIENT_INSTANCE;

        EntityMinecart coupledCartA = lm.getCoupledCartA(minecart);
        if (coupledCartA != null) {
            drawCouplingBetweenMinecarts(minecart, coupledCartA, x, y, z, yaw, partialTicks);
        }
        EntityMinecart coupledCartB = lm.getCoupledCartB(minecart);
        if (coupledCartB != null) {
            drawCouplingBetweenMinecarts(minecart, coupledCartB, x, y, z, yaw, partialTicks);
        }
    }

    /*
     * Adapted from RenderLiving.renderLeash
     */
    private void drawCouplingInProgress(@Nonnull EntityMinecart minecart, double x, double y, double z, float partialTicks) {
        Integer playerId = CouplingsInProgressDao.CLIENT_INSTANCE.getPlayerCouplingMinecart(minecart.getEntityId());
        if (playerId == null) {
            return;
        }
        EntityPlayer player = (EntityPlayer) minecart.world.getEntityByID(playerId);
        if (player == null) {
            return;
        }

        y = y - 1.3;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        double d0 = this.interpolateValue(player.prevRotationYaw, player.rotationYaw, partialTicks * 0.5F) * 0.01745329238474369D;
        double d1 = this.interpolateValue(player.prevRotationPitch, player.rotationPitch, partialTicks * 0.5F) * 0.01745329238474369D;
        double d2 = Math.cos(d0);
        double d3 = Math.sin(d0);

        double d5 = Math.cos(d1);
        double d6 = this.interpolateValue(player.prevPosX, player.posX, partialTicks) - d2 * 0.7D - d3 * 0.5D * d5;
        double d7 = this.interpolateValue(player.prevPosY + (double) player.getEyeHeight(), player.posY + (double) player.getEyeHeight(), partialTicks);
        double d8 = this.interpolateValue(player.prevPosZ, player.posZ, partialTicks) - d3 * 0.7D + d2 * 0.5D * d5;
        double d9 = 0.01745329238474369D + (Math.PI / 2D);
        d2 = Math.cos(d9) * (double) minecart.width * 0.4;
        d3 = Math.sin(d9) * (double) minecart.width * 0.4;
        double d10 = minecart.prevPosX + (minecart.posX - minecart.prevPosX) + d2;
        double d11 = minecart.prevPosY + (minecart.posY - minecart.prevPosY);
        double d12 = minecart.prevPosZ + (minecart.posZ - minecart.prevPosZ) + d3;
        double d13 = (double) ((float) (d6 - d10));
        double d14 = (double) ((float) (d7 - d11));
        double d15 = (double) ((float) (d8 - d12));
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        bufferbuilder.begin(5, DefaultVertexFormats.POSITION_COLOR);

        for (int j = 0; j <= 24; j = j + 24) {
            float f3 = (float) j / 24.0F;
            bufferbuilder.pos(x + d13 * (double) f3, y + d14 * (double) (f3 * f3 + f3) * 0.5D + (double) ((24.0F - (float) j) / 18.0F + 0.125F), z + d15 * (double) f3).color(stringRed, stringGreen, stringBlue, 1.0F).endVertex();
            bufferbuilder.pos(x + d13 * (double) f3 + 0.025D, y + d14 * (double) (f3 * f3 + f3) * 0.5D + (double) ((24.0F - (float) j) / 18.0F + 0.125F) + 0.025D, z + d15 * (double) f3).color(stringRed, stringGreen, stringBlue, 1.0F).endVertex();
        }

        tessellator.draw();
        bufferbuilder.begin(5, DefaultVertexFormats.POSITION_COLOR);

        for (int k = 0; k <= 24; k = k + 24) {
            float f7 = (float) k / 24.0F;
            bufferbuilder.pos(x + d13 * (double) f7 + 0.0D, y + d14 * (double) (f7 * f7 + f7) * 0.5D + (double) ((24.0F - (float) k) / 18.0F + 0.125F) + 0.025D, z + d15 * (double) f7).color(stringRed, stringGreen, stringBlue, 1.0F).endVertex();
            bufferbuilder.pos(x + d13 * (double) f7 + 0.025D, y + d14 * (double) (f7 * f7 + f7) * 0.5D + (double) ((24.0F - (float) k) / 18.0F + 0.125F), z + d15 * (double) f7 + 0.025D).color(stringRed, stringGreen, stringBlue, 1.0F).endVertex();
        }

        tessellator.draw();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.enableCull();
    }

    /**
     * Gets the value between start and end according to pct
     */
    private double interpolateValue(double start, double end, double pct) {
        return start + (end - start) * pct;
    }
}