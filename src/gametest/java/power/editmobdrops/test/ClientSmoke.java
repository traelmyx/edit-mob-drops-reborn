package power.editmobdrops.test;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;

import power.editmobdrops.handlers.ConfigHandler;
import net.minecraft.world.entity.item.ItemEntity;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.nio.file.Files;

// Run with -Dsmoketest=true: creates a flat world, takes a screenshot and quits. Exit code 1 on failure.
public final class ClientSmoke {

    public static final boolean ENABLED = Boolean.getBoolean("smoketest");
    private static final long TIMEOUT_MS = 5 * 60 * 1000;

    private static int stage;
    private static int ticks;

    private ClientSmoke() {
    }

    public static boolean start() {
        if (!ENABLED) {
            return false;
        }
        Thread watchdog = new Thread(() -> {
            try {
                Thread.sleep(TIMEOUT_MS);
                fail("timed out");
            } catch (InterruptedException ignored) {
            }
        }, "smoketest-watchdog");
        watchdog.setDaemon(true);
        watchdog.start();
        return true;
    }

    public static void tick(Minecraft mc) {
        try {
            switch (stage) {
                case 0 -> {
                    Screen screen = mc.screen;
                    if (screen != null && mc.getOverlay() == null && ++ticks >= 20) {
                        if (screen.getClass().getSimpleName().equals("LoadingErrorScreen")) {
                            fail("mod loading errors");
                        }
                        ticks = 0;
                        mc.options.tutorialStep = net.minecraft.client.tutorial.TutorialSteps.NONE;
                        createWorld(mc);
                        stage = 1;
                    }
                }
                case 1 -> {
                    if (mc.level != null && mc.player != null && mc.screen == null) {
                        stage = 2;
                    }
                }
                case 2 -> {
                    ticks++;
                    if (ticks == 40) {
                        setup(mc);
                    }
                    if (ticks == 60) {
                        mc.player.setXRot(35);
                        mc.options.hideGui = true;
                    }
                    if (ticks == 120) {
                        Screenshot.grab(mc.gameDirectory, "drops.png", mc.getMainRenderTarget(), msg -> {});
                        mc.options.hideGui = false;
                    }
                    if (ticks >= 130) {
                        long diamonds = mc.level.getEntitiesOfClass(ItemEntity.class, mc.player.getBoundingBox().inflate(10)).stream().filter(e -> e.getItem().is(Items.DIAMOND)).count();
                        if (diamonds == 0) fail("client sees no diamond drops");
                        Files.writeString(mc.gameDirectory.toPath().resolve("smoke-ok"), "ok\n");
                        stage = 3;
                        mc.stop();
                    }
                }
                default -> {
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
            fail(t.toString());
        }
    }

    private static void createWorld(Minecraft mc) {
        LevelSettings settings = new LevelSettings("smoke", GameType.SURVIVAL, false, Difficulty.PEACEFUL, true, new GameRules(), WorldDataConfiguration.DEFAULT);
        mc.createWorldOpenFlows().createFreshLevel("smoke", settings, new WorldOptions(0L, false, false),
            registries -> registries.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),
            mc.screen);
    }

    // every mob drops a diamond: kill a few cows in front of the player
    private static void setup(Minecraft mc) {
        var server = mc.getSingleplayerServer();
        server.execute(() -> {
            ConfigHandler.CONFIG.itemsToAdd.set(List.of("minecraft:diamond::1:3:100"));
            ConfigHandler.bakeConfig();
            var player = server.getPlayerList().getPlayers().get(0);
            var level = player.serverLevel();
            var look = player.getLookAngle();
            for (int i = -1; i <= 1; i++) {
                var cow = net.minecraft.world.entity.EntityType.COW.create(level);
                cow.moveTo(player.getX() + look.x * 4 + i * 1.5 * look.z, player.getY(), player.getZ() + look.z * 4 - i * 1.5 * look.x, 0, 0);
                level.addFreshEntity(cow);
                cow.hurt(level.damageSources().playerAttack(player), 1000);
            }
        });
    }

    public static void fail(String why) {
        System.err.println("Smoke test failed: " + why);
        System.out.println("[clientsmoke] FAILED: " + why);
        System.err.flush();
        Runtime.getRuntime().halt(1);
    }
}
