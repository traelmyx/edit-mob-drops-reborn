package power.editmobdrops.test.neoforge;

//? if neoforge {
import power.editmobdrops.test.ClientSmoke;
import power.editmobdrops.test.EditMobDropsTests;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
//? if >=1.21.5 {
/*import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Consumer;
*///?} else {
import net.minecraft.gametest.framework.GameTest;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
//?}

//? if <1.21.5 {
@GameTestHolder(NeoForgeGameTests.MOD_ID)
@PrefixGameTestTemplate(false)
//?}
@Mod(NeoForgeGameTests.MOD_ID)
public class NeoForgeGameTests {
	public static final String MOD_ID = "editmobdrops_gametest";

	public NeoForgeGameTests(IEventBus modBus, Dist dist) {
		//? if >=1.21.5 {
		/*FUNCTIONS.register(modBus);
		modBus.addListener(NeoForgeGameTests::registerTests);
		*///?}
		if (dist.isClient() && ClientSmoke.start()) {
			NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post e) -> ClientSmoke.tick(net.minecraft.client.Minecraft.getInstance()));
		}
	}

	//? if >=1.21.5 {
	/*private static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS = DeferredRegister.create(Registries.TEST_FUNCTION, MOD_ID);
	private static final Identifier EMPTY = Identifier.fromNamespaceAndPath(MOD_ID, "empty");

	static {
		FUNCTIONS.register("universal_drop_from_every_mob", () -> EditMobDropsTests::universalDropFromEveryMob);
		FUNCTIONS.register("monster_chance_only_for_monsters", () -> EditMobDropsTests::monsterChanceOnlyForMonsters);
		FUNCTIONS.register("cleared_mob_drops_nothing_else", () -> EditMobDropsTests::clearedMobDropsNothingElse);
		FUNCTIONS.register("single_mob_drop", () -> EditMobDropsTests::singleMobDrop);
		FUNCTIONS.register("group_chance", () -> EditMobDropsTests::groupChance);
		FUNCTIONS.register("component_file", () -> EditMobDropsTests::componentFile);
	}

	private static void registerTests(RegisterGameTestsEvent event) {
		//? if >=26 {
		Holder<TestEnvironmentDefinition<?>> env = event.registerEnvironment(Identifier.fromNamespaceAndPath(MOD_ID, "default"));
		//?} else
		/^Holder<TestEnvironmentDefinition> env = event.registerEnvironment(Identifier.fromNamespaceAndPath(MOD_ID, "default"));^/
		register(event, event.registerEnvironment(Identifier.fromNamespaceAndPath(MOD_ID, "universal_drop_from_every_mob")), "universal_drop_from_every_mob", 100, 1, 1);
		register(event, event.registerEnvironment(Identifier.fromNamespaceAndPath(MOD_ID, "monster_chance_only_for_monsters")), "monster_chance_only_for_monsters", 100, 1, 1);
		register(event, event.registerEnvironment(Identifier.fromNamespaceAndPath(MOD_ID, "cleared_mob_drops_nothing_else")), "cleared_mob_drops_nothing_else", 100, 1, 1);
		register(event, event.registerEnvironment(Identifier.fromNamespaceAndPath(MOD_ID, "single_mob_drop")), "single_mob_drop", 100, 1, 1);
		register(event, event.registerEnvironment(Identifier.fromNamespaceAndPath(MOD_ID, "group_chance")), "group_chance", 100, 1, 1);
		register(event, event.registerEnvironment(Identifier.fromNamespaceAndPath(MOD_ID, "component_file")), "component_file", 100, 1, 1);
	}

	//? if >=26 {
	private static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> env, String name, int maxTicks, int attempts, int successes) {
	//?} else
	/^private static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition> env, String name, int maxTicks, int attempts, int successes) {^/
		Identifier id = Identifier.fromNamespaceAndPath(MOD_ID, name);
		event.registerTest(id, new FunctionGameTestInstance(ResourceKey.create(Registries.TEST_FUNCTION, id),
			//? if >=26.3 {
			new TestData<>(env, net.minecraft.world.level.Level.OVERWORLD, EMPTY, maxTicks, 0, true,
				net.minecraft.world.level.block.Rotation.NONE, false, attempts, successes, false, 0)));
			//?} else if >=26 {
			/^new TestData<>(env, EMPTY, maxTicks, 0, true,
				net.minecraft.world.level.block.Rotation.NONE, false, attempts, successes, false, 0)));
			^///?} else {
			/^new TestData<>(env, EMPTY, maxTicks, 0, true,
				net.minecraft.world.level.block.Rotation.NONE, false, attempts, successes, false)));
			^///?}
	}
	*///?} else {
	@GameTest(template = "empty", timeoutTicks = 100, batch = "universal_drop_from_every_mob")
	public static void universalDropFromEveryMob(GameTestHelper helper) {
		EditMobDropsTests.universalDropFromEveryMob(helper);
	}

	@GameTest(template = "empty", timeoutTicks = 100, batch = "monster_chance_only_for_monsters")
	public static void monsterChanceOnlyForMonsters(GameTestHelper helper) {
		EditMobDropsTests.monsterChanceOnlyForMonsters(helper);
	}

	@GameTest(template = "empty", timeoutTicks = 100, batch = "cleared_mob_drops_nothing_else")
	public static void clearedMobDropsNothingElse(GameTestHelper helper) {
		EditMobDropsTests.clearedMobDropsNothingElse(helper);
	}

	@GameTest(template = "empty", timeoutTicks = 100, batch = "single_mob_drop")
	public static void singleMobDrop(GameTestHelper helper) {
		EditMobDropsTests.singleMobDrop(helper);
	}

	@GameTest(template = "empty", timeoutTicks = 100, batch = "group_chance")
	public static void groupChance(GameTestHelper helper) {
		EditMobDropsTests.groupChance(helper);
	}

	@GameTest(template = "empty", timeoutTicks = 100, batch = "component_file")
	public static void componentFile(GameTestHelper helper) {
		EditMobDropsTests.componentFile(helper);
	}
	//?}
}
//?}
