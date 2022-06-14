package dev.rosewood.roseminions.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public interface ObjectSerializable {

    void serialize(ObjectOutputStream outputStream) throws IOException;

    void deserialize(ObjectInputStream inputStream) throws IOException;

}
