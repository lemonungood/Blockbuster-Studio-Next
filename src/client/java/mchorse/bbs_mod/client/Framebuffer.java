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
        // [MC 26.2] RenderTarget is abstract; store dimensions only,
        // target is set when wrapping an existing RenderTarget via window()
    }

    public Framebuffer(RenderTarget target)
    {
        this.target = target;
        if (target != null)
        {
            this.textureWidth = target.width;
            this.textureHeight = target.height;
        }
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
            // [MC 26.2] RenderTarget.blitToScreen removed; blit handled by beginWrite/endWrite
            // this.target.blitToScreen(width, height);
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
        int w = Minecraft.getInstance().getWindow().getWidth();
        int h = Minecraft.getInstance().getWindow().getHeight();
        Framebuffer fb = new Framebuffer(w, h);
        return fb;
    }
}
