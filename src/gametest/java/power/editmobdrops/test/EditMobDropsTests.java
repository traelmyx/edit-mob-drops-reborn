package power.editmobdrops.test;

import net.minecraft.core.BlockPos;
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
import power.editmobdrops.Reference;
import power.editmobdrops.handlers.ConfigHandler;

import java.nio.file.Files;
import java.util.List;

public class EditMobDropsTests {
    //? if >=26 {
    /*private static final EntityType<? extends Mob> COW = net.minecraft.world.entity.EntityTypes.COW, ZOMBIE = net.minecraft.world.entity.EntityTypes.ZOMBIE,
        SHEEP = net.minecraft.world.entity.EntityTypes.SHEEP, PIG = net.minecraft.world.entity.EntityTypes.PIG, RABBIT = net.minecraft.world.entity.EntityTypes.RABBIT;
    *///?} else {
    private static final EntityType<? extends Mob> COW = EntityType.COW, ZOMBIE = EntityType.ZOMBIE, SHEEP = EntityType.SHEEP, PIG = EntityType.PIG, RABBIT = EntityType.RABBIT;
    //?}

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
        //? if >=1.21.11 {
        /*mob.hurtServer(helper.getLevel(), helper.getLevel().damageSources().playerAttack(player), 10000);
        *///?} else {
        mob.hurt(helper.getLevel().damageSources().playerAttack(player), 10000);
        //?}
        List<ItemStack> drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(rel)).inflate(4)).stream().map(ItemEntity::getItem).toList();
        helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(rel)).inflate(4)).forEach(e -> e.discard());
        return drops;
    }

    private static int count(List<ItemStack> drops, Item item) {
        return drops.stream().filter(s -> s.is(item)).mapToInt(ItemStack::getCount).sum();
    }

    public static void universalDropFromEveryMob(GameTestHelper helper) {
        configure(List.of("minecraft:diamond::1:1:100"), List.of(), List.of(), List.of());
        helper.assertTrue(count(kill(helper, COW), Items.DIAMOND) == 1, "cow did not drop the diamond");
        helper.assertTrue(count(kill(helper, ZOMBIE), Items.DIAMOND) == 1, "zombie did not drop the diamond");
        helper.succeed();
    }

    public static void monsterChanceOnlyForMonsters(GameTestHelper helper) {
        configure(List.of("minecraft:emerald::2:2:0:100"), List.of(), List.of(), List.of());
        helper.assertTrue(count(kill(helper, ZOMBIE), Items.EMERALD) == 2, "zombie did not drop 2 emeralds");
        helper.assertTrue(count(kill(helper, COW), Items.EMERALD) == 0, "cow dropped emeralds");
        helper.succeed();
    }

    public static void clearedMobDropsNothingElse(GameTestHelper helper) {
        configure(List.of(), List.of(), List.of("minecraft:cow"), List.of());
        List<ItemStack> drops = kill(helper, COW);
        helper.assertTrue(drops.isEmpty(), "cleared cow still dropped " + drops);
        helper.assertTrue(!kill(helper, SHEEP).isEmpty(), "sheep lost its drops too");
        helper.succeed();
    }

    public static void singleMobDrop(GameTestHelper helper) {
        configure(List.of(), List.of(), List.of(), List.of("minecraft:pig:minecraft:gold_ingot::3:3:100"));
        helper.assertTrue(count(kill(helper, PIG), Items.GOLD_INGOT) == 3, "pig did not drop 3 gold");
        helper.assertTrue(count(kill(helper, COW), Items.GOLD_INGOT) == 0, "cow dropped the pig's gold");
        helper.succeed();
    }

    public static void groupChance(GameTestHelper helper) {
        configure(List.of("minecraft:feather::5:5:0:0:0:100"), List.of("minecraft:chicken:minecraft:rabbit"), List.of(), List.of());
        helper.assertTrue(count(kill(helper, RABBIT), Items.FEATHER) == 5, "rabbit (in group 1) did not drop 5 feathers");
        helper.assertTrue(count(kill(helper, COW), Items.FEATHER) == 0, "cow (not in the group) dropped feathers");
        helper.succeed();
    }

    public static void componentFile(GameTestHelper helper) {
        try {
            Files.createDirectories(Reference.CONFIG_PATH);
            Files.writeString(Reference.CONFIG_PATH.resolve("lucky.json"), "{\"minecraft:custom_name\": '\"Lucky Bone\"'}");
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
        configure(List.of("minecraft:bone:lucky:1:1:100"), List.of(), List.of(), List.of());
        List<ItemStack> bones = kill(helper, COW).stream().filter(s -> s.is(Items.BONE)).toList();
        helper.assertTrue(bones.size() == 1 && bones.get(0).getHoverName().getString().equals("Lucky Bone"), "bone drop: " + bones);
        configure(List.of(), List.of(), List.of(), List.of());
        helper.succeed();
    }
}
