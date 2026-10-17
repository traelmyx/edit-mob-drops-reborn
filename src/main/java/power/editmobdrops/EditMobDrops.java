package power.editmobdrops;

import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import power.editmobdrops.handlers.ConfigHandler;
import power.editmobdrops.handlers.LivingDropsEventHandler;

import java.io.IOException;
import java.nio.file.Files;

@Mod(Reference.MODID)
public class EditMobDrops {
	public static final Logger LOGGER = LoggerFactory.getLogger("EditMobDrops");

	public EditMobDrops(IEventBus modBus, ModContainer container) {
		Reference.CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve(Reference.MODID);
		try {
			Files.createDirectories(Reference.CONFIG_PATH);
		} catch (IOException e) {
			LOGGER.error("Could not create the editmobdrops config directory", e);
		}

		//? if >=26.3 {
		/*container.registerConfig(ModConfig.Type.LOCAL, ConfigHandler.CONFIG_SPEC, Reference.MODID + "/editmobdrops.toml");
		*///?} else
		container.registerConfig(ModConfig.Type.COMMON, ConfigHandler.CONFIG_SPEC, Reference.MODID + "/editmobdrops.toml");
		modBus.addListener((ModConfigEvent.Loading event) -> ConfigHandler.onConfigChanged(event.getConfig()));
		modBus.addListener((ModConfigEvent.Reloading event) -> ConfigHandler.onConfigChanged(event.getConfig()));
		NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, LivingDropsEventHandler::onMobDrops);
	}
}
