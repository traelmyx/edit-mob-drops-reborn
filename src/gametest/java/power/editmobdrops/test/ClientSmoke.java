package power.editmobdrops.test;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.level.GameType;

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
                    Screen screen = SmokeCompat.screen(mc);
                    if (screen != null && SmokeCompat.overlayHidden(mc) && ++ticks >= 20) {
                        if (screen.getClass().getSimpleName().equals("LoadingErrorScreen")) {
                            fail("mod loading errors");
                        }
                        ticks = 0;
                        mc.options.tutorialStep = net.minecraft.client.tutorial.TutorialSteps.NONE;
                        SmokeCompat.createFlatWorld(mc, GameType.SURVIVAL);
                        stage = 1;
                    }
                }
                case 1 -> {
                    if (mc.level != null && mc.player != null && SmokeCompat.screen(mc) == null) {
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
                        SmokeCompat.hideGui(mc, true);
                    }
                    if (ticks == 120) {
                        SmokeCompat.screenshot(mc, "drops.png");
                        SmokeCompat.hideGui(mc, false);
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

    // every mob drops a diamond: kill a few cows in front of the player
    private static void setup(Minecraft mc) {
        var server = mc.getSingleplayerServer();
        server.execute(() -> {
            ConfigHandler.CONFIG.itemsToAdd.set(List.of("minecraft:diamond::1:3:100"));
            ConfigHandler.bakeConfig();
            var player = server.getPlayerList().getPlayers().get(0);
            //? if >=1.21.11 {
            /*var level = player.level();
            *///?} else {
            var level = player.serverLevel();
            //?}
            var look = player.getLookAngle();
            for (int i = -1; i <= 1; i++) {
                double x = player.getX() + look.x * 4 + i * 1.5 * look.z, z = player.getZ() + look.z * 4 - i * 1.5 * look.x;
                //? if >=26 {
                /*var cow = net.minecraft.world.entity.EntityTypes.COW.create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
                *///?} else if >=1.21.11 {
                /*var cow = net.minecraft.world.entity.EntityType.COW.create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
                *///?} else {
                var cow = net.minecraft.world.entity.EntityType.COW.create(level);
                //?}
                //? if >=1.21.11 {
                /*cow.snapTo(x, player.getY(), z, 0, 0);
                level.addFreshEntity(cow);
                cow.hurtServer(level, level.damageSources().playerAttack(player), 1000);
                *///?} else {
                cow.moveTo(x, player.getY(), z, 0, 0);
                level.addFreshEntity(cow);
                cow.hurt(level.damageSources().playerAttack(player), 1000);
                //?}
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
