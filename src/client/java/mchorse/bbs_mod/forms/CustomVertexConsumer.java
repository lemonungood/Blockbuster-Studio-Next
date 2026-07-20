package mchorse.bbs_mod.forms;

import com.mojang.blaze3d.systems.RenderSystem;
import mchorse.bbs_mod.forms.renderers.utils.RecolorVertexConsumer;
import com.mojang.blaze3d.vertex.BufferBuilder;
// [MC 26.2 REMOVED] import net.minecraft.client.renderer.rendertype.RenderTypes;
import com.mojang.blaze3d.vertex.VertexConsumer;
// [MC 26.2 REMOVED] import com.mojang.blaze3d.vertex.VertexConsumer;
import org.lwjgl.opengl.GL11;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class CustomVertexConsumer extends VertexConsumer.Immediate
{
    private static Consumer<RenderTypes> runnables;

    private Function<VertexConsumer, VertexConsumer> substitute;
    private boolean ui;

    public static void drawLayer(RenderTypes layer)
    {
        if (runnables != null)
        {
            runnables.accept(layer);
        }
    }

    public static void hijackVertexFormat(Consumer<RenderTypes> runnable)
    {
        runnables = runnable;
    }

    public static void clearRunnables()
    {
        runnables = null;
    }

    public CustomVertexConsumer(BufferBuilder fallback, Map<RenderTypes, BufferBuilder> layers)
    {
        super(fallback, layers);
    }

    public void setSubstitute(Function<VertexConsumer, VertexConsumer> substitute)
    {
        this.substitute = substitute;

        if (this.substitute == null)
        {
            RecolorVertexConsumer.newColor = null;
        }
    }

    public void setUI(boolean ui)
    {
        this.ui = ui;
    }

    @Override
    public VertexConsumer getBuffer(RenderTypes renderLayer)
    {
        VertexConsumer buffer = super.getBuffer(renderLayer);

        if (this.substitute != null)
        {
            VertexConsumer apply = this.substitute.apply(buffer);

            if (apply != null)
            {
                return apply;
            }
        }

        return buffer;
    }

    public void draw()
    {
        super.draw();

        if (this.ui)
        {
            /* Force back the depth func because it seems like stuff rendered by a vertex
             * consumer is resetting the depth func to GL_LESS, and since this vertex consumer
             * is designed  */
            RenderSystem.depthFunc(GL11.GL_ALWAYS);
        }
    }
}
