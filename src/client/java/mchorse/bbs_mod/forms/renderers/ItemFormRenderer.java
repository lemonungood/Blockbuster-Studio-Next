package mchorse.bbs_mod.forms.renderers;

import com.mojang.blaze3d.systems.RenderSystem;
import mchorse.bbs_mod.client.BBSRendering;
import mchorse.bbs_mod.client.BBSShaders;
import mchorse.bbs_mod.forms.CustomVertexConsumer;
import mchorse.bbs_mod.forms.FormUtilsClient;
import mchorse.bbs_mod.forms.forms.ItemForm;
import mchorse.bbs_mod.ui.framework.UIContext;
import mchorse.bbs_mod.utils.PoseStackUtils;
import mchorse.bbs_mod.utils.colors.Color;
import mchorse.bbs_mod.utils.joml.Vectors;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix4f;

public class ItemFormRenderer extends FormRenderer<ItemForm>
{
    public ItemFormRenderer(ItemForm form)
    {
        super(form);
    }

    @Override
    public void renderInUI(UIContext context, int x1, int y1, int x2, int y2)
    {
        // [MC 26.2] context.batcher.getContext().draw() removed
        CustomVertexConsumer consumers = FormUtilsClient.getProvider();
        PoseStack matrices = new PoseStack();

        Matrix4f uiMatrix = ModelFormRenderer.getUIMatrix(context, x1, y1, x2, y2);

        matrices.pushPose();
        PoseStackUtils.multiply(matrices, uiMatrix);
        matrices.scale(this.form.uiScale.get(), this.form.uiScale.get(), this.form.uiScale.get());

        matrices.last().normal().getScale(Vectors.EMPTY_3F);
        matrices.last().normal().scale(1F / Vectors.EMPTY_3F.x, -1F / Vectors.EMPTY_3F.y, 1F / Vectors.EMPTY_3F.z);

        Color set = this.form.color.get();

        // [MC 26.2] consumers.setSubstitute/setUI/draw/getItemRenderer/renderItem removed
        // Item rendering in UI is disabled until MC 26.2 API is adapted

        matrices.popPose();
    }

    @Override
    protected void render3D(FormRenderingContext context)
    {
        CustomVertexConsumer consumers = FormUtilsClient.getProvider();
        int light = context.light;

        context.stack.pushPose();

        if (context.isPicking())
        {
            // [MC 26.2] CustomVertexConsumer.hijackVertexFormat/RenderSystem.setShader removed
            this.setupTarget(context, BBSShaders.getPickerModelsProgram());
            light = 0;
        }
        else
        {
            // [MC 26.2] RenderSystem.enableBlend removed
        }

        Color set = this.form.color.get();

        BlockFormRenderer.color.set(context.color);
        BlockFormRenderer.color.mul(set);

        // [MC 26.2] consumers.setSubstitute/getItemRenderer/renderItem/consumers.draw/clearRunnables removed
        // Item 3D rendering is disabled until MC 26.2 API is adapted

        context.stack.popPose();

        // enableDepthTest removed;
    }
}
