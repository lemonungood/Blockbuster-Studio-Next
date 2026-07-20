package mchorse.bbs_mod.utils.iris;

import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.server.packs.resources.ResourceManager;

/**
 * IrisTextureWrapperLoader - disabled in MC 26.2 because Iris PBR API changed.
 */
public class IrisTextureWrapperLoader
{
    public void load(AbstractTexture abstractTexture, ResourceManager resourceManager)
    {
        // PBR texture loading disabled until Iris API for 26.2 is stable
    }
}
