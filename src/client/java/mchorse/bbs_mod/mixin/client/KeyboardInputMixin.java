package mchorse.bbs_mod.mixin.client;

import mchorse.bbs_mod.graphics.window.Window;
import mchorse.bbs_mod.ui.dashboard.UIDashboard;
import mchorse.bbs_mod.ui.film.UIFilmPanel;
import mchorse.bbs_mod.ui.framework.UIBaseMenu;
import mchorse.bbs_mod.ui.framework.UIScreen;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public class KeyboardInputMixin
{
    @Shadow
    protected Vec2 moveVector;
    private static float getMovementMultiplier(boolean positive, boolean negative)
    {
        return positive == negative ? 0F : (positive ? 1F : -1F);
    }

    @Inject(method = "tick", at = @At("RETURN"))
    public void onTick(boolean slowDown, float slowDownFactor, CallbackInfo info)
    {
        UIBaseMenu menu = UIScreen.getCurrentMenu();

        if (
            menu instanceof UIDashboard dashboard &&
            dashboard.getPanels().panel instanceof UIFilmPanel filmPanel &&
            filmPanel.getController().isControlling()
        ) {
            KeyboardInput input = (KeyboardInput) (Object) this;

            boolean forward = Window.isKeyPressed(GLFW.GLFW_KEY_W);
            boolean backward = Window.isKeyPressed(GLFW.GLFW_KEY_S);
            boolean left = Window.isKeyPressed(GLFW.GLFW_KEY_A);
            boolean right = Window.isKeyPressed(GLFW.GLFW_KEY_D);
            boolean jump = Window.isKeyPressed(GLFW.GLFW_KEY_SPACE);
            boolean shift = Window.isKeyPressed(GLFW.GLFW_KEY_LEFT_SHIFT);

            input.keyPresses = new Input(forward, backward, left, right, jump, shift, false);

            float forwardImpulse = getMovementMultiplier(forward, backward);
            float leftImpulse = getMovementMultiplier(left, right);
            this.moveVector = new Vec2(leftImpulse, forwardImpulse);

            if (slowDown)
            {
                this.moveVector = new Vec2(this.moveVector.x * slowDownFactor, this.moveVector.y * slowDownFactor);
            }
        }
    }
}
