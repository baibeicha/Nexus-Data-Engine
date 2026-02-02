package by.nexus.data.processor.util;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.springframework.stereotype.Component;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

@Component
public class DynamicAvroSchemaGenerator {

    public Schema generate(ResultSetMetaData metaData) throws SQLException {
        SchemaBuilder.FieldAssembler<Schema> fields = SchemaBuilder.record("NexusData")
                .namespace("by.nexus.data")
                .fields();

        int columnCount = metaData.getColumnCount();

        for (int i = 1; i <= columnCount; i++) {
            String columnName = metaData.getColumnName(i);

            String safeName = normalizeName(columnName);

            switch (sqlType(metaData, i)) {
                case Types.INTEGER, Types.SMALLINT, Types.TINYINT ->
                        fields.name(safeName).type().nullable().intType().noDefault();
                case Types.BIGINT ->
                        fields.name(safeName).type().nullable().longType().noDefault();
                case Types.FLOAT, Types.REAL, Types.DOUBLE, Types.NUMERIC, Types.DECIMAL ->
                        fields.name(safeName).type().nullable().doubleType().noDefault();
                case Types.BOOLEAN, Types.BIT ->
                        fields.name(safeName).type().nullable().booleanType().noDefault();
                case Types.TIMESTAMP, Types.TIMESTAMP_WITH_TIMEZONE ->
                        fields.name(safeName).type().nullable().longBuilder()
                                .prop("logicalType", "timestamp-millis").endLong().noDefault();
                case Types.DATE ->
                        fields.name(safeName).type().nullable().intBuilder()
                                .prop("logicalType", "date").endInt().noDefault();
                default ->
                        fields.name(safeName).type().nullable().stringType().noDefault();
            }
        }

        return fields.endRecord();
    }

    public String normalizeName(String rawName) {
        String safe = rawName.toLowerCase().replaceAll("[^a-z0-9_]", "_");
        if (Character.isDigit(safe.charAt(0))) {
            return "_" + safe;
        }
        return safe;
    }

    private int sqlType(ResultSetMetaData meta, int i) throws SQLException {
        return meta.getColumnType(i);
    }
}
