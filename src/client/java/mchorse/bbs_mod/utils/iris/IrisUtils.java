package mchorse.bbs_mod.utils.iris;

import net.irisshaders.iris.uniforms.custom.cached.CachedUniform;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * IrisUtils - disabled in MC 26.2 because Iris API changed.
 * All methods are stubs that return safe defaults.
 */
public class IrisUtils
{
    public static boolean isIrisLoaded()
    {
        return false;
    }

    public static void setup()
    {
    }

    public static boolean isShaderPackEnabled()
    {
        return false;
    }

    public static boolean isShadowPass()
    {
        return false;
    }

    public static void trackTexture(mchorse.bbs_mod.graphics.texture.Texture texture)
    {
    }

    public static float[] calculateTangents(float[] t, float[] v, float[] n, float[] u)
    {
        return t;
    }

    public static float[] calculateTangents(float[] v, float[] n, float[] u)
    {
        return v;
    }

    public static void addUniforms(List<CachedUniform> list, Map<String, ShaderCurves.ShaderVariable> variableMap)
    {
    }

    public static List<String> getSliderProperties()
    {
        return Collections.emptyList();
    }

    public static Map<String, String> getShadersLanguageMap(String language)
    {
        return Collections.emptyMap();
    }
}
