package mchorse.bbs_mod.forms.renderers.utils;

import com.mojang.blaze3d.vertex.VertexConsumer;

/**
 * RecolorVertexSodiumConsumer - simplified for MC 26.2.
 * Sodium vertex format API changed, this provides a basic implementation.
 */
public class RecolorVertexSodiumConsumer implements VertexConsumer
{
    private final VertexConsumer parent;

    public RecolorVertexSodiumConsumer(VertexConsumer parent)
    {
        this.parent = parent;
    }

    @Override
    public VertexConsumer addVertex(float x, float y, float z)
    {
        return parent.addVertex(x, y, z);
    }

    @Override
    public VertexConsumer setColor(int packed)
    {
        return parent.setColor(packed);
    }

    @Override
    public VertexConsumer setColor(int r, int g, int b, int a)
    {
        return parent.setColor(r, g, b, a);
    }

    @Override
    public VertexConsumer setUv(float u, float v)
    {
        return parent.setUv(u, v);
    }

    @Override
    public VertexConsumer setUv1(int u, int v)
    {
        return parent.setUv1(u, v);
    }

    @Override
    public VertexConsumer setUv2(int u, int v)
    {
        return parent.setUv2(u, v);
    }

    @Override
    public VertexConsumer setNormal(float x, float y, float z)
    {
        return parent.setNormal(x, y, z);
    }

    @Override
    public VertexConsumer setLineWidth(float width)
    {
        return this;
    }
}
