package dev.rosewood.roseminions.model;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;

public interface PDCSerializable {

    void writePDC(PersistentDataContainer container, PersistentDataAdapterContext context);

    void readPDC(PersistentDataContainer container);

}
