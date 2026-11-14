/**
 * File: FluidProvider.java
 * Author: Maxime66410
 * Created: 2026-11-02
 * Last Modified: 2026-11-14
 */
package org.furranystudio.avalinexus.compat.jade;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.furranystudio.avalinexus.AvaliNexus;
import org.furranystudio.avalinexus.block.GraphenePipeBlock;
import org.furranystudio.avalinexus.block.machine.MachineBlockEntity;
import org.furranystudio.avalinexus.block.machine.MachineTank;
import org.furranystudio.avalinexus.block.pipe.GraphenePipeBlockEntity;
import org.furranystudio.avalinexus.block.pipe.PipeNetwork;
import org.furranystudio.avalinexus.block.tank.GrapheneTankBlockEntity;
import org.furranystudio.avalinexus.block.tank.TankStructure;
import org.furranystudio.avalinexus.fluid.AvaliLiquids;
import snownee.jade.api.Accessor;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.fluid.JadeFluidObject;
import snownee.jade.api.view.ClientViewGroup;
import snownee.jade.api.view.FluidView;
import snownee.jade.api.view.IClientExtensionProvider;
import snownee.jade.api.view.IServerExtensionProvider;
import snownee.jade.api.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public enum FluidProvider implements IServerExtensionProvider<FluidView.Data>, IClientExtensionProvider<FluidView.Data, FluidView> {
    INSTANCE;

    private static final Identifier UID = AvaliNexus.id("fluids");

    @Override
    public Identifier getUid() {
        return UID;
    }

    @Override
    public List<ViewGroup<FluidView.Data>> getGroups(Accessor<?> accessor) {
        if (!(accessor instanceof BlockAccessor block)) {
            return null;
        }
        List<FluidView.Data> views = new ArrayList<>();
        if (block.getBlockEntity() instanceof MachineBlockEntity machine) {
            for (MachineTank tank : machine.tanks()) {
                views.add(data(tank.fluid(), tank.amount(), tank.capacity()));
            }
        } else if (block.getBlockEntity() instanceof GrapheneTankBlockEntity) {
            TankStructure tank = TankStructure.at(block.getLevel(), block.getPosition());
            views.add(data(tank.fluid(), tank.amount(), tank.capacity()));
        } else if (block.getBlockEntity() instanceof GraphenePipeBlockEntity pipe) {
            Fluid fluid = GraphenePipeBlock.fluidAt(block.getLevel(), block.getPosition(), block.getBlockState());
            views.add(data(fluid, pipe.amount(), PipeNetwork.PIPE_CAPACITY));
        }
        return views.isEmpty() ? null : List.of(new ViewGroup<>(views));
    }

    private static FluidView.Data data(Fluid fluid, int amount, int capacity) {
        JadeFluidObject object = fluid == Fluids.EMPTY || amount <= 0 ? JadeFluidObject.empty() : JadeFluidObject.of(fluid, scale(amount));
        return new FluidView.Data(object, scale(capacity));
    }

    private static long scale(int amount) {
        return amount * JadeFluidObject.bucketVolume() / AvaliLiquids.BUCKET;
    }

    @Override
    public List<ClientViewGroup<FluidView>> getClientGroups(Accessor<?> accessor, List<ViewGroup<FluidView.Data>> groups) {
        return ClientViewGroup.map(groups, FluidView::readDefault, (group, clientGroup) -> {
        });
    }
}
