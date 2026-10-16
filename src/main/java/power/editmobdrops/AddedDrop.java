package power.editmobdrops;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddedDrop {
	public boolean valid = false;
	public Item item;
	/** Name of a file in config/editmobdrops holding the item's components as SNBT, or empty. */
	public String componentFile = "";
	private DataComponentPatch components;
	private boolean componentsParsed;
	public int minStack = 1;
	public int maxStack = 1;
	public List<Double> chances = new ArrayList<>();
	public EntityType<?> entityFrom;

	public AddedDrop(String rawDropData, boolean singleDrop) {
		List<String> dropData = new ArrayList<>(Arrays.asList(rawDropData.split("\\s*:\\s*", -1)));
		if (dropData.size() < 6) {
			EditMobDrops.LOGGER.warn("Ignoring drop \"{}\": too few fields", rawDropData);
			return;
		}
		if (dropData.size() < 8 && singleDrop) {
			EditMobDrops.LOGGER.warn("Ignoring single mob drop \"{}\": too few fields", rawDropData);
			return;
		}

		// Entity data for single mob drops
		if (singleDrop) {
			ResourceLocation entityId = ResourceLocation.tryBuild(dropData.get(0), dropData.get(1));
			if (entityId == null || !BuiltInRegistries.ENTITY_TYPE.containsKey(entityId)) {
				EditMobDrops.LOGGER.warn("Ignoring single mob drop \"{}\": unknown mob", rawDropData);
				return;
			}
			entityFrom = BuiltInRegistries.ENTITY_TYPE.get(entityId);
		}

		// Item data
		ResourceLocation itemId = ResourceLocation.tryBuild(dropData.get(singleDrop ? 2 : 0), dropData.get(singleDrop ? 3 : 1));
		if (itemId == null || !BuiltInRegistries.ITEM.containsKey(itemId)) {
			EditMobDrops.LOGGER.warn("Ignoring drop \"{}\": unknown item", rawDropData);
			return;
		}
		item = BuiltInRegistries.ITEM.get(itemId);
		componentFile = dropData.get(singleDrop ? 4 : 2);

		// Stack size
		try {
			minStack = Integer.parseInt(dropData.get(singleDrop ? 5 : 3));
		} catch (NumberFormatException ignored) {
		}
		try {
			maxStack = Integer.parseInt(dropData.get(singleDrop ? 6 : 4));
		} catch (NumberFormatException ignored) {
		}

		// Chances, in percent
		for (int i = singleDrop ? 7 : 5; i < dropData.size(); i++) {
			try {
				chances.add(Double.parseDouble(dropData.get(i)));
			} catch (NumberFormatException e) {
				chances.add(0.0);
			}
		}
		while (!singleDrop && chances.size() < 3) {
			chances.add(0.0);
		}

		valid = true;
	}

	public ItemStack createStack(int count, HolderLookup.Provider registries) {
		ItemStack stack = new ItemStack(item, count);
		if (!componentsParsed) {
			components = parseComponentFile(componentFile, registries);
			componentsParsed = true;
		}
		if (components != null) {
			stack.applyComponents(components);
		}
		return stack;
	}

	/** Reads e.g. {"minecraft:custom_name": '"Lucky Bone"'} from config/editmobdrops/<name>.json (or .snbt). */
	private static DataComponentPatch parseComponentFile(String name, HolderLookup.Provider registries) {
		if (name.isEmpty()) {
			return null;
		}
		Path file = Reference.CONFIG_PATH.resolve(name + ".json");
		if (!Files.exists(file)) file = Reference.CONFIG_PATH.resolve(name + ".snbt");
		try {
			if (!Files.isReadable(file)) {
				EditMobDrops.LOGGER.warn("Component file {}.json not found", name);
				return null;
			}
			CompoundTag tag = TagParser.parseTag(Files.readString(file, StandardCharsets.UTF_8));
			return DataComponentPatch.CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), tag)
				.resultOrPartial(error -> EditMobDrops.LOGGER.warn("Component file {} is not valid: {}", name, error))
				.orElse(null);
		} catch (CommandSyntaxException e) {
			EditMobDrops.LOGGER.warn("Component file {} is not valid SNBT: {}", name, e.getMessage());
		} catch (IOException e) {
			EditMobDrops.LOGGER.warn("Couldn't read component file {}", name, e);
		}
		return null;
	}
}
