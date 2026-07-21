package mchorse.bbs_mod.forms.renderers;

import com.mojang.blaze3d.systems.RenderSystem;
import mchorse.bbs_mod.client.BBSShaders;
import mchorse.bbs_mod.forms.CustomVertexConsumer;
import mchorse.bbs_mod.forms.FormUtilsClient;
import mchorse.bbs_mod.forms.forms.LabelForm;
import mchorse.bbs_mod.ui.framework.UIContext;
import mchorse.bbs_mod.ui.framework.elements.utils.FontRenderer;
import mchorse.bbs_mod.utils.PoseStackUtils;
import mchorse.bbs_mod.utils.StringUtils;
import mchorse.bbs_mod.utils.colors.Color;
import mchorse.bbs_mod.utils.joml.Vectors;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.PrimitiveTopology;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.List;

public class LabelFormRenderer extends FormRenderer<LabelForm>
{
    public static void fillQuad(BufferBuilder builder, PoseStack stack, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4, float r, float g, float b, float a)
    {
        Matrix4f matrix4f = stack.last().pose();

        /* 1 - BR, 2 - BL, 3 - TL, 4 - TR */
        builder.addVertex(matrix4f, x1, y1, z1).setColor(r, g, b, a).setUv(0F, 0F);
        builder.addVertex(matrix4f, x2, y2, z2).setColor(r, g, b, a).setUv(0F, 0F);
        builder.addVertex(matrix4f, x3, y3, z3).setColor(r, g, b, a).setUv(0F, 0F);
        builder.addVertex(matrix4f, x1, y1, z1).setColor(r, g, b, a).setUv(0F, 0F);
        builder.addVertex(matrix4f, x3, y3, z3).setColor(r, g, b, a).setUv(0F, 0F);
        builder.addVertex(matrix4f, x4, y4, z4).setColor(r, g, b, a).setUv(0F, 0F);
    }

    public LabelFormRenderer(LabelForm form)
    {
        super(form);
    }

    @Override
    public void renderInUI(UIContext context, int x1, int y1, int x2, int y2)
    {
        int color = this.form.color.get().getARGBColor();
        String text = StringUtils.processColoredText(this.form.text.get());
        List<String> wrap = context.batcher.getFont().wrap(text, x2 - x1 - 4);

        int th = context.batcher.getFont().getHeight();
        int lineHeight = th + 4;
        int h = th + (wrap.size() - 1) * lineHeight;
        int y = (y2 + y1) / 2 - h / 2;

        for (String s : wrap)
        {
            context.batcher.textShadow(s, x1 + 2, y, color);

            y += lineHeight;
        }
    }

    @Override
    public void render3D(FormRenderingContext context)
    {
        context.stack.pushPose();

        if (this.form.billboard.get())
        {
            Matrix4f modelMatrix = context.stack.last().pose();
            Vector3f scale = Vectors.TEMP_3F;

            modelMatrix.getScale(scale);

            modelMatrix.m00(1).m01(0).m02(0);
            modelMatrix.m10(0).m11(1).m12(0);
            modelMatrix.m20(0).m21(0).m22(1);

            modelMatrix.scale(scale);

            context.stack.last().normal().identity();
        }

        Font renderer = Minecraft.getInstance().font;
        CustomVertexConsumer consumers = FormUtilsClient.getProvider();
        float scale = 1F / 16F;
        int light = context.light;

        PoseStackUtils.scaleStack(context.stack, scale, -scale, scale);

        // disableCull removed in MC 26.2

        if (context.isPicking())
        {
            CustomVertexConsumer.hijackVertexFormat((layer) ->
            {
                this.setupTarget(context, BBSShaders.getPickerModelsProgram());
                // setShader removed in MC 26.2
            });

            light = 0;
        }

        if (this.form.max.get() <= 10)
        {
            this.renderString(context, consumers, renderer, light);
        }
        else
        {
            this.renderLimitedString(context, consumers, renderer, light);
        }

        CustomVertexConsumer.clearRunnables();

        // enableDepthTest removed;
        // enableCull removed in MC 26.2

        context.stack.popPose();
    }

    private void renderString(FormRenderingContext context, CustomVertexConsumer consumers, Font renderer, int light)
    {
        String content = StringUtils.processColoredText(this.form.text.get());
        float transition = context.getTransition();
        int w = renderer.width(content) - 1;
        int h = renderer.lineHeight - 2;
        int x = (int) (-w * this.form.anchorX.get());
        int y = (int) (-h * this.form.anchorY.get());

        Color shadowColor = this.form.shadowColor.get().copy();
        Color color = this.form.color.get().copy();

        color.mul(context.color);
        shadowColor.mul(context.color);

        if (shadowColor.a > 0)
        {
            context.stack.pushPose();
            context.stack.translate(0F, 0F, -0.1F);
            Font.PreparedText shadowText = renderer.prepareText(
                content,
                x + this.form.shadowX.get(),
                y + this.form.shadowY.get(),
                shadowColor.getARGBColor(), false,
                light
            );
            shadowText.visit(new Font.GlyphVisitor()
            {
                @Override
                public void acceptRenderable(net.minecraft.client.gui.font.TextRenderable renderable)
                {
                    renderable.render(context.stack.last().pose(), consumers, light, false);
                }
            });
            context.stack.popPose();
        }

        Font.PreparedText mainText = renderer.prepareText(
            content,
            x,
            y,
            color.getARGBColor(), false,
            light
        );
        mainText.visit(new Font.GlyphVisitor()
        {
            @Override
            public void acceptRenderable(net.minecraft.client.gui.font.TextRenderable renderable)
            {
                renderable.render(context.stack.last().pose(), consumers, light, false);
            }
        });

        // enableDepthTest removed;

        consumers.draw();

        this.renderShadow(context, x, y, w, h);
    }

    private void renderLimitedString(FormRenderingContext context, CustomVertexConsumer consumers, Font renderer, int light)
    {
        float transition = context.getTransition();
        int w = 0;
        int h = renderer.lineHeight - 2;
        String content = StringUtils.processColoredText(this.form.text.get());
        List<String> lines = FontRenderer.wrap(renderer, content, this.form.max.get());

        if (lines.size() <= 1)
        {
            this.renderString(context, consumers, renderer, light);

            return;
        }

        for (int i = 0; i < lines.size(); i++)
        {
            lines.set(i, lines.get(i).trim());
        }

        for (String line : lines)
        {
            w = Math.max(renderer.width(line) - 1, w);
            h += 12;
        }

        h -= 12;

        int x = (int) (-w * this.form.anchorX.get());
        int y = (int) (-h * this.form.anchorY.get());
        int y2 = y;

        Color shadowColor = this.form.shadowColor.get().copy();

        shadowColor.mul(context.color);

        if (shadowColor.a > 0)
        {
            context.stack.pushPose();
            context.stack.translate(0F, 0F, -0.1F);

            for (String line : lines)
            {
                int x2 = x + (this.form.anchorLines.get() ? (int) ((w - renderer.width(line)) * this.form.anchorX.get()) : 0);

                Font.PreparedText shadowLine = renderer.prepareText(
                    line,
                    x2 + this.form.shadowX.get(),
                    y2 + this.form.shadowY.get(),
                    shadowColor.getARGBColor(), false,
                    light
                );
                shadowLine.visit(new Font.GlyphVisitor()
                {
                    @Override
                    public void acceptRenderable(net.minecraft.client.gui.font.TextRenderable r)
                    {
                        r.render(context.stack.last().pose(), consumers, light, false);
                    }
                });

                y2 += 12;
            }

            context.stack.popPose();

            y2 = y;
        }

        Color cColor = this.form.color.get();

        cColor.mul(context.color);

        int color = cColor.getARGBColor();

        for (String line : lines)
        {
            int x2 = x + (this.form.anchorLines.get() ? (int) ((w - renderer.width(line)) * this.form.anchorX.get()) : 0);

            Font.PreparedText mainLine = renderer.prepareText(
                line,
                x2,
                y2,
                color, false,
                light
            );
            mainLine.visit(new Font.GlyphVisitor()
            {
                @Override
                public void acceptRenderable(net.minecraft.client.gui.font.TextRenderable r)
                {
                    r.render(context.stack.last().pose(), consumers, light, false);
                }
            });

            y2 += 12;
        }

        consumers.draw();

        // enableDepthTest removed;

        this.renderShadow(context, x, y, w, h);
    }

    private void renderShadow(FormRenderingContext context, int x, int y, int w, int h)
    {
        float offset = this.form.offset.get();
        Color color = this.form.background.get().copy();

        color.mul(context.color);

        if (color.a <= 0)
        {
            return;
        }

        context.stack.pushPose();
        context.stack.translate(0, 0, -0.2F);

        // Shadow rendering disabled in MC 26.2 (no BufferUploader API)

        context.stack.popPose();
    }
}


