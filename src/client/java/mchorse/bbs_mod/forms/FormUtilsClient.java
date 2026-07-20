package mchorse.bbs_mod.forms;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import mchorse.bbs_mod.forms.forms.AnchorForm;
import mchorse.bbs_mod.forms.forms.BillboardForm;
import mchorse.bbs_mod.forms.forms.BlockForm;
import mchorse.bbs_mod.forms.forms.ExtrudedForm;
import mchorse.bbs_mod.forms.forms.Form;
import mchorse.bbs_mod.forms.forms.FramebufferForm;
import mchorse.bbs_mod.forms.forms.ItemForm;
import mchorse.bbs_mod.forms.forms.LabelForm;
import mchorse.bbs_mod.forms.forms.MobForm;
import mchorse.bbs_mod.forms.forms.ModelForm;
import mchorse.bbs_mod.forms.forms.ParticleForm;
import mchorse.bbs_mod.forms.forms.TrailForm;
import mchorse.bbs_mod.forms.forms.VanillaParticleForm;
import mchorse.bbs_mod.forms.renderers.AnchorFormRenderer;
import mchorse.bbs_mod.forms.renderers.BillboardFormRenderer;
import mchorse.bbs_mod.forms.renderers.BlockFormRenderer;
import mchorse.bbs_mod.forms.renderers.ExtrudedFormRenderer;
import mchorse.bbs_mod.forms.renderers.FormRenderer;
import mchorse.bbs_mod.forms.renderers.FormRenderingContext;
import mchorse.bbs_mod.forms.renderers.FramebufferFormRenderer;
import mchorse.bbs_mod.forms.renderers.ItemFormRenderer;
import mchorse.bbs_mod.forms.renderers.LabelFormRenderer;
import mchorse.bbs_mod.forms.renderers.MobFormRenderer;
import mchorse.bbs_mod.forms.renderers.ModelFormRenderer;
import mchorse.bbs_mod.forms.renderers.ParticleFormRenderer;
import mchorse.bbs_mod.forms.renderers.TrailFormRenderer;
import mchorse.bbs_mod.forms.renderers.VanillaParticleFormRenderer;
import mchorse.bbs_mod.ui.framework.UIContext;
import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.renderer.rendertype.RenderTypes;
// [MC 26.2 REMOVED] import net.minecraft.client.renderer.Sheets;
// [MC 26.2 REMOVED] import net.minecraft.client.render.chunk.SectionBufferBuilderPool;
import net.minecraft.client.resources.model.ModelBakery;
// [MC26.2] import net.minecraft.Util; -> use java.util.Objects or keep

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.Stack;

public class FormUtilsClient
{
    private static Map<Class, IFormRendererFactory> map = new HashMap<>();
    private static CustomVertexConsumer customVertexConsumer;
    private static Stack<Form> currentForm = new Stack<>();

    static
    {
        SectionBufferBuilderPool storage = new SectionBufferBuilderPool();
        SortedMap sortedMap = Util.make(new Object2ObjectLinkedOpenHashMap(), map -> {
            map.put(TexturedRenderTypess.getEntitySolid(), storage.get(RenderTypes.getSolid()));
            map.put(TexturedRenderTypess.getEntityCutout(), storage.get(RenderTypes.getCutout()));
            map.put(TexturedRenderTypess.getBannerPatterns(), storage.get(RenderTypes.getCutoutMipped()));
            map.put(TexturedRenderTypess.getEntityTranslucentCull(), storage.get(RenderTypes.getTranslucent()));
            assignBufferBuilder(map, TexturedRenderTypess.getShieldPatterns());
            assignBufferBuilder(map, TexturedRenderTypess.getBeds());
            assignBufferBuilder(map, TexturedRenderTypess.getShulkerBoxes());
            assignBufferBuilder(map, TexturedRenderTypess.getSign());
            assignBufferBuilder(map, TexturedRenderTypess.getHangingSign());
            map.put(TexturedRenderTypess.getChest(), new BufferBuilder(786432));
            assignBufferBuilder(map, RenderTypes.getArmorGlint());
            assignBufferBuilder(map, RenderTypes.getArmorEntityGlint());
            assignBufferBuilder(map, RenderTypes.getGlint());
            assignBufferBuilder(map, RenderTypes.getDirectGlint());
            assignBufferBuilder(map, RenderTypes.getGlintTranslucent());
            assignBufferBuilder(map, RenderTypes.getEntityGlint());
            assignBufferBuilder(map, RenderTypes.getDirectEntityGlint());
            assignBufferBuilder(map, RenderTypes.getWaterMask());
            ModelBakery.BLOCK_DESTRUCTION_RENDER_LAYERS.forEach(renderLayer -> assignBufferBuilder(map, renderLayer));
        });

        customVertexConsumer = new CustomVertexConsumer(new BufferBuilder(1536), sortedMap);

        register(BillboardForm.class, BillboardFormRenderer::new);
        register(ExtrudedForm.class, ExtrudedFormRenderer::new);
        register(LabelForm.class, LabelFormRenderer::new);
        register(ModelForm.class, ModelFormRenderer::new);
        register(ParticleForm.class, ParticleFormRenderer::new);
        register(BlockForm.class, BlockFormRenderer::new);
        register(ItemForm.class, ItemFormRenderer::new);
        register(AnchorForm.class, AnchorFormRenderer::new);
        register(MobForm.class, MobFormRenderer::new);
        register(VanillaParticleForm.class, VanillaParticleFormRenderer::new);
        register(TrailForm.class, TrailFormRenderer::new);
        register(FramebufferForm.class, FramebufferFormRenderer::new);
    }

    private static void assignBufferBuilder(Object2ObjectLinkedOpenHashMap<RenderTypes, BufferBuilder> builderStorage, RenderTypes layer)
    {
        builderStorage.put(layer, new BufferBuilder(layer.getExpectedBufferSize()));
    }

    public static CustomVertexConsumer getProvider()
    {
        return customVertexConsumer;
    }

    public static <T extends Form> void register(Class<T> clazz, IFormRendererFactory<T> function)
    {
        map.put(clazz, function);
    }

    public static Form getCurrentForm()
    {
        return currentForm.isEmpty() ? null : currentForm.peek();
    }

    public static FormRenderer getRenderer(Form form)
    {
        if (form == null)
        {
            return null;
        }

        if (form.getRenderer() instanceof FormRenderer renderer)
        {
            return renderer;
        }

        IFormRendererFactory factory = map.get(form.getClass());

        if (factory != null)
        {
            FormRenderer formRenderer = factory.create(form);

            form.setRenderer(formRenderer);

            return formRenderer;
        }

        return null;
    }

    public static void renderUI(Form form, UIContext context, int x1, int y1, int x2, int y2)
    {
        FormRenderer renderer = getRenderer(form);

        if (renderer != null)
        {
            renderer.renderUI(context, x1, y1, x2, y2);
        }
    }

    public static void render(Form form, FormRenderingContext context)
    {
        FormRenderer renderer = getRenderer(form);

        if (renderer != null)
        {
            currentForm.push(form);

            try
            {
                renderer.render(context);
            }
            catch (Exception e)
            {}

            currentForm.pop();
        }
    }

    public static List<String> getBones(Form form)
    {
        FormRenderer renderer = getRenderer(form);

        if (renderer != null)
        {
            return renderer.getBones();
        }

        return Collections.emptyList();
    }

    public static interface IFormRendererFactory <T extends Form>
    {
        public FormRenderer<T> create(T form);
    }
}


