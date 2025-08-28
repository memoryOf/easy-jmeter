package com.zhao.easyJmeter.common.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.zhao.easyJmeter.common.enumeration.JmeterStatusEnum;

import java.io.IOException;

public class JmeterStatusEnumSerializer extends JsonSerializer<JmeterStatusEnum> {
    @Override
    public void serialize(JmeterStatusEnum value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("value");
        gen.writeObject(value.getValue());
        gen.writeFieldName("desc");
        gen.writeObject(value.getDesc());
        gen.writeEndObject();
    }
}
