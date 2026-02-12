package by.nexus.data.processor.service.impl;

import by.nexus.data.processor.service.DataExtractionService;
import by.nexus.data.processor.event.ImportRequestEvent;
import com.github.baibeicha.nexus.io.format.nxdt.NxdtWriter;
import com.github.baibeicha.nexus.io.sql.DynamicAvroSchemaGenerator;
import com.github.baibeicha.nexus.io.sql.JdbcToAvroConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.hadoop.conf.Configuration;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
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

        try (Connection conn = DriverManager.getConnection(
                event.connectionUrl(),
                event.username(),
                event.password());
             Statement stmt = conn.createStatement()) {

            stmt.setFetchSize(1000);

            try (ResultSet rs = stmt.executeQuery(event.sqlQuery())) {
                ResultSetMetaData metaData = rs.getMetaData();
                Schema schema = schemaGenerator.generate(metaData);

                try (NxdtWriter writer = new NxdtWriter(targetFile.toPath(), schema)) {
                    int columnCount = metaData.getColumnCount();

                    while (rs.next()) {
                        GenericRecord record = writer.createRecord();

                        for (int i = 1; i <= columnCount; i++) {
                            record.put(
                                    DynamicAvroSchemaGenerator.normalizeName(metaData.getColumnName(i)),
                                    jdbcToAvroConverter.convert(rs, i, metaData.getColumnType(i))
                            );
                        }

                        writer.writeRecord(record);
                    }
                }
            }
        }

        return targetFile;
    }
}
