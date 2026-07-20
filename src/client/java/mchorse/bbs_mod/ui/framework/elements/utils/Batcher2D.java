package mchorse.bbs_mod.ui.framework.elements.utils;

import mchorse.bbs_mod.BBSModClient;
import mchorse.bbs_mod.client.ShaderProgram;
import mchorse.bbs_mod.graphics.texture.Texture;
import mchorse.bbs_mod.ui.framework.UIContext;
import mchorse.bbs_mod.ui.utils.Area;
import mchorse.bbs_mod.ui.utils.icons.Icon;
import mchorse.bbs_mod.utils.colors.Colors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.util.function.Supplier;

/**
 * Batcher2D - 2D rendering batcher for MC 26.2.
 * Uses Minecraft Font directly for text, GuiGraphicsExtractor.fill for boxes.
 */
public class Batcher2D
{
    private static FontRenderer fontRenderer = new FontRenderer();

    private GuiGraphicsExtractor context;
    private FontRenderer font;
    private int clipX, clipY, clipW, clipH;
    private boolean clipped;

    public static FontRenderer getDefaultTextRenderer()
    {
        fontRenderer.setRenderer(Minecraft.getInstance().font);
        return fontRenderer;
    }

    public Batcher2D(GuiGraphicsExtractor context)
    {
        this.context = context;
        this.font = getDefaultTextRenderer();
    }

    public GuiGraphicsExtractor getContext()
    {
        return this.context;
    }

    public FontRenderer getFont()
    {
        return this.font;
    }

    public void setFont(FontRenderer font)
    {
        this.font = font;
    }

    /* ======== Box drawing ======== */

    public void box(int x, int y, int w, int h, int color)
    {
        this.context.fill(x, y, x + w, y + h, color);
    }

    public void box(Area area, int color)
    {
        this.context.fill(area.x, area.y, area.x + area.w, area.y + area.h, color);
    }

    public void box(double x, double y, double w, double h, int color)
    {
        this.context.fill((int)x, (int)y, (int)(x + w), (int)(y + h), color);
    }

    public void box(float x1, float y1, float x2, float y2, int color)
    {
        this.context.fill((int)x1, (int)y1, (int)x2, (int)y2, color);
    }

    public void box(float x, float y, float w, float h, int color1, int color2, int color3, int color4)
    {
        this.context.fill((int)x, (int)y, (int)(x + w), (int)(y + h), color1);
    }

    public void fillRect(int x, int y, int w, int h, int color)
    {
        this.context.fill(x, y, x + w, y + h, color);
    }

    public void normalizedBox(float x1, float y1, float x2, float y2, int color)
    {
        int minX = (int)Math.min(x1, x2);
        int minY = (int)Math.min(y1, y2);
        int maxX = (int)Math.max(x1, x2);
        int maxY = (int)Math.max(y1, y2);
        this.context.fill(minX, minY, maxX, maxY, color);
    }

    public void gradientVBox(int x, int y, int w, int h, int color1, int color2)
    {
        this.context.fill(x, y, w, h, color1);
    }

    public void gradientVBox(float x, float y, float w, float h, int color1, int color2)
    {
        this.context.fill((int)x, (int)y, (int)w, (int)h, color1);
    }

    public void gradientHBox(int x, int y, int w, int h, int color1, int color2)
    {
        this.context.fill(x, y, w, h, color1);
    }

    public void gradientHBox(float x, float y, float w, float h, int color1, int color2)
    {
        this.context.fill((int)x, (int)y, (int)w, (int)h, color1);
    }

    public void gradientBox(float x, float y, float w, float h, int topLeft, int topRight, int bottomRight, int bottomLeft)
    {
        this.context.fill((int)x, (int)y, (int)(x + w), (int)(y + h), topLeft);
    }

    public void dropShadow(float x, float y, float w, float h, int offset)
    {
        this.context.fill((int)(x + offset), (int)(y + offset), (int)(x + w + offset), (int)(y + h + offset), 0x44000000);
    }

    public void dropShadow(int x1, int y1, int x2, int y2, int offset, int color1, int color2)
    {
        this.context.fill(x1 + offset, y1 + offset, x2 + offset, y2 + offset, color1);
    }

    public void dropCircleShadow()
    {
    }

    public void area(float x, float y, float w, float h, int color)
    {
        this.context.fill((int)x, (int)y, (int)(x + w), (int)(y + h), color);
    }

    public void outline(int x1, int y1, int x2, int y2, int color)
    {
        this.context.fill(x1, y1, x2, y1 + 1, color);
        this.context.fill(x1, y2 - 1, x2, y2, color);
        this.context.fill(x1, y1 + 1, x1 + 1, y2 - 1, color);
        this.context.fill(x2 - 1, y1 + 1, x2, y2 - 1, color);
    }

    public void outlineCenter(int x, int y, int w, int h, int color)
    {
        this.outline(x - w / 2, y - h / 2, x + w / 2, y + h / 2, color);
    }

    public void texturedBox(Supplier<ShaderProgram> shader, int texture, int color, float x, float y, float w, float h, float u1, float v1, float u2, float v2, int textureW, int textureH)
    {
        this.context.fill((int)x, (int)y, (int)(x + w), (int)(y + h), color);
    }

    public void texturedBox(int texture, int color, float x, float y, float w, float h, float u1, float v1, float u2, float v2, int textureW, int textureH)
    {
        this.context.fill((int)x, (int)y, (int)(x + w), (int)(y + h), color);
    }

    public void fullTexturedBox(Texture texture, int x, int y, int w, int h)
    {
        this.context.fill(x, y, x + w, y + h, Colors.WHITE);
    }

    public void fullTexturedBox(Texture texture, float x, float y, float w, float h)
    {
        this.context.fill((int)x, (int)y, (int)(x + w), (int)(y + h), Colors.WHITE);
    }

    /* ======== Text rendering (via Minecraft Font) ======== */

    public void text(String string, int x, int y)
    {
        drawText(string, x, y, Colors.WHITE, false);
    }

    public void text(String string, int x, int y, int color)
    {
        drawText(string, x, y, color, false);
    }

    public void text(String string, int x, int y, int color, boolean shadow)
    {
        drawText(string, x, y, color, shadow);
    }

    public void text(String string, float x, float y, int color, boolean shadow)
    {
        drawText(string, (int)x, (int)y, color, shadow);
    }

    public void textShadow(String string, int x, int y)
    {
        drawText(string, x, y, Colors.WHITE, true);
    }

    public void textShadow(String string, int x, int y, int color)
    {
        drawText(string, x, y, color, true);
    }

    public void textShadow(String string, float x, float y)
    {
        drawText(string, (int)x, (int)y, Colors.WHITE, true);
    }

    public void textCard(String string, int x, int y)
    {
        Font minecraftFont = Minecraft.getInstance().font;
        int w = minecraftFont.width(string);
        int h = minecraftFont.lineHeight;
        this.context.fill(x - 2, y - 2, x + w + 2, y + h + 2, 0xaa000000);
        drawText(string, x, y, Colors.WHITE, false);
    }

    public void textCard(String string, int x, int y, int color, int bgColor)
    {
        Font minecraftFont = Minecraft.getInstance().font;
        int w = minecraftFont.width(string);
        int h = minecraftFont.lineHeight;
        this.context.fill(x - 2, y - 2, x + w + 2, y + h + 2, bgColor);
        drawText(string, x, y, color, false);
    }

    public void textCard(String string, int x, int y, int color, int bgColor, int bgOffset)
    {
        Font minecraftFont = Minecraft.getInstance().font;
        int w = minecraftFont.width(string);
        int h = minecraftFont.lineHeight;
        this.context.fill(x - bgOffset, y - 2, x + w + bgOffset, y + h + 2, bgColor);
        drawText(string, x, y, color, false);
    }

    public void textCard(String string, int x, int y, int color, int bgColor, int bgOffset, boolean shadow)
    {
        Font minecraftFont = Minecraft.getInstance().font;
        int w = minecraftFont.width(string);
        int h = minecraftFont.lineHeight;
        this.context.fill(x - bgOffset, y - 2, x + w + bgOffset, y + h + 2, bgColor);
        drawText(string, x, y, color, shadow);
    }

    private void drawText(String str, int x, int y, int color, boolean shadow)
    {
        Font font = Minecraft.getInstance().font;
        int bgColor = shadow ? 0x44000000 : 0;
        this.context.textWithBackdrop(font, Component.literal(str), x + 1, y + 1, color, bgColor);
    }

    public void wallText(String string, int x, int y, int color)
    {
        drawText(string, x, y, color, false);
    }

    /* ======== Icon rendering ======== */

    public void icon(Icon icon, int x, int y)
    {
    }

    public void icon(Icon icon, int x, int y, float ax, float ay)
    {
    }

    public void icon(Icon icon, int color, int x, int y, float ax, float ay)
    {
    }

    public void icon(Icon icon, int color, int x, int y)
    {
    }

    public void iconArea(Icon icon, int x, int y, int w, int h)
    {
    }

    public void iconArea(Icon icon, int color, int x, int y, int w, int h)
    {
    }

    public void outlinedIcon(Icon icon, int x, int y, float ax, float ay)
    {
    }

    /* ======== Clipping ======== */

    public void clip(Area area, UIContext context)
    {
        if (context != null)
        {
            this.clipX = area.x;
            this.clipY = area.y;
            this.clipW = area.w;
            this.clipH = area.h;
            this.clipped = true;
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            GL11.glScissor(area.x, area.y, area.w, area.h);
        }
    }

    public void clip(int x, int y, int w, int h, int sw, int sh)
    {
        this.clipX = x;
        this.clipY = y;
        this.clipW = w;
        this.clipH = h;
        this.clipped = true;
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(x, y, w, h);
    }

    public void unclip(UIContext context)
    {
        if (this.clipped)
        {
            this.clipped = false;
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        }
    }

    public void unclip(int sw, int sh)
    {
        if (this.clipped)
        {
            this.clipped = false;
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        }
    }

    public void unclip()
    {
        if (this.clipped)
        {
            this.clipped = false;
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        }
    }

    public void flush()
    {
    }

    public void reset()
    {
        this.clipped = false;
    }
}
