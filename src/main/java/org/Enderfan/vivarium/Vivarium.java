package org.Enderfan.vivarium;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.Enderfan.vivarium.block.ModBlocks;
import org.Enderfan.vivarium.client.*;
import org.Enderfan.vivarium.config.VivariumConfig;
import org.Enderfan.vivarium.entities.ModEntities;
import org.Enderfan.vivarium.events.*;
import org.Enderfan.vivarium.item.ModItems;
import org.Enderfan.vivarium.particles.ModParticles;
import org.Enderfan.vivarium.server.GuiltProvider;
import org.Enderfan.vivarium.server.GuiltSyncPacket;
import org.Enderfan.vivarium.server.ModMessages;
import org.Enderfan.vivarium.server.PlayerGuilt;
import org.Enderfan.vivarium.world.ModStructures;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Vivarium.MODID)
public class Vivarium
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "vivarium";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public Vivarium()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // === NETWORKING ===
        ModMessages.register();

        // === REGISTRIES ===
        ModEntities.ENTITIES.register(modEventBus);
        ModSounds.SOUNDS.register(modEventBus);
        ModParticles.PARTICLES.register(modEventBus);
        ModStructures.register(modEventBus);
        ModItems.MOD_ITEMS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);

        // === CLIENT SETUP ===
        modEventBus.addListener(ClientSetup::onClientSetup);
        modEventBus.addListener(ClientSetup::registerParticleFactories);
        modEventBus.addListener(this::registerCaps);

        // === EVENT HANDLERS ===
        MinecraftForge.EVENT_BUS.register(BloodPoolEffectHandler.class);
        MinecraftForge.EVENT_BUS.register(Events.class);
        MinecraftForge.EVENT_BUS.register(DreamEvent.class);
        MinecraftForge.EVENT_BUS.register(TreeBleedEvent.class);
        MinecraftForge.EVENT_BUS.register(CaveInEvent.class);
        MinecraftForge.EVENT_BUS.register(StormEvent.class);
        MinecraftForge.EVENT_BUS.register(ClientMusicHandler.class);
        MinecraftForge.EVENT_BUS.register(VolcanoEvent.class);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(HallucinationHandler.class);
        MinecraftForge.EVENT_BUS.register(ApocalypseEvents.class);
        MinecraftForge.EVENT_BUS.register(ClientScheduler.class);

        // === COMMON SETUP ===
        modEventBus.addListener(this::commonSetup);
        registerPackets();

        // === CONFIG ===
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, VivariumConfig.SPEC);
        ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (minecraft, parentScreen) -> new org.Enderfan.vivarium.client.gui.VivariumConfigScreen(parentScreen)
                )
        );
    }

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MODID, "main"),
            () -> "1.0",
            s -> true,
            s -> true
    );

    // Call this in your Mod Constructor or CommonSetup
    public static void registerPackets()
    {
        int id = 0;
        INSTANCE.registerMessage(id++, GuiltSyncPacket.class,
                GuiltSyncPacket::toBytes,
                GuiltSyncPacket::new,
                GuiltSyncPacket::handle);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {

    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    @SubscribeEvent
    public void registerCaps(RegisterCapabilitiesEvent event)
    {
        event.register(PlayerGuilt.class);
    }


    //The following is for the global 'guilt' counter
    public static final Capability<PlayerGuilt> PLAYER_GUILT = CapabilityManager.get(new CapabilityToken<>() {});

    @SubscribeEvent
    public static void onAttachCapabilitiesPlayer(AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getObject() instanceof Player)
        {
            if (!event.getObject().getCapability(PLAYER_GUILT).isPresent())
            {
                event.addCapability(new ResourceLocation(Vivarium.MODID, "guilt"), new GuiltProvider() {
                    @Override
                    public CompoundTag serializeNBT() {
                        return null;
                    }

                    @Override
                    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
                        return null;
                    }
                });
            }
        }
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // Some client setup code
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }
}

