package dev.benew.filmbiblecrawling.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import com.jayway.jsonpath.TypeRef;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Slf4j
public class JsonUtil {
    public static ObjectMapper getMapperStatic() {
        return new ObjectMapper().registerModule(new JavaTimeModule()
                .addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                .addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern("HH:mm")))
                .addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                .addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern("HH:mm")))
        );
    }
    public static String prettyString(Object object) {
        try {
            String s = getMapperStatic().writerWithDefaultPrettyPrinter().writeValueAsString(
                    object
            );
            log.info(s);
            return s;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> T readValue(
            String json,
            ObjectMapper mapper,
            TypeReference<T> typeReference
    ) {
        if (mapper == null)
            mapper = getMapperStatic();
        try {
            return mapper.readValue(json, typeReference);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ReadContext jsonParse(
            String json,
            ObjectMapper mapper
    ) {
        if (mapper != null)
            mapper = getMapperStatic();


        return JsonPath.using(
                        Configuration.builder()
                                .jsonProvider(new JacksonJsonProvider(mapper))
                                .mappingProvider(new JacksonMappingProvider(mapper))
                                .build())
                .parse(json);
    }
    public static ReadContext jsonParseNoException(
            String json,
            ObjectMapper mapper
    ) {
        try {
            return jsonParse(json, mapper);
        } catch (Exception e) {
            return null;
        }
    }

    public static <T> T getValue(ReadContext readContext, String expression, TypeRef<T> typeRef) {
        try {
            return readContext.read(expression, typeRef);
        } catch (Exception e) {
            log.error("readContext - read error");
            log.error("expression:" + expression);
            e.printStackTrace();
            /*throw new RuntimeException("readContext - read error throw");*/
            return null;
        }
    }

    public static <T> T getValueNoEx(ReadContext readContext, String expression, TypeRef<T> typeRef) {
        try {
            return readContext.read(expression, typeRef);
        } catch (Exception e) {
            return null;
        }
    }
}
