package org.embeddedt.peacefultable.forge;

import dev.architectury.platform.forge.EventBuses;
import org.embeddedt.peacefultable.PeacefulTable;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(PeacefulTable.MOD_ID)
public class PeacefulTableForge {
    public PeacefulTableForge() {
        // Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(PeacefulTable.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        PeacefulTable.init();
    }
}
