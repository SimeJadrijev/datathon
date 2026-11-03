package com.datathon.api.common;

import jakarta.persistence.EntityManagerFactory;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EnumCheckConstraintCleaner implements InitializingBean {

    private static final String FIND_ENUM_CHECKS = """
            select conrelid::regclass::text as table_name, conname
            from pg_constraint
            where contype = 'c'
              and connamespace = 'public'::regnamespace
              and pg_get_constraintdef(oid) like '%= ANY ((ARRAY[%'
            """;

    private final JdbcTemplate jdbcTemplate;

    public EnumCheckConstraintCleaner(JdbcTemplate jdbcTemplate, EntityManagerFactory schemaReady) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void afterPropertiesSet() {
        List<String[]> constraints = jdbcTemplate.query(FIND_ENUM_CHECKS,
                (row, index) -> new String[] {row.getString("table_name"), row.getString("conname")});
        for (String[] constraint : constraints) {
            jdbcTemplate.execute("alter table " + constraint[0] + " drop constraint if exists " + constraint[1]);
        }
        if (!constraints.isEmpty()) {
            log.info("Dropped {} enum check constraints so new enum values can be stored", constraints.size());
        }
    }
}
