package org.Enderfan.vivarium.mixins;

import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.components.SplashRenderer;
import org.Enderfan.vivarium.client.MetaStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// TWEAK
@Mixin(TitleScreen.class)
public class MixinTitleScreen
{
    // Grab the internal splash renderer from the vanilla TitleScreen class
    @Shadow private SplashRenderer splash;

    @Inject(method = "init", at = @At("RETURN"))
    private void onInit(CallbackInfo ci)
    {
        // Check the local file to see if the player left a high-guilt world
        if (MetaStateManager.getLastGuilt() >= 1000)
        {
            // Forcibly overwrite the yellow text
            this.splash = new SplashRenderer("Why do you hurt me when I give you everything?");
        }
        else if (MetaStateManager.getLastGuilt() < 0) //for if they killed the world heart
        {
            this.splash = new SplashRenderer("...");
        }
    }
}