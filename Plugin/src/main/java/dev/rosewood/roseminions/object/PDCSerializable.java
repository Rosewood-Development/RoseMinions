package dev.rosewood.roseminions.object;

import org.bukkit.persistence.PersistentDataContainer;

public interface PDCSerializable {

    void writePDC(PersistentDataContainer container);

    void readPDC(PersistentDataContainer container);

}
