package mchorse.bbs_mod.client;

import mchorse.bbs_mod.BBSMod;
import mchorse.bbs_mod.client.ShaderProgram;
import net.minecraft.client.Minecraft;
// [MC 26.2 REMOVED] // [MC26.2] import com.mojang.blaze3d.shaders.ShaderProgram;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.resources.Identifier;

import java.io.IOException;
import java.util.Optional;

public class BBSShaders
{
    private static ShaderProgram model;
    private static ShaderProgram multiLink;
    private static ShaderProgram subtitles;

    private static ShaderProgram pickerPreview;
    private static ShaderProgram pickerBillboard;
    private static ShaderProgram pickerBillboardNoShading;
    private static ShaderProgram pickerParticles;
    private static ShaderProgram pickerModels;

    static
    {
        setup();
    }

    public static void setup()
    {
        // [MC 26.2] ShaderProgram no longer has close(); model field reused via reassignment
        // Also ShaderProgram constructor changed to (Identifier, VertexFormat) - no resource loading

        try
        {
            ResourceProvider factory = new ProxyResourceProvider(Minecraft.getInstance().getResourceManager());

            model = new ShaderProgram(Identifier.parse("bbs:model"), DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP);
            multiLink = new ShaderProgram(Identifier.parse("bbs:multilink"), DefaultVertexFormat.POSITION_TEX_COLOR);
            subtitles = new ShaderProgram(Identifier.parse("bbs:subtitles"), DefaultVertexFormat.POSITION_TEX_COLOR);

            pickerPreview = new ShaderProgram(Identifier.parse("bbs:picker_preview"), DefaultVertexFormat.POSITION_TEX_COLOR);
            pickerBillboard = new ShaderProgram(Identifier.parse("bbs:picker_billboard"), DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP);
            pickerBillboardNoShading = new ShaderProgram(Identifier.parse("bbs:picker_billboard_no_shading"), DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP);
            pickerParticles = new ShaderProgram(Identifier.parse("bbs:picker_particles"), DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP);
            pickerModels = new ShaderProgram(Identifier.parse("bbs:picker_models"), DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static ShaderProgram getModel()
    {
        return model;
    }

    public static ShaderProgram getMultilinkProgram()
    {
        return multiLink;
    }

    public static ShaderProgram getSubtitlesProgram()
    {
        return subtitles;
    }

    public static ShaderProgram getPickerPreviewProgram()
    {
        return pickerPreview;
    }

    public static ShaderProgram getPickerBillboardProgram()
    {
        return pickerBillboard;
    }

    public static ShaderProgram getPickerBillboardNoShadingProgram()
    {
        return pickerBillboardNoShading;
    }

    public static ShaderProgram getPickerParticlesProgram()
    {
        return pickerParticles;
    }

    public static ShaderProgram getPickerModelsProgram()
    {
        return pickerModels;
    }

    private static class ProxyResourceProvider implements ResourceProvider
    {
        private ResourceManager manager;

        public ProxyResourceProvider(ResourceManager manager)
        {
            this.manager = manager;
        }

        @Override
        public Optional<Resource> getResource(Identifier id)
        {
            if (id.getPath().contains("/core/"))
            {
                return this.manager.getResource(Identifier.parse(BBSMod.MOD_ID + ":" + id.getPath()));
            }

            return this.manager.getResource(id);
        }
    }
}



