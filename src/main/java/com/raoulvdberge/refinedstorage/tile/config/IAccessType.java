package com.raoulvdberge.refinedstorage.tile.config;

import com.raoulvdberge.refinedstorage.api.network.INetworkNodeProxy;
import com.raoulvdberge.refinedstorage.api.storage.AccessType;
import com.raoulvdberge.refinedstorage.tile.data.ITileDataConsumer;
import com.raoulvdberge.refinedstorage.tile.data.ITileDataProducer;
import com.raoulvdberge.refinedstorage.tile.data.RSSerializers;
import com.raoulvdberge.refinedstorage.tile.data.TileDataParameter;
import net.minecraft.tileentity.TileEntity;

public interface IAccessType {
    static <T extends TileEntity & INetworkNodeProxy> TileDataParameter<AccessType> createParameter() {
        return new TileDataParameter<>(RSSerializers.ACCESS_TYPE_SERIALIZER, AccessType.INSERT_EXTRACT, new ITileDataProducer<AccessType, T>() {
            @Override
            public AccessType getValue(T tile) {
                return ((IAccessType) tile.getNode()).getAccessType();
            }
        }, new ITileDataConsumer<AccessType, T>() {
            @Override
            public void setValue(T tile, AccessType value) {
                ((IAccessType) tile.getNode()).setAccessType(value);
            }
        });
    }

    AccessType getAccessType();

    void setAccessType(AccessType accessType);
}