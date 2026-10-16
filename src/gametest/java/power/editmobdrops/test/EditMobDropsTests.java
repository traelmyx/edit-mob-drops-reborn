package power.editmobdrops.test;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import power.editmobdrops.Reference;
import power.editmobdrops.handlers.ConfigHandler;

import java.nio.file.Files;
import java.util.List;

@GameTestHolder(EditMobDropsTests.MOD_ID)
@PrefixGameTestTemplate(false)
public class EditMobDropsTests {
    public static final String MOD_ID = "editmobdrops_gametest";

    // Each test rewrites the shared config, so each runs in its own batch.
    private static void configure(List<String> items, List<String> groups, List<String> clear, List<String> single) {
        ConfigHandler.CONFIG.itemsToAdd.set(items);
        ConfigHandler.CONFIG.mobGroups.set(groups);
        ConfigHandler.CONFIG.mobsToClear.set(clear);
        ConfigHandler.CONFIG.singleMobItems.set(single);
        ConfigHandler.bakeConfig();
    }

    /** Kills a fresh mob as a player would and returns what it dropped. */
    private static List<ItemStack> kill(GameTestHelper helper, EntityType<? extends Mob> type) {
        BlockPos rel = new BlockPos(1, 2, 1);
        helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(rel)).inflate(4)).forEach(e -> e.discard());
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        LivingEntity mob = helper.spawnWithNoFreeWill(type, rel);
        mob.hurt(helper.getLevel().damageSources().playerAttack(player), 10000);
        List<ItemStack> drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(rel)).inflate(4)).stream().map(ItemEntity::getItem).toList();
        helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(rel)).inflate(4)).forEach(e -> e.discard());
        return drops;
    }

    private static int count(List<ItemStack> drops, Item item) {
        return drops.stream().filter(s -> s.is(item)).mapToInt(ItemStack::getCount).sum();
    }

    @GameTest(template = "empty", batch = "universal")
    public static void universalDropFromEveryMob(GameTestHelper helper) {
        configure(List.of("minecraft:diamond::1:1:100"), List.of(), List.of(), List.of());
        helper.assertTrue(count(kill(helper, EntityType.COW), Items.DIAMOND) == 1, "cow did not drop the diamond");
        helper.assertTrue(count(kill(helper, EntityType.ZOMBIE), Items.DIAMOND) == 1, "zombie did not drop the diamond");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "monster")
    public static void monsterChanceOnlyForMonsters(GameTestHelper helper) {
        configure(List.of("minecraft:emerald::2:2:0:100"), List.of(), List.of(), List.of());
        helper.assertTrue(count(kill(helper, EntityType.ZOMBIE), Items.EMERALD) == 2, "zombie did not drop 2 emeralds");
        helper.assertTrue(count(kill(helper, EntityType.COW), Items.EMERALD) == 0, "cow dropped emeralds");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "clear")
    public static void clearedMobDropsNothingElse(GameTestHelper helper) {
        configure(List.of(), List.of(), List.of("minecraft:cow"), List.of());
        List<ItemStack> drops = kill(helper, EntityType.COW);
        helper.assertTrue(drops.isEmpty(), "cleared cow still dropped " + drops);
        helper.assertTrue(!kill(helper, EntityType.SHEEP).isEmpty(), "sheep lost its drops too");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "single")
    public static void singleMobDrop(GameTestHelper helper) {
        configure(List.of(), List.of(), List.of(), List.of("minecraft:pig:minecraft:gold_ingot::3:3:100"));
        helper.assertTrue(count(kill(helper, EntityType.PIG), Items.GOLD_INGOT) == 3, "pig did not drop 3 gold");
        helper.assertTrue(count(kill(helper, EntityType.COW), Items.GOLD_INGOT) == 0, "cow dropped the pig's gold");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "group")
    public static void groupChance(GameTestHelper helper) {
        configure(List.of("minecraft:feather::5:5:0:0:0:100"), List.of("minecraft:chicken:minecraft:rabbit"), List.of(), List.of());
        helper.assertTrue(count(kill(helper, EntityType.RABBIT), Items.FEATHER) == 5, "rabbit (in group 1) did not drop 5 feathers");
        helper.assertTrue(count(kill(helper, EntityType.COW), Items.FEATHER) == 0, "cow (not in the group) dropped feathers");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "components")
    public static void componentFile(GameTestHelper helper) throws Exception {
        Files.createDirectories(Reference.CONFIG_PATH);
        Files.writeString(Reference.CONFIG_PATH.resolve("lucky.json"), "{\"minecraft:custom_name\": '\"Lucky Bone\"'}");
        configure(List.of("minecraft:bone:lucky:1:1:100"), List.of(), List.of(), List.of());
        List<ItemStack> bones = kill(helper, EntityType.COW).stream().filter(s -> s.is(Items.BONE)).toList();
        helper.assertTrue(bones.size() == 1 && bones.get(0).getHoverName().getString().equals("Lucky Bone"), "bone drop: " + bones);
        configure(List.of(), List.of(), List.of(), List.of());
        helper.succeed();
    }
}
