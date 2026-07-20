package mchorse.bbs_mod.client;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

/**
 * Framebuffer - wraps MC 26.2 RenderTarget to provide the old Framebuffer API.
 * Used by BBSRendering for custom framebuffer management.
 */
public class Framebuffer
{
    public int textureWidth;
    public int textureHeight;

    private RenderTarget target;

    public Framebuffer(int width, int height)
    {
        this.textureWidth = width;
        this.textureHeight = height;
        this.target = new RenderTarget(
            Minecraft.getInstance().getDevice(),
            width, height, false, GpuFormat.RGBA8_UNORM
        );
    }

    public Framebuffer(RenderTarget target)
    {
        this.target = target;
        this.textureWidth = target.width;
        this.textureHeight = target.height;
    }

    public void beginWrite(boolean setViewport)
    {
        if (setViewport)
        {
            GL11.glViewport(0, 0, this.textureWidth, this.textureHeight);
        }
    }

    public void draw(int width, int height)
    {
        if (this.target != null)
        {
            this.target.draw();
        }
    }

    public void resize(int width, int height, boolean mac)
    {
        this.textureWidth = width;
        this.textureHeight = height;
        if (this.target != null)
        {
            this.target.resize(width, height);
        }
    }

    public static Framebuffer window()
    {
        return new Framebuffer(Minecraft.getInstance().mainRenderTarget);
    }
}
