package org.Enderfan.vivarium.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.Enderfan.vivarium.Vivarium;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = Vivarium.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientScheduler {
    private static final List<TickTask> tasks = new ArrayList<>();

    // Call this to schedule code: ClientScheduler.schedule(60, () -> { System.out.println("Done!"); });
    public static void schedule(int delayInTicks, Runnable action) {
        tasks.add(new TickTask(delayInTicks, action));
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            tasks.removeIf(TickTask::tick); // Update all tasks and remove finished ones
        }
    }

    private static class TickTask {
        private int delay;
        private final Runnable action;

        public TickTask(int delay, Runnable action) {
            this.delay = delay;
            this.action = action;
        }

        public boolean tick() {
            delay--;
            if (delay <= 0) {
                action.run(); // Run the code
                return true;  // Remove from list
            }
            return false; // Keep in list
        }
    }
}