package mchorse.bbs_mod.ui.film.clips.renderer;

import mchorse.bbs_mod.BBSSettings;
import mchorse.bbs_mod.camera.clips.ClipFactoryData;
import mchorse.bbs_mod.ui.film.UIClips;
import mchorse.bbs_mod.ui.framework.UIContext;
import mchorse.bbs_mod.ui.framework.elements.utils.FontRenderer;
import mchorse.bbs_mod.ui.utils.Area;
import mchorse.bbs_mod.ui.utils.icons.Icons;
import mchorse.bbs_mod.utils.MathUtils;
import mchorse.bbs_mod.utils.clips.Clip;
import mchorse.bbs_mod.utils.clips.Envelope;
import mchorse.bbs_mod.utils.colors.Color;
import mchorse.bbs_mod.utils.colors.Colors;
import mchorse.bbs_mod.utils.keyframes.Keyframe;
import mchorse.bbs_mod.utils.keyframes.KeyframeChannel;
import com.mojang.blaze3d.vertex.BufferBuilder;
// [MC 26.2 REMOVED] import com.mojang.blaze3d.vertex.BufferUploader;
// [MC 26.2 REMOVED] import com.mojang.blaze3d.vertex.Tessellator;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import org.joml.Matrix4f;
import org.joml.Vector2f;

public class UIClipRenderer <T extends Clip> implements IUIClipRenderer<T>
{
    private static final Color ENVELOPE_COLOR = new Color(0, 0, 0, 0.25F);

    /* Temporary objects */
    private static Vector2f vector = new Vector2f();
    private static Vector2f previous = new Vector2f();

    @Override
    public void renderClip(UIContext context, UIClips clips, T clip, Area area, boolean selected, boolean current)
    {
        int y = area.y;
        int h = area.h;

        int left = area.x;
        int right = area.ex();

        if (current)
        {
            int color = BBSSettings.primaryColor.get();

            context.batcher.dropShadow(left + 2, y + 2, right - 2, y + h - 2, 8, Colors.A75 + color, color);
        }

        ClipFactoryData data = clips.getFactory().getData(clip);
        int color = Colors.A100 | data.color;

        if (clip.enabled.get())
        {
            this.renderBackground(context, color, clip, area, selected, current);
        }
        else
        {
            context.batcher.iconArea(Icons.DISABLED, color, left, y, (right - left), h);
        }

        context.batcher.outline(left, y, right, y + h, selected ? Colors.WHITE : Colors.A50);

        if (right - left > 10 && clip.envelope.enabled.get())
        {
            this.renderEnvelope(context, clip.envelope, clip.duration.get(), left + 1, y + 1, right - 1, y + 17);
        }

        FontRenderer font = context.batcher.getFont();
        String label = font.limitToWidth(clip.title.get(), right - 6 - left);

        if (right - left >= 20)
        {
            context.batcher.icon(data.icon, Colors.mulA(Colors.mulRGB(Colors.WHITE, 0.75F), 0.5F), right - 2, y + h / 2, 1F, 0.5F);
        }

        if (!label.isEmpty())
        {
            context.batcher.textShadow(label, left + 5, y + (h - font.getHeight()) / 2);
        }
    }

    protected void renderBackground(UIContext context, int color, T clip, Area area, boolean selected, boolean current)
    {
        context.batcher.box(area.x, area.y, area.ex(), area.ey(), color);
    }

    /**
     * Render envelope's preview (either through keyframes or simple)
     */
    private void renderEnvelope(UIContext context, Envelope envelope, int duration, int x1, int y1, int x2, int y2)
    {
        ByteBufferBuilder byteBuf = new ByteBufferBuilder(4096);
        BufferBuilder builder = new BufferBuilder(byteBuf, PrimitiveTopology.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f matrix = new Matrix4f();

        if (envelope.keyframes.get())
        {
            if (!envelope.channel.isEmpty())
            {
                this.renderEnvelopesKeyframes(builder, matrix, envelope.channel, duration, x1, y1, x2, y2);
            }
        }
        else
        {
            this.renderSimpleEnvelope(builder, matrix, envelope, duration, x1, y1, x2, y2);
        }

        // MeshData is built and discarded - Batcher2D handles screen rendering
        builder.buildOrThrow();
    }

    /**
     * Render keyframe based envelope.
     */
    private void renderEnvelopesKeyframes(BufferBuilder builder, Matrix4f matrix, KeyframeChannel<Double> channel, int duration, int x1, int y1, int x2, int y2)
    {
        Keyframe<Double> prevKeyframe = null;
        int c = ENVELOPE_COLOR.getARGBColor();
        float r = ((c >> 16) & 0xFF) / 255.0F;
        float g = ((c >> 8) & 0xFF) / 255.0F;
        float b = (c & 0xFF) / 255.0F;
        float a = ((c >> 24) & 0xFF) / 255.0F;

        for (Keyframe<Double> keyframe : channel.getKeyframes())
        {
            if (prevKeyframe != null)
            {
                Vector2f point = this.calculateEnvelopePoint(vector, (int) keyframe.getTick(), keyframe.getValue().floatValue(), duration, x1, y1, x2, y2);
                Vector2f prevPoint = this.calculateEnvelopePoint(previous, (int) prevKeyframe.getTick(), prevKeyframe.getValue().floatValue(), duration, x1, y1, x2, y2);

                builder.addVertex(matrix, prevPoint.x, y2, 0F).setColor(r, g, b, a);
                builder.addVertex(matrix, point.x, point.y, 0F).setColor(r, g, b, a);
                builder.addVertex(matrix, prevPoint.x, prevPoint.y, 0F).setColor(r, g, b, a);

                builder.addVertex(matrix, point.x, y2, 0F).setColor(r, g, b, a);
                builder.addVertex(matrix, point.x, point.y, 0F).setColor(r, g, b, a);
                builder.addVertex(matrix, prevPoint.x, y2, 0F).setColor(r, g, b, a);
            }

            prevKeyframe = keyframe;
        }

        /* Finish the end */
        if (prevKeyframe != null && prevKeyframe.getTick() < duration)
        {
            Vector2f point = this.calculateEnvelopePoint(vector, (int) prevKeyframe.getTick(), prevKeyframe.getValue().floatValue(), duration, x1, y1, x2, y2);

            builder.addVertex(matrix, point.x, y2, 0F).setColor(r, g, b, a);
            builder.addVertex(matrix, x2, point.y, 0F).setColor(r, g, b, a);
            builder.addVertex(matrix, point.x, point.y, 0F).setColor(r, g, b, a);

            builder.addVertex(matrix, x2, y2, 0F).setColor(r, g, b, a);
            builder.addVertex(matrix, x2, point.y, 0F).setColor(r, g, b, a);
            builder.addVertex(matrix, point.x, y2, 0F).setColor(r, g, b, a);
        }
    }

    /**
     * Render simple envelope (using start and end values).
     */
    protected void renderSimpleEnvelope(BufferBuilder builder, Matrix4f matrix, Envelope envelope, int duration, int x1, int y1, int x2, int y2)
    {
        /* First triangle */
        int c = ENVELOPE_COLOR.getARGBColor();
        float r = ((c >> 16) & 0xFF) / 255.0F;
        float g = ((c >> 8) & 0xFF) / 255.0F;
        float b = (c & 0xFF) / 255.0F;
        float a = ((c >> 24) & 0xFF) / 255.0F;
        Vector2f point = this.calculateEnvelopePoint(vector, (int) envelope.getStartX(duration), 0, duration, x1, y1, x2, y2);
        builder.addVertex(matrix, point.x, point.y, 0F).setColor(r, g, b, a);

        previous.set(point);
        point = this.calculateEnvelopePoint(vector, (int) envelope.getStartDuration(duration), 1, duration, x1, y1, x2, y2);
        builder.addVertex(matrix, point.x, y2, 0F).setColor(r, g, b, a);
        builder.addVertex(matrix, point.x, point.y, 0F).setColor(r, g, b, a);

        /* Second triangle */
        previous.set(point);
        point = this.calculateEnvelopePoint(vector, (int) envelope.getEndDuration(duration), 1, duration, x1, y1, x2, y2);
        builder.addVertex(matrix, point.x, point.y, 0F).setColor(r, g, b, a);
        builder.addVertex(matrix, previous.x, y2, 0F).setColor(r, g, b, a);
        builder.addVertex(matrix, point.x, y2, 0F).setColor(r, g, b, a);

        /* Third triangle */
        builder.addVertex(matrix, point.x, point.y, 0F).setColor(r, g, b, a);
        builder.addVertex(matrix, previous.x, previous.y, 0F).setColor(r, g, b, a);
        builder.addVertex(matrix, previous.x, y2, 0F).setColor(r, g, b, a);

        /* Fourth triangle */
        previous.set(point);
        point = this.calculateEnvelopePoint(vector, (int) envelope.getEndX(duration), 0, duration, x1, y1, x2, y2);
        builder.addVertex(matrix, previous.x, previous.y, 0F).setColor(r, g, b, a);
        builder.addVertex(matrix, previous.x, y2, 0F).setColor(r, g, b, a);
        builder.addVertex(matrix, point.x, point.y, 0F).setColor(r, g, b, a);
    }

    protected Vector2f calculateEnvelopePoint(Vector2f vector, int tick, float value, int duration, int x1, int y1, int x2, int y2)
    {
        int width = x2 - x1;
        int height = y2 - y1;

        /* 1 - value due to higher numbers are lower on the screen */
        vector.x = MathUtils.clamp((tick / (float) duration) * width + x1, x1, x2);
        vector.y = (1 - MathUtils.clamp(value, 0, 1)) * height + y1;

        return vector;
    }
}


