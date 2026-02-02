package by.nexus.data.processor.service;

import by.nexus.data.processor.event.ImportRequestEvent;
import by.nexus.data.processor.service.impl.NxdtDataExtractionService;
import by.nexus.data.processor.util.DynamicAvroSchemaGenerator;
import by.nexus.data.processor.util.JdbcToAvroConverter;
import by.nexus.data.processor.util.ParquetIo;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.hadoop.conf.Configuration;
import org.apache.parquet.avro.AvroParquetReader;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.parquet.io.InputFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DataExtractionServiceTest {

    private DataExtractionService service;

    @TempDir
    java.nio.file.Path tempStorageDir;

    private static final String DB_URL = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL";
    private static final String DB_USER = "sa";
    private static final String DB_PASS = "";

    @BeforeEach
    void setUp() throws Exception {
        service = new NxdtDataExtractionService(
                new DynamicAvroSchemaGenerator(),
                new JdbcToAvroConverter()
        );

        ReflectionTestUtils.setField(service, "storagePath", tempStorageDir.toString());

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE TABLE IF NOT EXISTS test_users (" +
                    "id INT PRIMARY KEY, " +
                    "username VARCHAR(255), " +
                    "salary DECIMAL(10, 2), " +
                    "is_active BOOLEAN, " +
                    "tags VARCHAR ARRAY, " +
                    "created_at TIMESTAMP)");

            stmt.execute("TRUNCATE TABLE test_users");

            stmt.execute("INSERT INTO test_users VALUES " +
                    "(1, 'Alice', 5000.50, true, ARRAY['admin', 'staff'], '2024-01-01 10:00:00'), " +
                    "(2, 'Bob', 3000.00, false, ARRAY['guest'], '2024-01-02 12:30:00')");
        }
    }

    @Test
    void testExtractAndConvert_ShouldCreateParquetFile() throws Exception {
        String jobId = UUID.randomUUID().toString();
        ImportRequestEvent event = new ImportRequestEvent(
                jobId,
                "1",
                DB_URL,
                DB_USER,
                DB_PASS,
                "SELECT * FROM test_users ORDER BY id"
        );

        File resultFile = service.extractAndConvert(event);

        assertNotNull(resultFile);
        assertTrue(resultFile.exists());
        assertTrue(resultFile.length() > 0, "Файл не должен быть пустым");
        assertTrue(resultFile.getName().endsWith(".nxdt"));

        List<GenericRecord> records = readParquetFile(resultFile);

        assertEquals(2, records.size(), "Должно быть ровно 2 записи");

        GenericRecord record1 = records.getFirst();
        List<?> tags = (List<?>) record1.get("tags");
        assertEquals(2, tags.size());
        assertEquals("admin", tags.getFirst().toString());
        assertEquals(1, record1.get("id")); // Avro int -> Java int
        assertEquals("Alice", record1.get("username").toString()); // Avro Utf8 -> String
        assertEquals(5000.5, record1.get("salary"));
        assertEquals(true, record1.get("is_active"));

        GenericRecord record2 = records.get(1);
        assertEquals(2, record2.get("id"));
        assertEquals("Bob", record2.get("username").toString());
        assertEquals(false, record2.get("is_active"));
    }

    private List<GenericRecord> readParquetFile(File file) throws Exception {
        List<GenericRecord> records = new ArrayList<>();

        InputFile inputFile = new ParquetIo.NioInputFile(file.toPath());

        try (ParquetReader<GenericRecord> reader = AvroParquetReader.<GenericRecord>builder(inputFile)
                .withDataModel(GenericData.get())
                .withConf(new Configuration())
                .build()) {

            GenericRecord record;
            while ((record = reader.read()) != null) {
                records.add(record);
            }
        }
        return records;
    }
}
