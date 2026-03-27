package org.Enderfan.vivarium.entities;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.Enderfan.vivarium.events.Events;
import org.Enderfan.vivarium.events.TreeBleedEvent;
import org.Enderfan.vivarium.item.ModItems;
import org.Enderfan.vivarium.server.GuiltProvider;
import org.Enderfan.vivarium.server.WorldHeartState;

public class WorldHeartEntity extends Mob
{
    public WorldHeartEntity(EntityType<? extends Mob> type, Level level)
    {
        super(type, level);
        this.setNoGravity(true);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer)
    {
        // false means "never despawn, I don't care how far away the player is"
        return false;
    }

    public static AttributeSupplier.Builder createAttributes()
    {
        // it needs max health or the game crashes when it spawns
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 1000.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void registerGoals()
    {
        // leave this empty so it literally never moves
    }

    @Override
    public boolean isPushable()
    {
        return false; // so players can't just shove it into a wall
    }

    @Override
    public void tick()
    {
        super.tick();

        // tell it it has no gravity every single tick just to be safe
        this.setNoGravity(true);

        // completely erase any velocity it tries to have
        this.setDeltaMovement(0, 0, 0);

    }

    @Override
    public AABB getBoundingBoxForCulling()
    {
        // grab the physical, pulsing hitbox we just fixed
        AABB normalBox = super.getBoundingBoxForCulling();

        // inflate it by 10 blocks in every direction just for the camera check.
        // this does absolutely nothing to the physical collision, players can still walk right up to the heart.
        return normalBox.inflate(10.0D);
    }

    @Override
    public boolean hurt(DamageSource source, float amount)
    {
        // DEBUG: If you hit it, this WILL print in your IntelliJ console
        System.out.println("[WORLD HEART] Hurt method triggered by: " + source.getMsgId());

        Entity attacker = source.getEntity();

        if (attacker instanceof Player player)
        {
            System.out.println("[WORLD HEART] Attacker is Player: " + player.getScoreboardName());

            if (player.getMainHandItem().getItem() == ModItems.BLADE.get())
            {
                System.out.println("[WORLD HEART] Player is holding the Blade! Executing kill.");

                if (!this.level().isClientSide())
                {
                    TreeBleedEvent.treeBleed(this.level(), this.blockPosition().above(), false);

                    // Bypass super.hurt() and invulnerability tags entirely.
                    // We just force its health to 0 and tell it to run the death sequence.
                    this.setHealth(0.0f);
                    this.die(source);
                }

                // Return true so the client plays the hit sound and red flash
                return true;
            }
            else
            {
                System.out.println("[WORLD HEART] Player punched it. Rejecting.");

                if (!this.level().isClientSide())
                {
                    player.displayClientMessage(Component.literal("It has no effect"), true);
                }
                return false;
            }
        }

        return false;
    }

    @Override
    public void die(DamageSource cause)
    {
        super.die(cause);

        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel)
        {
            // Flip the permanent doomsday switch
            WorldHeartState state = WorldHeartState.get(serverLevel);
            state.killWorld();

            Entity killer = cause.getEntity();
            String killerName = "an unknown force";

            // SAFELY check if a player actually killed it to avoid NullPointerExceptions
            if (killer instanceof Player player)
            {
                killerName = player.getScoreboardName();

                player.getCapability(GuiltProvider.PLAYER_GUILT).ifPresent(guilt ->
                        guilt.setGuilt(Integer.MIN_VALUE));
            }

            serverLevel.getServer().getPlayerList().broadcastSystemMessage(
                    Component.literal("The World has been slain by " + killerName).withStyle(ChatFormatting.WHITE),
                    false );

            serverLevel.getServer().getPlayerList().broadcastSystemMessage(
                    Component.literal("Your gamemode has been updated to Hardcore").withStyle(ChatFormatting.WHITE),
                    false );

            serverLevel.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, serverLevel.getServer());
        }
    }
}
