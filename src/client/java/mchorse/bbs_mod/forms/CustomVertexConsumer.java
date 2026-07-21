package mchorse.bbs_mod.forms;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.rendertype.RenderTypes;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * CustomVertexConsumer - provides multi-layer buffer building for BBS forms.
 * In MC 26.2, VertexConsumer.Immediate was removed, so we use a different approach.
 */
public class CustomVertexConsumer implements VertexConsumer
{
    private static Consumer<RenderTypes> runnables;

    private final BufferBuilder fallback;
    private final Map<RenderTypes, BufferBuilder> layers;
    private boolean ui;
    private Function<VertexConsumer, VertexConsumer> substitute;

    public static void drawLayer(RenderTypes layer)
    {
        if (runnables != null)
        {
            runnables.accept(layer);
        }
    }

    public static void hijackVertexFormat(Consumer<RenderTypes> runnable)
    {
        CustomVertexConsumer.runnables = runnable;
    }

    public CustomVertexConsumer(BufferBuilder fallback)
    {
        this.fallback = fallback;
        this.layers = null;
    }

    public CustomVertexConsumer(BufferBuilder fallback, Map<RenderTypes, BufferBuilder> layers)
    {
        this.fallback = fallback;
        this.layers = layers;
    }

    public VertexConsumer getBuffer(RenderTypes renderLayer)
    {
        return this;
    }

    public static void clearRunnables()
    {
        runnables = null;
    }

    public void setUI(boolean ui)
    {
        this.ui = ui;
    }

    public void setSubstitute(Function<VertexConsumer, VertexConsumer> substitute)
    {
        this.substitute = substitute;
    }

    public void draw()
    {
        // No-op in MC 26.2 - BufferBuilder auto-submits during rendering
    }

    private VertexConsumer getEffective()
    {
        if (this.substitute != null)
        {
            return this.substitute.apply(this.fallback);
        }
        return this.fallback;
    }

    @Override
    public VertexConsumer addVertex(float x, float y, float z)
    {
        return getEffective().addVertex(x, y, z);
    }

    @Override
    public VertexConsumer setColor(int packed)
    {
        return getEffective().setColor(packed);
    }

    @Override
    public VertexConsumer setColor(int r, int g, int b, int a)
    {
        return getEffective().setColor(r, g, b, a);
    }

    @Override
    public VertexConsumer setUv(float u, float v)
    {
        return getEffective().setUv(u, v);
    }

    @Override
    public VertexConsumer setUv1(int u, int v)
    {
        return getEffective().setUv1(u, v);
    }

    @Override
    public VertexConsumer setUv2(int u, int v)
    {
        return getEffective().setUv2(u, v);
    }

    @Override
    public VertexConsumer setNormal(float x, float y, float z)
    {
        return getEffective().setNormal(x, y, z);
    }

    @Override
    public VertexConsumer setLineWidth(float width)
    {
        return this;
    }
}
