package org.Enderfan.vivarium.client.handlers;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.Enderfan.vivarium.item.ModItems;

@Mod.EventBusSubscriber(modid = "vivarium", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientPoseHandler
{
    @SubscribeEvent
    public static void onPlayerRender(RenderPlayerEvent.Pre event)
    {
        Player player = event.getEntity();

        // check if they are holding right click with the sword
        if (player.isUsingItem() && player.getUseItem().getItem() == ModItems.BLADE.get())
        {
            PlayerModel<?> model = event.getRenderer().getModel();

            // this forces the arm into their own chest for third person view.
            // you might have to tweak the math if it looks too janky or misses their body.
            if (player.getUsedItemHand() == InteractionHand.MAIN_HAND)
            {
                model.rightArm.xRot = (float) Math.toRadians(-90.0);
                model.rightArm.yRot = (float) Math.toRadians(45.0);
                model.rightArm.zRot = 0.0f;
            }
            else
            {
                // do the left arm too if they put it in their offhand because players are weird
                model.leftArm.xRot = (float) Math.toRadians(-90.0);
                model.leftArm.yRot = (float) Math.toRadians(-45.0);
                model.leftArm.zRot = 0.0f;
            }
        }
    }
}
