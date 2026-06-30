package mchorse.bbs_mod.client;

import mchorse.bbs_mod.BBSMod;
import net.minecraft.client.Minecraft;
// [MC 26.2 REMOVED] // [MC26.2] import net.minecraft.client.gl.ShaderProgram;
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
        if (model != null) model.close();
        if (subtitles != null) subtitles.close();
        if (subtitles != null) subtitles.close();

        if (pickerPreview != null) pickerPreview.close();
        if (pickerBillboard != null) pickerBillboard.close();
        if (pickerBillboardNoShading != null) pickerBillboardNoShading.close();
        if (pickerParticles != null) pickerParticles.close();
        if (pickerModels != null) pickerModels.close();

        try
        {
            ResourceProvider factory = new ProxyResourceProvider(Minecraft.getInstance().getResourceManager());

            model = new ShaderProgram(factory, "model", DefaultVertexFormat.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL);
            multiLink = new ShaderProgram(factory, "multilink", DefaultVertexFormat.POSITION_TEXTURE_COLOR);
            subtitles = new ShaderProgram(factory, "subtitles", DefaultVertexFormat.POSITION_TEXTURE_COLOR);

            pickerPreview = new ShaderProgram(factory, "picker_preview", DefaultVertexFormat.POSITION_TEXTURE_COLOR);
            pickerBillboard = new ShaderProgram(factory, "picker_billboard", DefaultVertexFormat.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL);
            pickerBillboardNoShading = new ShaderProgram(factory, "picker_billboard_no_shading", DefaultVertexFormat.POSITION_TEXTURE_LIGHT_COLOR);
            pickerParticles = new ShaderProgram(factory, "picker_particles", DefaultVertexFormat.POSITION_COLOR_TEXTURE_LIGHT);
            pickerModels = new ShaderProgram(factory, "picker_models", DefaultVertexFormat.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL);
        }
        catch (IOException e)
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
                return this.manager.getResource(new Identifier(BBSMod.MOD_ID, id.getPath()));
            }

            return this.manager.getResource(id);
        }
    }
}



