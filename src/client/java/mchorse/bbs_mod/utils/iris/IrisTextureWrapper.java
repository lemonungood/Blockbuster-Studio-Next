package mchorse.bbs_mod.utils.iris;

import mchorse.bbs_mod.BBSModClient;
import mchorse.bbs_mod.resources.Link;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;

public class IrisTextureWrapper extends AbstractTexture
{
    public final Link texture;
    public final AbstractTexture fallback;
    public final int index;

    public IrisTextureWrapper(Link texture, int index)
    {
        this(texture, null, index);
    }

    public IrisTextureWrapper(Link texture, AbstractTexture fallback, int index)
    {
        this.texture = texture;
        this.fallback = fallback;
        this.index = index;
    }

    public void load(ResourceManager manager) throws IOException
    {}

    public int getGlId()
    {
        /* PBR texture bridging is disabled on MC 26.2 because the Iris PBR API
           changed and GL texture ids no longer exist (GpuTexture). Returning -1
           signals "no GL texture" so Iris skips the sampler instead of binding a
           bogus BBS texture handle. */
        return -1;
    }

    @Override
    public void close()
    {
        BBSModClient.getTextures().delete(this.texture);
    }
}
