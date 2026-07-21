package mchorse.bbs_mod.ui.film;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
// [MC26.2] 
import mchorse.bbs_mod.BBSModClient;
import mchorse.bbs_mod.camera.clips.misc.Subtitle;
import mchorse.bbs_mod.client.BBSShaders;
import mchorse.bbs_mod.graphics.Framebuffer;
import mchorse.bbs_mod.graphics.texture.Texture;
import mchorse.bbs_mod.resources.Link;
import mchorse.bbs_mod.ui.framework.elements.utils.Batcher2D;
import mchorse.bbs_mod.ui.framework.elements.utils.FontRenderer;
import mchorse.bbs_mod.utils.PoseStackUtils;
import mchorse.bbs_mod.utils.StringUtils;
import mchorse.bbs_mod.utils.colors.Colors;
import mchorse.bbs_mod.utils.pose.Transform;
import net.minecraft.client.Minecraft;
// [MC26.2] import net.minecraft.client.renderer.GlUniform;
import mchorse.bbs_mod.client.ShaderProgram;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class UISubtitleRenderer
{
    private static Framebuffer getTextFramebuffer()
    {
        return BBSModClient.getFramebuffers().getFramebuffer(Link.bbs("camera_subtitles"), (f) ->
        {
            Texture texture = BBSModClient.getTextures().createTexture(Link.bbs("test"));

            texture.setFilter(GL11.GL_NEAREST);
            texture.setWrap(GL13.GL_CLAMP_TO_EDGE);

            f.deleteTextures();
            f.attach(texture, GL30.GL_COLOR_ATTACHMENT0);

            f.unbind();
        });
    }

    public static void renderSubtitles(PoseStack stack, Batcher2D batcher, List<Subtitle> subtitles)
    {
        if (subtitles.isEmpty())
        {
            return;
        }

        ShaderProgram program = BBSShaders.getSubtitlesProgram();
        int blurUniform = program.getUniformLocation("Blur");
        int textureSizeUniform = program.getUniformLocation("TextureSize");
        Supplier<ShaderProgram> supplier = () -> program;

        com.mojang.blaze3d.pipeline.RenderTarget fb = Minecraft.getInstance().gameRenderer.mainRenderTarget();
        int width = fb.width;
        int height = fb.height;

        Matrix4f cache = new Matrix4f(new org.joml.Matrix4f());

        width /= 2;
        height /= 2;

        Framebuffer framebuffer = getTextFramebuffer();
        Texture texture = framebuffer.getMainTexture();
        Matrix4f ortho = new Matrix4f().ortho(0, width, height, 0, -100, 100);
        FontRenderer font = Batcher2D.getDefaultTextRenderer();

        for (Subtitle subtitle : subtitles)
        {
            float alpha = Colors.getA(subtitle.color);

            if (alpha <= 0)
            {
                continue;
            }

            String label = StringUtils.processColoredText(subtitle.label);
            int w = 0;
            int h = 0;
            int x = (int) (width * subtitle.windowX + subtitle.x);
            int y = (int) (height * subtitle.windowY + subtitle.y);
            float scale = subtitle.size;
            int subColor = subtitle.color;

            List<String> strings = subtitle.maxWidth <= 10 ? Arrays.asList(label) : font.wrap(label, subtitle.maxWidth);

            for (String string : strings)
            {
                w = Math.max(w, font.getWidth(string.trim()));
            }

            h = (strings.size() - 1) * subtitle.lineHeight + font.getHeight();

            int fw = (int) ((w + 10) * scale);
            int fh = (int) ((h + 10) * scale);

            framebuffer.resize(fw, fh);
            framebuffer.applyClear();

            int yy = 5;

            for (String string : strings)
            {
                string = string.trim();

                int xx = 5 + (w - font.getWidth(string)) / 2;

                if (Colors.getA(subtitle.backgroundColor) > 0)
                {
                    batcher.textCard(string, xx, yy, Colors.setA(subColor, 1F), Colors.mulA(subtitle.backgroundColor, (int) (alpha * 255F)), (int) subtitle.backgroundOffset, subtitle.textShadow);
                }
                else
                {
                    batcher.text(string, xx, yy, Colors.setA(subColor, 1F), subtitle.textShadow);
                }

                yy += subtitle.lineHeight;
            }

            /* Render the texture */
            /* fb binding removed in MC 26.2 */

            /* setProjectionMatrix removed in MC 26.2 */

            Transform transform = new Transform();

            transform.lerp(subtitle.transform, 1F - subtitle.factor);

            stack.pushPose();
            stack.translate(x, y, 0);
            PoseStackUtils.applyTransform(stack, transform);

            if (blurUniform >= 0)
            {
                program.setUniform(blurUniform, subtitle.shadow, subtitle.shadowOpaque ? 1F : 0F, 0F, 0F);
            }

            if (textureSizeUniform >= 0)
            {
                program.setUniform(textureSizeUniform, (float) texture.width, (float) texture.height, 0F, 0F);
            }

            // enableBlend removed in MC 26.2;

            batcher.texturedBox(supplier, texture.id, Colors.setA(Colors.WHITE, alpha), -fw * subtitle.anchorX, -fh * subtitle.anchorY, texture.width, texture.height, 0, 0, texture.width, texture.height, texture.width, texture.height);

            stack.popPose();
        }

        /* setProjectionMatrix removed in MC 26.2 */
    }
}



