package org.Enderfan.vivarium.item;


import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import org.Enderfan.vivarium.config.VivariumConfig;
import org.Enderfan.vivarium.events.ApocalypseEvents;
import org.Enderfan.vivarium.events.TreeBleedEvent;

import javax.annotation.Nullable;
import java.util.List;

public class BladeItem extends Item
{
    public BladeItem(Properties properties)
    {
        super(properties);
    }

    // this tells the game how long they can hold right click. 72000 is the vanilla bow number.
    @Override
    public int getUseDuration(ItemStack stack)
    {
        return 72000;
    }

    // makes them hold it up to their face like a bow or a trident
    @Override
    public UseAnim getUseAnimation(ItemStack stack)
    {
        return UseAnim.BOW;
    }

    // triggers when they start holding right click
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand)
    {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    // triggers when they let go of right click
    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entityLiving, int timeLeft)
    {
        if (!level.isClientSide && entityLiving instanceof ServerPlayer player)
        {
            // calculate how long they held it. 20 ticks is 1 second.
            int charge = this.getUseDuration(stack) - timeLeft;

            if (charge >= 20)
            {
                // the "hardcore" ending
                // rip bozo
                player.getInventory().dropAll();
                player.kill();
                // flag the player so the server knows to start draining guilt
                player.getPersistentData().putBoolean("vivarium_cleansing_guilt", true);
                player.setGameMode(GameType.SPECTATOR);
                TreeBleedEvent.treeBleed(player.level(), player.getOnPos().above(), false);
                if(VivariumConfig.DO_CREDITS.get()) ApocalypseEvents.credits(player);
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag)
    {
        tooltip.add(Component.literal("Click on a block to guide you").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Right click to use on yourself").withStyle(ChatFormatting.GRAY));
    }
}
