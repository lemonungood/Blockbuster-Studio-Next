package mchorse.bbs_mod.mixin.client;

import mchorse.bbs_mod.client.renderer.MorphRenderer;
import mchorse.bbs_mod.forms.FormUtilsClient;
import mchorse.bbs_mod.forms.forms.Form;
import mchorse.bbs_mod.forms.renderers.FormRenderer;
import mchorse.bbs_mod.morphing.Morph;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;

/**
 * PlayerRendererMixin - disabled in MC 26.2 because PlayerRenderer was removed.
 * TODO: Re-implement using new 26.2 player rendering hooks when available.
 */
// @Mixin target was net.minecraft.client.renderer.entity.player.PlayerRenderer (removed in 26.2)
public class PlayerRendererMixin
{
    /* Inject method = "render" - disabled
    public void onRender(...) { ... }
    */

    /* Inject method = "getPositionOffset" - disabled
    public void onPositionOffset(...) { ... }
    */

    /* Inject method = "renderArm" - disabled
    public void onRenderArmBegin(...) { ... }
    */
}
