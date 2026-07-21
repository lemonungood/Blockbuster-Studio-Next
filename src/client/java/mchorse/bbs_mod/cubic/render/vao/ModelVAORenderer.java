package mchorse.bbs_mod.cubic.render.vao;

import com.mojang.blaze3d.systems.RenderSystem;
// [MC26.2] import net.minecraft.client.renderer.GlUniform;
import mchorse.bbs_mod.client.ShaderProgram;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL30;

public class ModelVAORenderer
{
    public static void render(ShaderProgram shader, IModelVAO modelVAO, PoseStack stack, float r, float g, float b, float a, int light, int overlay)
    {
        int currentVAO = GL30.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING);
        int currentElementArrayBuffer = GL30.glGetInteger(GL30.GL_ELEMENT_ARRAY_BUFFER_BINDING);

        setupUniforms(stack, shader);

        shader.bind();
        modelVAO.render(shader.getVertexFormat(), r, g, b, a, light, overlay);
        shader.unbind();

        GL30.glBindVertexArray(currentVAO);
        GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, currentElementArrayBuffer);
    }

    public static void setupUniforms(PoseStack stack, ShaderProgram shader)
    {
        // [MC 26.2 removed] RenderSystem.getShaderTexture(int) removed
        // [MC 26.2 removed] shader.addSampler() removed
        /*
        for (int i = 0; i < 12; i++)
        {
            shader.addSampler("Sampler" + i, RenderSystem.getShaderTexture(i));
        }
        */

        // [MC 26.2] shader.projectionMat/modelViewMat/gameTime/textureMat/colorModulator fields removed from ShaderProgram
        // [MC 26.2] RenderSystem.setupShaderLights/getShaderGameTime/getTextureMatrix all removed
        // Uniforms are now set via shader.setUniform(location, ...) directly
    }
}



