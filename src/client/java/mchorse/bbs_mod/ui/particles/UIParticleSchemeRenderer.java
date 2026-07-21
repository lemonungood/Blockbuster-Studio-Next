package mchorse.bbs_mod.ui.particles;

import mchorse.bbs_mod.graphics.Draw;
import mchorse.bbs_mod.particles.ParticleScheme;
import mchorse.bbs_mod.particles.components.expiration.ParticleComponentKillPlane;
import mchorse.bbs_mod.particles.emitter.ParticleEmitter;
import mchorse.bbs_mod.ui.framework.UIBaseMenu;
import mchorse.bbs_mod.ui.framework.UIContext;
import mchorse.bbs_mod.ui.framework.elements.utils.UIModelRenderer;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.PrimitiveTopology;
import net.minecraft.client.renderer.RenderPipelines;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class UIParticleSchemeRenderer extends UIModelRenderer
{
    public ParticleEmitter emitter;

    private Vector3f vector = new Vector3f(0, 0, 0);

    public UIParticleSchemeRenderer()
    {
        super();

        this.emitter = new ParticleEmitter();
    }

    public void setScheme(ParticleScheme scheme)
    {
        this.emitter = new ParticleEmitter();
        this.emitter.setScheme(scheme);
    }

    @Override
    protected void update()
    {
        super.update();

        if (this.emitter != null)
        {
            this.emitter.rotation.identity();
            this.emitter.update();
        }
    }

    @Override
    protected void renderUserModel(UIContext context)
    {
        if (this.emitter == null || this.emitter.scheme == null)
        {
            return;
        }

        this.emitter.setupCameraProperties(this.camera);
        this.emitter.rotation.identity();

        /* LightmapTextureManager removed in MC 26.2 */

        PoseStack stack = new PoseStack();

        stack.pushPose();
        stack.setIdentity();

        this.emitter.render(DefaultVertexFormat.PARTICLE, () -> null, stack, 0, context.getTransition());

        stack.popPose();

        ParticleComponentKillPlane plane = this.emitter.scheme.get(ParticleComponentKillPlane.class);

        if (plane.a != 0 || plane.b != 0 || plane.c != 0)
        {
            this.renderPlane(context, plane.a, plane.b, plane.c, plane.d);
        }
    }

    private void renderPlane(UIContext context, float a, float b, float c, float d)
    {
        Matrix4f matrix = new Matrix4f();
        BufferBuilder builder = new BufferBuilder(new ByteBufferBuilder(256), PrimitiveTopology.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        final float alpha = 0.5F;
        final int color = (int) (alpha * 255F);

        this.calculate(0, 0, a, b, c, d);
        builder.addVertex(matrix, this.vector.x, this.vector.y, this.vector.z).setColor(0, 255, 0, color);
        this.calculate(0, 1, a, b, c, d);
        builder.addVertex(matrix, this.vector.x, this.vector.y, this.vector.z).setColor(0, 255, 0, color);
        this.calculate(1, 0, a, b, c, d);
        builder.addVertex(matrix, this.vector.x, this.vector.y, this.vector.z).setColor(0, 255, 0, color);

        this.calculate(1, 0, a, b, c, d);
        builder.addVertex(matrix, this.vector.x, this.vector.y, this.vector.z).setColor(0, 255, 0, color);
        this.calculate(0, 1, a, b, c, d);
        builder.addVertex(matrix, this.vector.x, this.vector.y, this.vector.z).setColor(0, 255, 0, color);
        this.calculate(1, 1, a, b, c, d);
        builder.addVertex(matrix, this.vector.x, this.vector.y, this.vector.z).setColor(0, 255, 0, color);

        Draw.drawBuffer(builder, RenderPipelines.DEBUG_FILLED_BOX);
    }

    private void calculate(float i, float j, float a, float b, float c, float d)
    {
        final float radius = 5;

        if (b != 0)
        {
            this.vector.x = -radius + radius * 2 * i;
            this.vector.z = -radius + radius * 2 * j;
            this.vector.y = (a * this.vector.x + c * this.vector.z + d) / -b;
        }
        else if (a != 0)
        {
            this.vector.y = -radius + radius * 2 * i;
            this.vector.z = -radius + radius * 2 * j;
            this.vector.x = (b * this.vector.y + c * this.vector.z + d) / -a;
        }
        else if (c != 0)
        {
            this.vector.x = -radius + radius * 2 * i;
            this.vector.y = -radius + radius * 2 * j;
            this.vector.z = (b * this.vector.y + a * this.vector.x + d) / -c;
        }
    }

    @Override
    protected void renderGrid(UIContext context)
    {
        super.renderGrid(context);

        if (UIBaseMenu.renderAxes)
        {
            Draw.coolerAxes(new PoseStack(), 1F, 0.01F, 1.01F, 0.02F);
        }
    }
}
