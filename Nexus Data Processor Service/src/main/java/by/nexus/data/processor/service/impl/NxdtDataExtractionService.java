package by.nexus.data.processor.service.impl;

import by.nexus.data.processor.service.DataExtractionService;
import by.nexus.data.processor.event.ImportRequestEvent;
import by.nexus.data.processor.util.DynamicAvroSchemaGenerator;
import by.nexus.data.processor.util.JdbcToAvroConverter;
import by.nexus.data.processor.util.ParquetIo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.hadoop.conf.Configuration;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.apache.parquet.io.OutputFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

@Slf4j
@Service
@RequiredArgsConstructor
public class NxdtDataExtractionService implements DataExtractionService {

    @Value("${nexus.storage.path}")
    private String storagePath;

    private final DynamicAvroSchemaGenerator schemaGenerator;
    private final JdbcToAvroConverter jdbcToAvroConverter;

    /**
     * Connect -> Extract -> Write Parquet -> Return File Path
     */
    public File extractAndConvert(ImportRequestEvent event) throws Exception {
        Path storageDir = Paths.get(storagePath);
        if (!Files.exists(storageDir)) {
            Files.createDirectories(storageDir);
        }

        String filename = "job_" + event.jobId() + ".nxdt";
        File targetFile = storageDir.resolve(filename).toFile();

        if (targetFile.exists()) {
            targetFile.delete();
        }

        OutputFile outputFile = new ParquetIo.NioOutputFile(targetFile.toPath());

        try (Connection conn = DriverManager.getConnection(
                event.connectionUrl(),
                event.username(),
                event.password());
             Statement stmt = conn.createStatement()) {

            stmt.setFetchSize(1000);

            try (ResultSet rs = stmt.executeQuery(event.sqlQuery())) {
                ResultSetMetaData metaData = rs.getMetaData();

                Schema schema = schemaGenerator.generate(metaData);
                log.debug("Generated Avro Schema: {}", schema.toString(true));

                try (ParquetWriter<GenericRecord> writer = AvroParquetWriter.<GenericRecord>builder(outputFile)
                        .withSchema(schema)
                        .withCompressionCodec(CompressionCodecName.SNAPPY)

                        .withRowGroupSize(16L * 1024 * 1024) // 16MB блок
                        .withPageSize(1024 * 1024)           // 1MB страница

                        .withDataModel(GenericData.get())
                        .withConf(new Configuration())
                        .build()) {

                    int columnCount = metaData.getColumnCount();

                    while (rs.next()) {
                        GenericRecord record = new GenericData.Record(schema);

                        for (int i = 1; i <= columnCount; i++) {
                            String columnName = metaData.getColumnName(i);
                            String safeName = schemaGenerator.normalizeName(columnName);
                            int sqlType = metaData.getColumnType(i);

                            Object val = jdbcToAvroConverter.convert(rs, i, sqlType);
                            record.put(safeName, val);
                        }

                        writer.write(record);
                    }
                }
            }
        }

        return targetFile;
    }
}
