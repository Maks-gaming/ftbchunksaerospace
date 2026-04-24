package dev.mgcode.ftbchunksaerospace;

import com.mojang.logging.LogUtils;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(FtbChunksSableAerospaceMod.MOD_ID)
public final class FtbChunksSableAerospaceMod {
    public static final String MOD_ID = "ftbchunksaerospace";
    public static final Logger LOGGER = LogUtils.getLogger();

    public FtbChunksSableAerospaceMod(final ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.CLIENT, FtbChunksSableAerospaceConfig.CLIENT_SPEC);

        NeoForge.EVENT_BUS.register(new SableFtbChunksCompat());
        if (FMLEnvironment.dist.isClient()) {
            NeoForge.EVENT_BUS.register(new SableAerospaceClientWarnings());
        }
        LOGGER.info("Loading mod {} with Sable + FTB Chunks claim protection", MOD_ID);
    }
}