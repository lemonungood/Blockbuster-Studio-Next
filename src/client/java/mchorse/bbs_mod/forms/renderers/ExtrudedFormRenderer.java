package mchorse.bbs_mod.forms.renderers;

import com.mojang.blaze3d.systems.RenderSystem;
import mchorse.bbs_mod.BBSModClient;
import mchorse.bbs_mod.client.BBSRendering;
import mchorse.bbs_mod.client.BBSShaders;
import mchorse.bbs_mod.cubic.render.vao.ModelVAO;
import mchorse.bbs_mod.cubic.render.vao.ModelVAORenderer;
import mchorse.bbs_mod.forms.forms.ExtrudedForm;
import mchorse.bbs_mod.resources.Link;
import mchorse.bbs_mod.ui.framework.UIContext;
import mchorse.bbs_mod.utils.PoseStackUtils;
import mchorse.bbs_mod.utils.colors.Color;
import mchorse.bbs_mod.utils.colors.Colors;
import mchorse.bbs_mod.utils.joml.Vectors;
import net.minecraft.client.Minecraft;
import mchorse.bbs_mod.client.ShaderProgram;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.function.Supplier;

public class ExtrudedFormRenderer extends FormRenderer<ExtrudedForm>
{
    public ExtrudedFormRenderer(ExtrudedForm form)
    {
        super(form);
    }

    @Override
    public void renderInUI(UIContext context, int x1, int y1, int x2, int y2)
    {
        PoseStack stack = new PoseStack();

        stack.pushPose();

        Matrix4f uiMatrix = ModelFormRenderer.getUIMatrix(context, x1, y1, x2, y2);

        this.applyTransforms(uiMatrix, context.getTransition());
        PoseStackUtils.multiply(stack, uiMatrix);
        stack.translate(0F, 1F, 0F);
        stack.scale(1.5F, 1.5F, 4F);
        stack.scale(this.form.uiScale.get(), this.form.uiScale.get(), this.form.uiScale.get());

        /* Shading fix */
        stack.last().normal().getScale(Vectors.EMPTY_3F);
        stack.last().normal().scale(1F / Vectors.EMPTY_3F.x, -1F / Vectors.EMPTY_3F.y, 1F / Vectors.EMPTY_3F.z);

        // [MC 26.2] RenderSystem.depthFunc removed
        this.renderModel(BBSShaders::getModel,
            stack,
            0, 15728880, Colors.WHITE,
            context.getTransition()
        );
        // [MC 26.2] RenderSystem.depthFunc removed

        stack.popPose();
    }

    @Override
    protected void render3D(FormRenderingContext context)
    {
        boolean shading = this.form.shading.get();

        if (BBSRendering.isIrisShadersEnabled())
        {
            shading = true;
        }

        // [MC 26.2] DefaultVertexFormat names changed
        VertexFormat format = shading ? DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP : DefaultVertexFormat.POSITION_TEX_LIGHTMAP_COLOR;
        Supplier<ShaderProgram> shader = this.getShader(context,
            shading ? BBSShaders::getModel : BBSShaders::getModel,
            shading ? BBSShaders::getPickerBillboardProgram : BBSShaders::getPickerBillboardNoShadingProgram
        );

        this.renderModel(shader, context.stack, context.overlay, context.light, context.color, context.getTransition());
    }

    private void renderModel(Supplier<ShaderProgram> shader, PoseStack matrices, int overlay, int light, int overlayColor, float transition)
    {
        Link texture = this.form.texture.get();
        ModelVAO data = BBSModClient.getTextures().getExtruder().get(texture);

        if (data != null)
        {
            if (this.form.billboard.get())
            {
                Matrix4f modelMatrix = matrices.last().pose();
                Vector3f scale = Vectors.TEMP_3F;

                modelMatrix.getScale(scale);

                modelMatrix.m00(1).m01(0).m02(0);
                modelMatrix.m10(0).m11(1).m12(0);
                modelMatrix.m20(0).m21(0).m22(1);

                modelMatrix.scale(scale);

                matrices.last().normal().identity();
            }

            Color color = Colors.COLOR.set(overlayColor, true);
            Color formColor = this.form.color.get();

            BBSModClient.getTextures().bindTexture(texture);

            // [MC 26.2] RenderSystem.enableBlend/defaultBlendFunc/disableBlend removed
            // [MC 26.2] gameRenderer.getLightmapTextureManager/getOverlayTexture removed

            ModelVAORenderer.render(shader.get(), data, matrices, color.r * formColor.r, color.g * formColor.g, color.b * formColor.b, color.a * formColor.a, light, overlay);
        }
    }
}
