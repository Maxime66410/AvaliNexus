/**
 * File: ProgressProvider.java
 * Author: Maxime66410
 * Created: 2026-11-02
 * Last Modified: 2026-11-02
 */
package org.furranystudio.avalinexus.compat.jade;

import net.minecraft.resources.Identifier;
import org.furranystudio.avalinexus.AvaliNexus;
import org.furranystudio.avalinexus.block.heater.HeaterBlockEntity;
import org.furranystudio.avalinexus.block.machine.MachineBlockEntity;
import org.furranystudio.avalinexus.block.machine.MachineMenu;
import org.furranystudio.avalinexus.client.ui.AvaliUi;
import snownee.jade.api.Accessor;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.JadeUI;
import snownee.jade.api.view.ClientViewGroup;
import snownee.jade.api.view.IClientExtensionProvider;
import snownee.jade.api.view.IServerExtensionProvider;
import snownee.jade.api.view.ProgressView;
import snownee.jade.api.view.ViewGroup;

import java.util.List;

public enum ProgressProvider implements IServerExtensionProvider<ProgressView.Data>, IClientExtensionProvider<ProgressView.Data, ProgressView> {
    INSTANCE;

    private static final Identifier UID = AvaliNexus.id("progress");

    @Override
    public Identifier getUid() {
        return UID;
    }

    @Override
    public List<ViewGroup<ProgressView.Data>> getGroups(Accessor<?> accessor) {
        if (!(accessor instanceof BlockAccessor block)) {
            return null;
        }
        if (block.getBlockEntity() instanceof MachineBlockEntity machine) {
            MachineMenu.Progress work = machine.work();
            if (work.duration() <= 0 || work.progress() <= 0) {
                return null;
            }
            return group(Math.min(1.0F, (float) work.progress() / work.duration()));
        }
        if (block.getBlockEntity() instanceof HeaterBlockEntity heater && heater.litTime() > 0 && heater.litDuration() > 0) {
            return group((float) heater.litTime() / heater.litDuration());
        }
        return null;
    }

    private static List<ViewGroup<ProgressView.Data>> group(float progress) {
        return List.of(new ViewGroup<>(List.of(new ProgressView.Data(progress))));
    }

    @Override
    public List<ClientViewGroup<ProgressView>> getClientGroups(Accessor<?> accessor, List<ViewGroup<ProgressView.Data>> groups) {
        return ClientViewGroup.map(groups, ProgressProvider::view, (group, clientGroup) -> {
        });
    }

    private static ProgressView view(ProgressView.Data data) {
        ProgressView view = new ProgressView(JadeUI.progressStyle(), BoxStyle.nestedBox());
        view.parts = List.of(new ProgressView.PartBuilder().progress(data.progress()).target(data.speed(), data.target())
            .messageType(data.messageType()).color(AvaliUi.PRIMARY_ORANGE).build());
        return view;
    }
}
