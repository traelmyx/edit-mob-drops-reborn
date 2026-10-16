package power.editmobdrops.handlers;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;
import power.editmobdrops.AddedDrop;
import power.editmobdrops.EditMobDrops;
import power.editmobdrops.Reference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ConfigHandler {
	public static final EditMobDropsConfig CONFIG;
	public static final ModConfigSpec CONFIG_SPEC;
	public static boolean debugMode;
	public static List<AddedDrop> itemsToAdd = List.of();
	public static List<List<EntityType<?>>> mobGroups = List.of();
	public static List<EntityType<?>> mobsToClear = List.of();
	public static List<AddedDrop> singleMobItems = List.of();
	private static volatile boolean dirty = true;

	static {
		final Pair<EditMobDropsConfig, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(EditMobDropsConfig::new);
		CONFIG_SPEC = specPair.getRight();
		CONFIG = specPair.getLeft();
	}

	public static void onConfigChanged(ModConfig config) {
		if (config.getSpec() == CONFIG_SPEC) {
			dirty = true;
		}
	}

	/** Parses the config the first time drops are needed after it (re)loads, when every mod's items and mobs exist. */
	public static void bakeIfNeeded() {
		if (dirty) {
			dirty = false;
			bakeConfig();
		}
	}

	public static void bakeConfig() {
		debugMode = CONFIG.debugMode.get();

		List<AddedDrop> items = new ArrayList<>();
		for (String raw : CONFIG.itemsToAdd.get()) {
			AddedDrop drop = new AddedDrop(raw, false);
			if (drop.valid) items.add(drop);
		}

		List<List<EntityType<?>>> groups = new ArrayList<>();
		for (String raw : CONFIG.mobGroups.get()) {
			groups.add(parseMobs(raw));
		}

		List<EntityType<?>> clear = new ArrayList<>();
		for (String raw : CONFIG.mobsToClear.get()) {
			clear.addAll(parseMobs(raw));
		}

		List<AddedDrop> single = new ArrayList<>();
		for (String raw : CONFIG.singleMobItems.get()) {
			AddedDrop drop = new AddedDrop(raw, true);
			if (drop.valid) single.add(drop);
		}

		itemsToAdd = items;
		mobGroups = groups;
		mobsToClear = clear;
		singleMobItems = single;
	}

	private static List<EntityType<?>> parseMobs(String raw) {
		List<String> split = new ArrayList<>(Arrays.asList(raw.split("\\s*:\\s*")));
		List<EntityType<?>> mobs = new ArrayList<>();
		for (int i = 0; i < split.size() - 1; i += 2) {
			ResourceLocation id = ResourceLocation.tryBuild(split.get(i), split.get(i + 1));
			if (id != null && BuiltInRegistries.ENTITY_TYPE.containsKey(id)) {
				mobs.add(BuiltInRegistries.ENTITY_TYPE.get(id));
			} else {
				EditMobDrops.LOGGER.warn("Unknown mob {}:{} in \"{}\"", split.get(i), split.get(i + 1), raw);
			}
		}
		return mobs;
	}

	public static class EditMobDropsConfig {
		public final ModConfigSpec.BooleanValue debugMode;
		public final ModConfigSpec.ConfigValue<List<? extends String>> itemsToAdd;
		public final ModConfigSpec.ConfigValue<List<? extends String>> mobGroups;
		public final ModConfigSpec.ConfigValue<List<? extends String>> mobsToClear;
		public final ModConfigSpec.ConfigValue<List<? extends String>> singleMobItems;

		public EditMobDropsConfig(ModConfigSpec.Builder builder) {
			debugMode = builder
					.comment("If debug mode is active, the mod will log when items are dropped")
					.translation(Reference.MODID + ".config.debugMode")
					.define("debugMode", false);

			itemsToAdd = builder
					.comment("Items to add, in the form modid:itemName:[componentfile]:minStackSize:maxStackSize:universalChance:[monsterChance]:[bossChance]:[group1Chance]:[group2Chance]...\nChances are %\nThe component file is an SNBT file in the \"editmobdrops\" folder here in config (leave blank for none)")
					.translation(Reference.MODID + ".config.itemsToAdd")
					.defineListAllowEmpty("itemsToAdd", List.of(), () -> "", o -> o instanceof String);

			mobGroups = builder
					.comment("Mob groups, in the form [EntityModid]:[EntityName]:[EntityModid]:[EntityName]...")
					.translation(Reference.MODID + ".config.mobGroups")
					.defineListAllowEmpty("mobGroups", List.of(), () -> "", o -> o instanceof String);

			mobsToClear = builder
					.comment("Mobs to clear existing drops from")
					.translation(Reference.MODID + ".config.mobsToClear")
					.defineListAllowEmpty("mobsToClear", List.of(), () -> "", o -> o instanceof String);

			singleMobItems = builder
					.comment("Single mob items, in the form EntityModid:EntityName:modid:itemName:[componentfile]:minStackSize:maxStackSize:chance")
					.translation(Reference.MODID + ".config.singleMobItems")
					.defineListAllowEmpty("singleMobItems", List.of(), () -> "", o -> o instanceof String);
		}
	}
}
