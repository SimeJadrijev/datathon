package com.datathon.api.association;

import static org.assertj.core.api.Assertions.assertThat;

import com.datathon.api.fundingcall.Area;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import tools.jackson.databind.json.JsonMapper;

class AssociationRegistryParserTest {

    @Test
    void keepsOnlyActiveAssociationsWithAValidOib() throws IOException {
        List<AssociationRecord> records = parse();

        assertThat(records).extracting(AssociationRecord::oib).containsExactly("42128339233", "11111111119");
    }

    @Test
    void readsNameSeatCountyAndAreasFromTheGoals() throws IOException {
        AssociationRecord sportsClub = parse().get(1);

        assertThat(sportsClub.name()).isEqualTo("Sportski klub Mladost");
        assertThat(sportsClub.seat()).isEqualTo("Jarunska 5, Zagreb");
        assertThat(sportsClub.county()).isEqualTo("Grad Zagreb");
        assertThat(sportsClub.registryNumber()).isEqualTo("21000001");
        assertThat(sportsClub.areas()).contains(Area.SPORT);
        assertThat(sportsClub.toEntity().municipality()).isEqualTo("Zagreb");
    }

    @Test
    void teachersAssociationIsAboutEducation() throws IOException {
        assertThat(parse().getFirst().areas()).contains(Area.EDUCATION);
    }

    private static List<AssociationRecord> parse() throws IOException {
        List<AssociationRecord> records = new ArrayList<>();
        try (InputStream json = new ClassPathResource("fixtures/association-registry/register.json").getInputStream()) {
            AssociationRegistryParser.parseActive(json, JsonMapper.builder().build(), records::add);
        }
        return records;
    }
}
