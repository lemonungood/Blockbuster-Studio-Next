package mchorse.bbs_mod.utils.iris;

import mchorse.bbs_mod.graphics.texture.Texture;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;

/**
 * IrisTextureWrapper - bridges a BBS texture to Iris.
 *
 * BBS manages its own OpenGL textures on the 26.2 GL backend
 * (Texture.id is a real GL texture name, see Texture.bind() ->
 * GL11.glBindTexture), so the wrapper can hand Iris the actual GL id
 * of the texture being sampled. Iris 1.11.1 exposes no public API to
 * inject mod-owned PBR maps, so this wrapper is the supported seam: it
 * reports the real GL id so that, when a BBS texture is also present in
 * Iris's PBR atlas (resource-pack driven _n/_s siblings), Iris samples
 * the correct texture instead of binding a bogus handle.
 */
public class IrisTextureWrapper extends AbstractTexture
{
    public final Texture texture;
    public final int index;

    public IrisTextureWrapper(Texture texture, int index)
    {
        this.texture = texture;
        this.index = index;
    }

    public void load(ResourceManager manager) throws IOException
    {}

    /**
     * Returns the real OpenGL texture id BBS uses for this texture.
     * BBS owns the GL texture lifecycle, so this id is valid for sampling
     * on the 26.2 GL backend. Returns -1 only when the texture is invalid
     * (never uploaded / already deleted).
     */
    public int getGlId()
    {
        if (this.texture == null || !this.texture.isValid())
        {
            return -1;
        }

        return this.texture.id;
    }

    @Override
    public void close()
    {
        /* BBS owns the texture lifecycle (TextureManager.delete), so do not
           delete it from here. */
    }
}
