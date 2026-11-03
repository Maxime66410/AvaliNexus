/**
 * File: AvaliJadePlugin.java
 * Author: Maxime66410
 * Created: 2026-11-02
 * Last Modified: 2026-11-02
 */
package org.furranystudio.avalinexus.compat.jade;

import org.furranystudio.avalinexus.AvaliNexus;
import org.furranystudio.avalinexus.block.heater.HeaterBlockEntity;
import org.furranystudio.avalinexus.block.machine.MachineBlockEntity;
import org.furranystudio.avalinexus.block.pipe.GraphenePipeBlockEntity;
import org.furranystudio.avalinexus.block.tank.GrapheneTankBlockEntity;
import org.furranystudio.avalinexus.entity.avali.AvaliEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin(AvaliNexus.MODID)
public class AvaliJadePlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerProgress(ProgressProvider.INSTANCE, MachineBlockEntity.class);
        registration.registerProgress(ProgressProvider.INSTANCE, HeaterBlockEntity.class);
        registration.registerFluidStorage(FluidProvider.INSTANCE, MachineBlockEntity.class);
        registration.registerFluidStorage(FluidProvider.INSTANCE, GrapheneTankBlockEntity.class);
        registration.registerFluidStorage(FluidProvider.INSTANCE, GraphenePipeBlockEntity.class);
        registration.registerEntityDataProvider(AvaliDataProvider.INSTANCE, AvaliEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerProgressClient(ProgressProvider.INSTANCE);
        registration.registerFluidStorageClient(FluidProvider.INSTANCE);
        registration.registerEntityComponent(AvaliProvider.INSTANCE, AvaliEntity.class);
    }
}
