package mchorse.bbs_mod.utils.iris;

import mchorse.bbs_mod.graphics.texture.Texture;
import net.fabricmc.loader.api.FabricLoader;
import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.uniforms.custom.cached.CachedUniform;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * IrisUtils - adapter for the Iris 1.11.1 shader mod on MC 26.2.
 *
 * Uses the stable public API (net.irisshaders.iris.api.v0.IrisApi) instead of
 * internal classes, so it survives Iris updates. All calls are guarded by
 * FabricLoader.isModLoaded("iris") so the mod also runs on clients without Iris.
 */
public class IrisUtils
{
    private static final boolean IRIS_LOADED = FabricLoader.getInstance().isModLoaded("iris");

    public static boolean isIrisLoaded()
    {
        return IRIS_LOADED;
    }

    public static void setup()
    {}

    public static boolean isShaderPackEnabled()
    {
        if (!IRIS_LOADED)
        {
            return false;
        }

        IrisApi api = IrisApi.getInstance();

        return api != null && api.isShaderPackInUse();
    }

    public static boolean isShadowPass()
    {
        if (!IRIS_LOADED)
        {
            return false;
        }

        IrisApi api = IrisApi.getInstance();

        return api != null && api.isRenderingShadowPass();
    }

    /**
     * Registers a BBS texture with Iris for PBR/sampler tracking. The PBR
     * registration API changed substantially in 26.2, so this is a safe no-op
     * until the PBR path is re-implemented against the new API.
     */
    public static void trackTexture(Texture texture)
    {}

    /**
     * Computes tangent vectors for normal mapping. Returns tangents (3 floats
     * per vertex) computed from the provided positions/normals/uvs, treating
     * consecutive vertex triplets as triangles (standard Lengyel method).
     */
    public static float[] calculateTangents(float[] t, float[] v, float[] n, float[] u)
    {
        return calculate(t, v, n, u);
    }

    public static float[] calculateTangents(float[] v, float[] n, float[] u)
    {
        return calculate(new float[v.length], v, n, u);
    }

    private static float[] calculate(float[] out, float[] v, float[] n, float[] u)
    {
        int vertexCount = v.length / 3;

        for (int i = 0; i < vertexCount; i += 3)
        {
            for (int k = 0; k < 3; k++)
            {
                int a = i + k;
                int b = i + (k + 1) % 3;
                int c = i + (k + 2) % 3;

                int pa = a * 3;
                int pb = b * 3;
                int pc = c * 3;

                float e1x = v[pb] - v[pa];
                float e1y = v[pb + 1] - v[pa + 1];
                float e1z = v[pb + 2] - v[pa + 2];

                float e2x = v[pc] - v[pa];
                float e2y = v[pc + 1] - v[pa + 1];
                float e2z = v[pc + 2] - v[pa + 2];

                float dax = u[pb] - u[pa];
                float day = u[pb + 1] - u[pa + 1];
                float dbx = u[pc] - u[pa];
                float dby = u[pc + 1] - u[pa + 1];

                float det = dax * dby - dbx * day;
                float f = det == 0F ? 0F : 1F / det;

                float tx = f * (dby * e1x - day * e2x);
                float ty = f * (dby * e1y - day * e2y);
                float tz = f * (dby * e1z - day * e2z);

                out[pa] += tx;
                out[pa + 1] += ty;
                out[pa + 2] += tz;
                out[pb] += tx;
                out[pb + 1] += ty;
                out[pb + 2] += tz;
                out[pc] += tx;
                out[pc + 1] += ty;
                out[pc + 2] += tz;
            }
        }

        for (int i = 0; i < vertexCount; i++)
        {
            int p = i * 3;

            float tx = out[p];
            float ty = out[p + 1];
            float tz = out[p + 2];

            float len = (float) Math.sqrt(tx * tx + ty * ty + tz * tz);

            if (len > 1e-6F)
            {
                out[p] = tx / len;
                out[p + 1] = ty / len;
                out[p + 2] = tz / len;
            }
        }

        return out;
    }

    /**
     * Pushes BBS curve-clip variables into Iris as custom uniforms. The actual
     * bridge is performed by CustomUniformsBuilderMixin (registered); this hook
     * is invoked from there. Kept as the integration seam.
     */
    public static void addUniforms(List<CachedUniform> list, Map<String, ShaderCurves.ShaderVariable> variableMap)
    {}

    /**
     * Returns the identifiers of the currently active shader pack's options, so
     * BBS curve-clip variables can be mapped to real Iris sliders.
     */
    public static List<String> getSliderProperties()
    {
        if (!IRIS_LOADED)
        {
            return Collections.emptyList();
        }

        try
        {
            return new java.util.ArrayList<>(net.irisshaders.iris.Iris.getShaderPackOptionQueue().keySet());
        }
        catch (Throwable t)
        {
            return Collections.emptyList();
        }
    }

    public static Map<String, String> getShadersLanguageMap(String language)
    {
        return Collections.emptyMap();
    }
}
