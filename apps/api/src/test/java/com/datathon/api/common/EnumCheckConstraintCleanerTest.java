package com.datathon.api.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class EnumCheckConstraintCleanerTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @BeforeEach
    void createTableWithStaleEnumCheck() {
        jdbcTemplate.execute("create table enum_check_probe (code varchar(20) check (code in ('OLD_A', 'OLD_B')),"
                + " position int check (position >= 0))");
    }

    @Test
    void staleEnumCheckRejectsNewValueBeforeCleaning() {
        assertThatThrownBy(() -> jdbcTemplate.update("insert into enum_check_probe (code) values ('NEW_C')"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void newEnumValueCanBeStoredAfterCleaningAndOtherChecksStay() {
        new EnumCheckConstraintCleaner(jdbcTemplate, entityManagerFactory).afterPropertiesSet();

        jdbcTemplate.update("insert into enum_check_probe (code) values ('NEW_C')");

        assertThat(jdbcTemplate.queryForObject("select count(*) from enum_check_probe", Integer.class)).isEqualTo(1);
        assertThatThrownBy(() -> jdbcTemplate.update("insert into enum_check_probe (position) values (-1)"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
