package mchorse.bbs_mod.forms.renderers;

import com.mojang.blaze3d.systems.RenderSystem;
import mchorse.bbs_mod.client.BBSRendering;
import mchorse.bbs_mod.client.BBSShaders;
import mchorse.bbs_mod.forms.CustomVertexConsumer;
import mchorse.bbs_mod.forms.FormUtilsClient;
import mchorse.bbs_mod.forms.forms.BlockForm;
import mchorse.bbs_mod.ui.framework.UIContext;
import mchorse.bbs_mod.utils.PoseStackUtils;
import mchorse.bbs_mod.utils.colors.Color;
import mchorse.bbs_mod.utils.joml.Vectors;
import net.minecraft.client.Minecraft;
// [MC 26.2 REMOVED] import net.minecraft.client.renderer.LightTexture;
// [MC 26.2 REMOVED] import net.minecraft.client.renderer.texture.OverlayTexture;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix4f;

public class BlockFormRenderer extends FormRenderer<BlockForm>
{
    public static final Color color = new Color();

    public BlockFormRenderer(BlockForm form)
    {
        super(form);
    }

    @Override
    public void renderInUI(UIContext context, int x1, int y1, int x2, int y2)
    {
        context.batcher.getContext().draw();

        CustomVertexConsumer consumers = FormUtilsClient.getProvider();
        PoseStack matrices = context.batcher.getContext().getMatrices();

        Matrix4f uiMatrix = ModelFormRenderer.getUIMatrix(context, x1, y1, x2, y2);

        matrices.push();
        PoseStackUtils.multiply(matrices, uiMatrix);
        matrices.scale(this.form.uiScale.get(), this.form.uiScale.get(), this.form.uiScale.get());
        matrices.translate(-0.5F, 0F, -0.5F);

        matrices.peek().getNormalMatrix().getScale(Vectors.EMPTY_3F);
        matrices.peek().getNormalMatrix().scale(1F / Vectors.EMPTY_3F.x, -1F / Vectors.EMPTY_3F.y, 1F / Vectors.EMPTY_3F.z);

        Color set = this.form.color.get();

        consumers.setSubstitute(BBSRendering.getColorConsumer(set));
        consumers.setUI(true);
        Minecraft.getInstance().getBlockRenderManager().renderBlockAsEntity(this.form.blockState.get(), matrices, consumers, LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE, 0);
        consumers.draw();
        consumers.setUI(false);
        consumers.setSubstitute(null);

        matrices.pop();
    }

    @Override
    protected void render3D(FormRenderingContext context)
    {
        CustomVertexConsumer consumers = FormUtilsClient.getProvider();
        int light = context.light;

        context.stack.push();
        context.stack.translate(-0.5F, 0F, -0.5F);

        if (context.isPicking())
        {
            CustomVertexConsumer.hijackVertexFormat((layer) ->
            {
                this.setupTarget(context, BBSShaders.getPickerModelsProgram());
                RenderSystem.setShader(BBSShaders::getPickerModelsProgram);
            });

            light = 0;
        }
        else
        {
            CustomVertexConsumer.hijackVertexFormat((l) -> RenderSystem.enableBlend());
        }

        Color set = this.form.color.get();

        color.set(context.color);
        color.mul(set);

        consumers.setSubstitute(BBSRendering.getColorConsumer(set));
        Minecraft.getInstance().getBlockRenderManager().renderBlockAsEntity(this.form.blockState.get(), context.stack, consumers, light, context.overlay);
        consumers.draw();
        consumers.setSubstitute(null);

        CustomVertexConsumer.clearRunnables();

        context.stack.pop();

        RenderSystem.enableDepthTest();
    }
}


