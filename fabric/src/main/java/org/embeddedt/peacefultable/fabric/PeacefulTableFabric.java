package org.embeddedt.peacefultable.fabric;

import org.embeddedt.peacefultable.PeacefulTable;
import net.fabricmc.api.ModInitializer;

public class PeacefulTableFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        PeacefulTable.init();
    }
}
