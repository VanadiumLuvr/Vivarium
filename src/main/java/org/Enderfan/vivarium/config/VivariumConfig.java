package org.Enderfan.vivarium.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class VivariumConfig
{
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    // Define all our threshold variables
    public static final ForgeConfigSpec.IntValue WATER_DRIP_THRESHOLD;
    public static final ForgeConfigSpec.DoubleValue PACE;
    public static final ForgeConfigSpec.IntValue FOG_COLOR_MIN;
    public static final ForgeConfigSpec.IntValue FOG_COLOR_MAX;
    public static final ForgeConfigSpec.IntValue FOG_THICKNESS_MIN;
    public static final ForgeConfigSpec.IntValue FOG_THICKNESS_MAX;
    public static final ForgeConfigSpec.IntValue HOSTILE_WILDLIFE_THRESHOLD;
    public static final ForgeConfigSpec.IntValue MUSIC_PITCH_MIN;
    public static final ForgeConfigSpec.IntValue MUSIC_PITCH_MAX;
    public static final ForgeConfigSpec.DoubleValue MUSIC_PITCH_FACTOR;
    public static final ForgeConfigSpec.IntValue BUTTERFLY_STARVATION_TIME;
    public static final ForgeConfigSpec.IntValue LOG_THRESHOLD;
    public static final ForgeConfigSpec.IntValue STONE_THRESHOLD;
    public static final ForgeConfigSpec.IntValue ANIMAL_BLEED_THRESHOLD;
    public static final ForgeConfigSpec.IntValue HEARTBEAT_THRESHOLD;
    public static final ForgeConfigSpec.IntValue CRY_THRESHOLD;
    public static final ForgeConfigSpec.DoubleValue CRY_CHANCE;
    public static final ForgeConfigSpec.IntValue FLOWER_CLOSE_THRESHOLD;
    public static final ForgeConfigSpec.IntValue FLOWER_WILT_THRESHOLD;
    public static final ForgeConfigSpec.IntValue ANIMAL_FLEE_THRESHOLD;
    public static final ForgeConfigSpec.IntValue MONSTER_SPAWN_MIN;
    public static final ForgeConfigSpec.IntValue MONSTER_SPAWN_MAX;
    public static final ForgeConfigSpec.DoubleValue MONSTER_SPAWN_FACTOR;
    public static final ForgeConfigSpec.IntValue DREAM_THRESHOLD;
    public static final ForgeConfigSpec.IntValue STORM_THRESHOLD;
    public static final ForgeConfigSpec.IntValue VOLCANO_THRESHOLD;
    public static final ForgeConfigSpec.IntValue CARVING_THRESHOLD;
    public static final ForgeConfigSpec.DoubleValue CARVING_CHANCE;
    public static final ForgeConfigSpec.IntValue BLOOD_RAIN_THRESHOLD;
    public static final ForgeConfigSpec.IntValue ALT_SUN_THRESHOLD;
    public static final ForgeConfigSpec.IntValue HEAT_WAVE_THRESHOLD;

    //define all our guilt increment variables
    public static final ForgeConfigSpec.IntValue GUILT_INC_GRASS;
    public static final ForgeConfigSpec.IntValue GUILT_INC_LEAVES;
    public static final ForgeConfigSpec.IntValue GUILT_INC_LOG;
    public static final ForgeConfigSpec.IntValue GUILT_INC_STONE;
    public static final ForgeConfigSpec.IntValue GUILT_INC_MISC;
    public static final ForgeConfigSpec.IntValue GUILT_INC_KILL;

    // define booleans
    public static final ForgeConfigSpec.BooleanValue CAVE_IN_TELEPORT;
    public static final ForgeConfigSpec.BooleanValue DO_CREDITS;

    static
    {

        BUILDER.push("Mod Pace");
        PACE = BUILDER
                .comment("Overarching multiplier that will increase/decrease the speed of happenings")
                .defineInRange("pace", 1D, 0, 10000);
        BUILDER.pop();

        BUILDER.push("Guilt Thresholds");

        WATER_DRIP_THRESHOLD = BUILDER
                .comment("Guilt level required for dripping water to turn to blood.")
                .defineInRange("waterDripThreshold", 1000, 0, 10000);

        FOG_COLOR_MIN = BUILDER
                .comment("Guilt level required for fog to begin turning red")
                .defineInRange("FogColorMin", 500, 0, 10000);

        FOG_COLOR_MAX = BUILDER
                .comment("Guilt level required for fog to begin turning red")
                .defineInRange("FogColorMax", 1000, 0, 10000);

        FOG_THICKNESS_MIN = BUILDER
                .comment("Guilt level required for fog to begin thickening")
                .defineInRange("FogThicknessMin", 800, 0, 10000);

        FOG_THICKNESS_MAX = BUILDER
                .comment("Guilt level required for fog to finish thickening")
                .defineInRange("FogThicknessMax", 2500, 0, 10000);

        HOSTILE_WILDLIFE_THRESHOLD = BUILDER
                .comment("Guilt level required for neutral mobs to become permanently hostile.")
                .defineInRange("hostileWildlifeThreshold", 1200, 0, 10000);

        MUSIC_PITCH_MIN = BUILDER
                .comment("Guilt level required for the game's music to start pitching down.")
                .defineInRange("musicPitchMin", 1400, 0, 10000);

        MUSIC_PITCH_MAX = BUILDER
                .comment("Guilt level required for the game's music to stop pitching down.")
                .defineInRange("musicPitchMax", 2500, 0, 10000);

        MUSIC_PITCH_FACTOR = BUILDER
                .comment("Pitch down of music at [Music Pitch Max], i.e. 0.5 = 50% pitch at [Music Pitch Max]")
                .defineInRange("musicPitchFactor", 0.7, 0, 10000);

        LOG_THRESHOLD = BUILDER
                .comment("Requisite number of logs to be broken to trigger the tree bleed event")
                .defineInRange("logThreshold", 15, 0, 10000);

        STONE_THRESHOLD = BUILDER
                .comment("Requisite number of stone/deepslate to be broken to trigger the cave in event")
                .defineInRange("stoneThreshold", 150, 0, 10000);

        ANIMAL_BLEED_THRESHOLD = BUILDER
                .comment("Guilt level required for animals to have a chance to bleed when killed")
                .defineInRange("bleedThreshold", 400, 0, 10000);

        HEARTBEAT_THRESHOLD = BUILDER
                .comment("Guilt level required for the heartbeat sound when underground")
                .defineInRange("heartbeatThreshold", 500, 0, 10000);

        CRY_THRESHOLD = BUILDER
                .comment("Guilt level required for the crying sound")
                .defineInRange("cryThreshold", 1000, 0, 10000);

        CRY_CHANCE = BUILDER
                .comment("Likelihood for the crying sound every second while guilt >= [Cry Threshold]")
                .defineInRange("cryChance", 0.05, 0, 1);

        FLOWER_CLOSE_THRESHOLD = BUILDER
                .comment("Guilt level required for Vitaflowers to close when in proximity")
                .defineInRange("flowerCloseThreshold", 700, 0, 10000);

        FLOWER_WILT_THRESHOLD = BUILDER
                .comment("Guilt level required for Vitaflowers to wilt when in proximity")
                .defineInRange("flowerWiltThreshold", 1300, 0, 10000);

        ANIMAL_FLEE_THRESHOLD = BUILDER
                .comment("Guilt level required for passive mobs to run away from you")
                .defineInRange("animalFleeThreshold", 1000, 0, 10000);

        MONSTER_SPAWN_MIN = BUILDER
                .comment("Guilt level when monster spawn rate starts increasing")
                .defineInRange("monsterSpawnMin", 1000, 0, 10000);

        MONSTER_SPAWN_MAX = BUILDER
                .comment("Guilt level when monster spawn rate stops increasing")
                .defineInRange("monsterSpawnMax", 3000, 0, 10000);

        MONSTER_SPAWN_FACTOR = BUILDER
                .comment("How much monster spawns will be increased at [Monster Spawn Max] guilt")
                .defineInRange("monsterSpawnFactor", 0.05, 0, 10000);

        DREAM_THRESHOLD = BUILDER
                .comment("Guilt level when the dream event will occur")
                .defineInRange("dreamThreshold", 2500, 0, 10000);

        STORM_THRESHOLD = BUILDER
                .comment("Guilt level when the storm will occur")
                .defineInRange("stormThreshold", 2200, 0, 10000);

        VOLCANO_THRESHOLD = BUILDER
                .comment("Guilt level when the volcano eruption will occur")
                .defineInRange("volcanoThreshold", 1900, 0, 10000);

        CARVING_THRESHOLD = BUILDER
                .comment("Guilt level when carvings will begin to appear")
                .defineInRange("carvingThreshold", 200, 0, 10000);

        CARVING_CHANCE = BUILDER
                .comment("Chance of carvings appearing per tick after staring at a block for 3 seconds (1 = 100%)")
                .defineInRange("carvingChance", 0.1, 0, 1);

        BLOOD_RAIN_THRESHOLD = BUILDER
                .comment("Guilt level when rain will turn red")
                .defineInRange("bloodRainThreshold", 1000, 0, 10000);

        ALT_SUN_THRESHOLD = BUILDER
                .comment("Guilt level when the sun will change")
                .defineInRange("altSunThreshold", 1200, 0, 10000);

        HEAT_WAVE_THRESHOLD = BUILDER
                .comment("Guilt level when the heat wave will occur")
                .defineInRange("heatWaveThreshold", 2550, 0, 10000);

        BUILDER.pop();



        BUILDER.push("Guilt Settings");

        GUILT_INC_GRASS = BUILDER
                .comment("How much to increment guilt on breaking a grass block")
                .defineInRange("grassInc", 2, 0, 10000);

        GUILT_INC_LEAVES = BUILDER
                .comment("How much to increment guilt upon breaking leaves")
                .defineInRange("leavesInc", 3, 0, 10000);

        GUILT_INC_LOG = BUILDER
                .comment("How much to increment guilt on breaking a log")
                .defineInRange("logInc", 4, 0, 10000);

        GUILT_INC_STONE = BUILDER
                .comment("How much to increment guilt on breaking a stone or deepslate block")
                .defineInRange("stoneInc", 1, 0, 10000);

        GUILT_INC_MISC = BUILDER
                .comment("How much to increment guilt on breaking blocks that can be held by Endermen")
                .defineInRange("miscInc", 1, 0, 10000);

        GUILT_INC_KILL = BUILDER
                .comment("How much to increment guilt on killing a passive mob")
                .defineInRange("killInc", 5, 0, 10000);

        BUILDER.pop();



        BUILDER.push("Game Rules");

        CAVE_IN_TELEPORT = BUILDER
                .comment("If the player will be teleported during the cave in event")
                .define("caveInTeleport", true);

        DO_CREDITS = BUILDER
                .comment("If credits will roll upon player's true death")
                .define("doCredits", true);

        BUILDER.pop();



        BUILDER.push("Ecosystem Settings");

        BUTTERFLY_STARVATION_TIME = BUILDER
                .comment("Time in ticks (20 ticks = 1 second) before a butterfly begins to starve without a Vitaflower.")
                .defineInRange("butterflyStarvationTime", 6000, 20, 1000000);

        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}