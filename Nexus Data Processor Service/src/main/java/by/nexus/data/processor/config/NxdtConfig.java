package by.nexus.data.processor.config;

import com.github.baibeicha.nexus.io.sql.DynamicAvroSchemaGenerator;
import com.github.baibeicha.nexus.io.sql.JdbcToAvroConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NxdtConfig {

    @Bean
    public DynamicAvroSchemaGenerator dynamicAvroSchemaGenerator() {
        return new DynamicAvroSchemaGenerator();
    }

    @Bean
    public JdbcToAvroConverter jdbcToAvroConverter() {
        return new JdbcToAvroConverter();
    }
}
