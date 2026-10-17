package power.editmobdrops.handlers;

import net.minecraft.core.registries.Registries;
//? if >=1.21.11 {
/*import net.minecraft.resources.Identifier;
*///?} else {
import net.minecraft.resources.ResourceLocation;
//?}
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import power.editmobdrops.AddedDrop;
import power.editmobdrops.EditMobDrops;

import java.util.ArrayList;
import java.util.List;

public final class LivingDropsEventHandler {
	private static final RandomSource random = RandomSource.create();
	/** Bosses, as tagged by NeoForge and other mods (c:bosses: the ender dragon and the wither in vanilla). */
	//? if >=1.21.11 {
	/*public static final TagKey<EntityType<?>> BOSSES = TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath("c", "bosses"));
	*///?} else {
	public static final TagKey<EntityType<?>> BOSSES = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath("c", "bosses"));
	//?}

	public static void onMobDrops(LivingDropsEvent event) {
		ConfigHandler.bakeIfNeeded();
		LivingEntity entityKilled = event.getEntity();
		var registries = entityKilled.level().registryAccess();
		List<ItemStack> itemsToDrop = new ArrayList<>();

		if (ConfigHandler.mobsToClear.contains(entityKilled.getType())) {
			event.getDrops().clear();
		}

		for (AddedDrop itemToAdd : ConfigHandler.itemsToAdd) {
			// Universal chance
			if (roll(itemToAdd.chances.get(0))) {
				itemsToDrop.add(itemToAdd.createStack(randomStackSize(itemToAdd), registries));
			}
			// Monster chance
			if (entityKilled instanceof Monster && roll(itemToAdd.chances.get(1))) {
				itemsToDrop.add(itemToAdd.createStack(randomStackSize(itemToAdd), registries));
			}
			// Boss chance
			if (entityKilled.getType().is(BOSSES) && roll(itemToAdd.chances.get(2))) {
				itemsToDrop.add(itemToAdd.createStack(randomStackSize(itemToAdd), registries));
			}
			// If the mob is in any of the groups, run the chance for that group
			for (int i = 0; i < ConfigHandler.mobGroups.size(); i++) {
				if (ConfigHandler.mobGroups.get(i).contains(entityKilled.getType()) && i + 3 < itemToAdd.chances.size()
						&& roll(itemToAdd.chances.get(i + 3))) {
					itemsToDrop.add(itemToAdd.createStack(randomStackSize(itemToAdd), registries));
				}
			}
		}

		// Single mob items
		for (AddedDrop singleMobItem : ConfigHandler.singleMobItems) {
			if (entityKilled.getType() == singleMobItem.entityFrom && roll(singleMobItem.chances.get(0))) {
				itemsToDrop.add(singleMobItem.createStack(randomStackSize(singleMobItem), registries));
			}
		}

		for (ItemStack item : itemsToDrop) {
			if (ConfigHandler.debugMode) {
				EditMobDrops.LOGGER.info("Adding {} to the drops of {}", item, entityKilled.getType().getDescriptionId());
			}
			event.getDrops().add(new ItemEntity(entityKilled.level(), entityKilled.getX(), entityKilled.getY(), entityKilled.getZ(), item));
		}
	}

	private static boolean roll(double percent) {
		return random.nextDouble() * 100 < percent;
	}

	private static int randomStackSize(AddedDrop drop) {
		if (drop.minStack < drop.maxStack) {
			return random.nextInt(1 + drop.maxStack - drop.minStack) + drop.minStack;
		}
		return drop.maxStack;
	}
}
