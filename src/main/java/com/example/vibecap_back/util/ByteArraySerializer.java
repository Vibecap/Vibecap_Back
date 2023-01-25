package com.example.vibecap_back.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class ByteArraySerializer extends JsonSerializer<byte[]> {

    @Override
    public void serialize(byte[] bytes, JsonGenerator jsonGenerator, SerializerProvider provider) throws IOException {
        jsonGenerator.writeStartArray();

        for (byte b : bytes) {
            jsonGenerator.writeNumber(unsignedToBytes(b));
        }

        jsonGenerator.writeEndArray();
    }

    private static int unsignedToBytes(byte b) {
        return b & 0xFF;
    }

}