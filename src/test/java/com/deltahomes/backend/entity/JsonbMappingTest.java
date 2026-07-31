package com.deltahomes.backend.entity;

import com.deltahomes.backend.entity.property.Property;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class JsonbMappingTest {

    @Test
    void propertyFeaturesFieldUsesJsonJdbcType() throws NoSuchFieldException {
        Field featuresField = Property.class.getDeclaredField("features");
        JdbcTypeCode annotation = featuresField.getAnnotation(JdbcTypeCode.class);

        assertNotNull(annotation);
        assertEquals(SqlTypes.JSON, annotation.value());
    }
}
