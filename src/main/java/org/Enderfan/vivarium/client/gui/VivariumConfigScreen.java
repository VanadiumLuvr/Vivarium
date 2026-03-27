package org.Enderfan.vivarium.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.ForgeConfigSpec;
import org.Enderfan.vivarium.config.VivariumConfig;

import java.util.ArrayList;
import java.util.List;

public class VivariumConfigScreen extends Screen
{
    private final Screen parent;
    private double scrollOffset = 0;
    private int totalContentHeight = 0;

    // We store these so we can render them outside the scissor mask on Page 1
    private Button saveButton;
    private Button backButton;
    private Button advancedButton;

    // 0 = Basic Pace Page, 1 = Advanced Spoilers Page
    private int currentPage = 0;

    private final List<ConfigEntry> configWidgets = new ArrayList<>();

    public VivariumConfigScreen(Screen parent)
    {
        super(Component.literal("Vivarium Configuration"));
        this.parent = parent;
    }

    @Override
    protected void init()
    {
        // Wipe the screen clean before building the current page
        this.clearWidgets();
        this.configWidgets.clear();

        if (this.currentPage == 0)
        {
            this.buildBasicPage();
        }
        else
        {
            this.buildAdvancedPage();
        }
    }

    private void buildBasicPage()
    {
        // Put the pace input right below the warning text
        int startY = this.height / 2 + 10;

        this.addDoubleInput("Mod Pace", "Overarching multiplier that will increase/decrease the speed of happenings", VivariumConfig.PACE, startY, 24);

        this.advancedButton = Button.builder(Component.literal("Advanced Settings (Spoilers)"), button ->
        {
            this.saveAll(); // Save the pace before destroying the page
            this.currentPage = 1;
            this.scrollOffset = 0;
            this.rebuildWidgets();
        }).bounds(this.width / 2 - 100, this.height - 60, 200, 20).build();

        this.saveButton = Button.builder(Component.literal("Save & Close"), button ->
        {
            this.saveAll();
            this.minecraft.setScreen(this.parent);
        }).bounds(this.width / 2 - 100, this.height - 30, 200, 20).build();

        this.addRenderableWidget(this.advancedButton);
        this.addRenderableWidget(this.saveButton);
    }

    private void buildAdvancedPage()
    {
        int startY = 30;
        int spacing = 24;

        // --- Guilt Thresholds ---
        startY = this.addIntInput("Water Drip Threshold", "Guilt level required for dripping water to turn to blood.", VivariumConfig.WATER_DRIP_THRESHOLD, startY, spacing);
        startY = this.addIntInput("Fog Color Threshold", "Guilt level required for fog to begin turning red", VivariumConfig.FOG_COLOR_THRESHOLD, startY, spacing);
        startY = this.addIntInput("Fog Thickness Min", "Guilt level required for fog to begin thickening", VivariumConfig.FOG_THICKNESS_MIN, startY, spacing);
        startY = this.addIntInput("Fog Thickness Max", "Guilt level required for fog to finish thickening", VivariumConfig.FOG_THICKNESS_MAX, startY, spacing);
        startY = this.addIntInput("Hostile Wildlife Threshold", "Guilt level required for neutral mobs to become permanently hostile.", VivariumConfig.HOSTILE_WILDLIFE_THRESHOLD, startY, spacing);
        startY = this.addIntInput("Music Pitch Min", "Guilt level required for the game's music to start pitching down.", VivariumConfig.MUSIC_PITCH_MIN, startY, spacing);
        startY = this.addIntInput("Music Pitch Max", "Guilt level required for the game's music to stop pitching down.", VivariumConfig.MUSIC_PITCH_MAX, startY, spacing);
        startY = this.addDoubleInput("Music Pitch Factor", "Pitch down of music at [Music Pitch Max], i.e. 0.5 = 50% pitch at [Music Pitch Max]", VivariumConfig.MUSIC_PITCH_FACTOR, startY, spacing);
        startY = this.addIntInput("Log Threshold", "Requisite number of logs to be broken to trigger the tree bleed event", VivariumConfig.LOG_THRESHOLD, startY, spacing);
        startY = this.addIntInput("Stone Threshold", "Requisite number of stone/deepslate to be broken to trigger the cave in event", VivariumConfig.STONE_THRESHOLD, startY, spacing);
        startY = this.addIntInput("Animal Bleed Threshold", "Guilt level required for animals to have a chance to bleed when killed", VivariumConfig.ANIMAL_BLEED_THRESHOLD, startY, spacing);
        startY = this.addIntInput("Heartbeat Threshold", "Guilt level required for the heartbeat sound when underground", VivariumConfig.HEARTBEAT_THRESHOLD, startY, spacing);
        startY = this.addIntInput("Cry Threshold", "Guilt level required for the crying sound", VivariumConfig.CRY_THRESHOLD, startY, spacing);
        startY = this.addDoubleInput("Cry Chance", "Likelihood for the crying sound every second while guilt >= [Cry Threshold]", VivariumConfig.CRY_CHANCE, startY, spacing);
        startY = this.addIntInput("Flower Close Threshold", "Guilt level required for Vitaflowers to close when in proximity", VivariumConfig.FLOWER_CLOSE_THRESHOLD, startY, spacing);
        startY = this.addIntInput("Flower Wilt Threshold", "Guilt level required for Vitaflowers to wilt when in proximity", VivariumConfig.FLOWER_WILT_THRESHOLD, startY, spacing);
        startY = this.addIntInput("Animal Flee Threshold", "Guilt level required for passive mobs to run away from you", VivariumConfig.ANIMAL_FLEE_THRESHOLD, startY, spacing);
        startY = this.addIntInput("Monster Spawn Min", "Guilt level when monster spawn rate starts increasing", VivariumConfig.MONSTER_SPAWN_MIN, startY, spacing);
        startY = this.addIntInput("Monster Spawn Max", "Guilt level when monster spawn rate stops increasing", VivariumConfig.MONSTER_SPAWN_MAX, startY, spacing);
        startY = this.addDoubleInput("Monster Spawn Factor", "How much monster spawns will be increased at [Monster Spawn Max] guilt", VivariumConfig.MONSTER_SPAWN_FACTOR, startY, spacing);
        startY = this.addIntInput("Dream Threshold", "Guilt level when the dream event will occur", VivariumConfig.DREAM_THRESHOLD, startY, spacing);
        startY = this.addIntInput("Storm Threshold", "Guilt level when the storm will occur", VivariumConfig.STORM_THRESHOLD, startY, spacing);
        startY = this.addIntInput("Volcano Threshold", "Guilt level when the volcano eruption will occur", VivariumConfig.VOLCANO_THRESHOLD, startY, spacing);
        startY = this.addIntInput("Carving Threshold", "Guilt level when carvings will begin to appear", VivariumConfig.CARVING_THRESHOLD, startY, spacing);
        startY = this.addDoubleInput("Carving Chance", "Chance of carvings appearing (1 = 100%)", VivariumConfig.CARVING_CHANCE, startY, spacing);
        startY = this.addIntInput("Blood Rain Threshold", "Guilt level when rain will turn red", VivariumConfig.BLOOD_RAIN_THRESHOLD, startY, spacing);
        startY = this.addIntInput("Alt Sun Threshold", "Guilt level when the sun will change", VivariumConfig.ALT_SUN_THRESHOLD, startY, spacing);

        // --- Guilt Settings (Increments) ---
        startY = this.addIntInput("Guilt Increment: Grass", "How much to increment guilt on breaking a grass block", VivariumConfig.GUILT_INC_GRASS, startY, spacing);
        startY = this.addIntInput("Guilt Increment: Leaves", "How much to increment guilt upon breaking leaves", VivariumConfig.GUILT_INC_LEAVES, startY, spacing);
        startY = this.addIntInput("Guilt Increment: Log", "How much to increment guilt on breaking a log", VivariumConfig.GUILT_INC_LOG, startY, spacing);
        startY = this.addIntInput("Guilt Increment: Stone", "How much to increment guilt on breaking a stone or deepslate block", VivariumConfig.GUILT_INC_STONE, startY, spacing);
        startY = this.addIntInput("Guilt Increment: Misc", "How much to increment guilt on breaking blocks that can be held by Endermen", VivariumConfig.GUILT_INC_MISC, startY, spacing);
        startY = this.addIntInput("Guilt Increment: Kill", "How much to increment guilt on killing a passive mob", VivariumConfig.GUILT_INC_KILL, startY, spacing);

        // --- Game Rules ---
        startY = this.addBoolInput("Cave-In Teleport", "If the player will be teleported during the cave in event", VivariumConfig.CAVE_IN_TELEPORT, startY, spacing);
        startY = this.addBoolInput("Do Credits", "If credits will roll upon player's true death", VivariumConfig.DO_CREDITS, startY, spacing);

        // --- Ecosystem Settings ---
        startY = this.addIntInput("Butterfly Starvation Time", "Time in ticks (20 ticks = 1 second) before a butterfly begins to starve without a Vitaflower.", VivariumConfig.BUTTERFLY_STARVATION_TIME, startY, spacing);


        this.totalContentHeight = startY + 20;

        // Split the bottom row into two buttons
        this.backButton = Button.builder(Component.literal("Back to Basic"), button ->
        {
            this.saveAll(); // Save all the advanced fields before destroying the page
            this.currentPage = 0;
            this.scrollOffset = 0;
            this.rebuildWidgets();
        }).bounds(this.width / 2 - 105, this.height - 30, 100, 20).build();

        this.saveButton = Button.builder(Component.literal("Save & Close"), button ->
        {
            this.saveAll();
            this.minecraft.setScreen(this.parent);
        }).bounds(this.width / 2 + 5, this.height - 30, 100, 20).build();

        this.addRenderableWidget(this.backButton);
        this.addRenderableWidget(this.saveButton);

        this.updateWidgetPositions();
    }

// --- HELPER METHODS ---

    private int addIntInput(String label, String tooltip, ForgeConfigSpec.IntValue config, int y, int spacing)
    {
        EditBox editBox = new EditBox(this.font, this.width / 2 + 10, y, 100, 20, Component.literal(label));
        editBox.setValue(String.valueOf(config.get()));
        this.addRenderableWidget(editBox);
        this.configWidgets.add(new ConfigEntry(label, tooltip, editBox, config, y));
        return y + spacing;
    }

    private int addDoubleInput(String label, String tooltip, ForgeConfigSpec.DoubleValue config, int y, int spacing)
    {
        EditBox editBox = new EditBox(this.font, this.width / 2 + 10, y, 100, 20, Component.literal(label));
        editBox.setValue(String.valueOf(config.get()));
        this.addRenderableWidget(editBox);
        this.configWidgets.add(new ConfigEntry(label, tooltip, editBox, config, y));
        return y + spacing;
    }

    private int addBoolInput(String label, String tooltip, ForgeConfigSpec.BooleanValue config, int y, int spacing)
    {
        Button btn = Button.builder(Component.literal(config.get() ? "True" : "False"), button ->
        {
            boolean newValue = !button.getMessage().getString().equals("True");
            button.setMessage(Component.literal(newValue ? "True" : "False"));
        }).bounds(this.width / 2 + 10, y, 100, 20).build();

        this.addRenderableWidget(btn);
        this.configWidgets.add(new ConfigEntry(label, tooltip, btn, config, y));
        return y + spacing;
    }

    // --- LOGIC & RENDERING ---

    private void saveAll()
    {
        for (ConfigEntry entry : this.configWidgets)
        {
            try
            {
                if (entry.widget instanceof EditBox editBox)
                {
                    if (entry.configValue instanceof ForgeConfigSpec.IntValue intValue)
                    {
                        intValue.set(Integer.parseInt(editBox.getValue()));
                    }
                    else if (entry.configValue instanceof ForgeConfigSpec.DoubleValue doubleValue)
                    {
                        doubleValue.set(Double.parseDouble(editBox.getValue()));
                    }
                }
                else if (entry.widget instanceof Button button && entry.configValue instanceof ForgeConfigSpec.BooleanValue boolValue)
                {
                    boolValue.set(button.getMessage().getString().equals("True"));
                }
            }
            catch (NumberFormatException e)
            {
                // If the player typed "potato" into a number field, we just ignore it and don't save.
            }
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta)
    {
        // Delta is 1.0 (scroll up) or -1.0 (scroll down)
        this.scrollOffset -= delta * 20;

        // Clamp the scroll so they can't scroll into the void
        int maxScroll = Math.max(0, this.totalContentHeight - this.height + 60);
        this.scrollOffset = Math.max(0, Math.min(this.scrollOffset, maxScroll));

        this.updateWidgetPositions();
        return true;
    }

    private void updateWidgetPositions()
    {
        for (ConfigEntry entry : this.configWidgets)
        {
            // Physically drag the hitboxes up and down the screen
            entry.widget.setY(entry.originalY - (int) this.scrollOffset);
        }
    }


    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        this.renderBackground(graphics);

        if (this.currentPage == 0)
        {
            // --- PAGE 0: BASIC RENDERING ---
            graphics.drawCenteredString(this.font, this.title, this.width / 2, 8, 0xFFFFFF);

            // Draw the context and warnings
            graphics.drawCenteredString(this.font, "Mod Pace acts as a global multiplier for all events.", this.width / 2, 50, 0xFFFFFF);
            graphics.drawCenteredString(this.font, "Increase it to make events happen faster, decrease to slow them down.", this.width / 2, 65, 0xAAAAAA);

            graphics.drawCenteredString(this.font, "WARNING: The next page contains major spoilers for the mod's progression.", this.width / 2, 100, 0xFF5555); // Red text
            graphics.drawCenteredString(this.font, "It is highly recommended not to change Pace and specific thresholds at the same time.", this.width / 2, 115, 0xFF5555);

            for (ConfigEntry entry : this.configWidgets)
            {
                graphics.drawString(this.font, entry.label, this.width / 2 - 140, entry.originalY + 6, 0xFFFFFF, false);
            }

            // Draw the buttons and text boxes normally, no scissors needed
            super.render(graphics, mouseX, mouseY, partialTick);
        }
        else
        {
            // --- PAGE 1: ADVANCED SCROLLING RENDERING ---
            graphics.enableScissor(0, 25, this.width, this.height - 40);

            for (ConfigEntry entry : this.configWidgets)
            {
                int drawY = entry.originalY - (int) this.scrollOffset;
                graphics.drawString(this.font, entry.label, this.width / 2 - 140, drawY + 6, 0xFFFFFF, false);
            }

            super.render(graphics, mouseX, mouseY, partialTick);
            graphics.disableScissor();

            // Draw the solid black borders
            graphics.fill(0, 0, this.width, 25, 0xFF000000);
            graphics.drawCenteredString(this.font, "Advanced Configuration", this.width / 2, 8, 0xFFFFFF);
            graphics.fill(0, this.height - 40, this.width, this.height, 0xFF000000);

            // Re-render the bottom buttons so they don't get chopped off by the scissor
            this.backButton.render(graphics, mouseX, mouseY, partialTick);
            this.saveButton.render(graphics, mouseX, mouseY, partialTick);
        }

        // --- DRAW TOOLTIPS ON HOVER ---
        for (ConfigEntry entry : this.configWidgets)
        {
            int drawY = entry.originalY - (int) this.scrollOffset;

            // Only trigger tooltips if the row is visible
            if (drawY > 25 && drawY < this.height - 40)
            {
                if (mouseY >= drawY && mouseY <= drawY + 20 && mouseX > this.width / 2 - 150 && mouseX < this.width / 2 + 120)
                {
                    graphics.renderTooltip(this.font, Component.literal(entry.tooltip), mouseX, mouseY);
                }
            }
        }
    }

    // --- DATA CLASS ---
    private static class ConfigEntry
    {
        String label;
        String tooltip;
        AbstractWidget widget;
        ForgeConfigSpec.ConfigValue<?> configValue;
        int originalY;

        public ConfigEntry(String label, String tooltip, AbstractWidget widget, ForgeConfigSpec.ConfigValue<?> configValue, int originalY)
        {
            this.label = label;
            this.tooltip = tooltip;
            this.widget = widget;
            this.configValue = configValue;
            this.originalY = originalY;
        }
    }
}