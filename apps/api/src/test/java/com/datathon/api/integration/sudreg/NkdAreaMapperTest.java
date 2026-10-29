package com.datathon.api.integration.sudreg;

import static org.assertj.core.api.Assertions.assertThat;

import com.datathon.api.fundingcall.Area;
import org.junit.jupiter.api.Test;

class NkdAreaMapperTest {

    @Test
    void agricultureDivisionGivesAgriculture() {
        assertThat(NkdAreaMapper.forActivityCode("01.50")).containsExactly(Area.AGRICULTURE, Area.EMPLOYMENT);
    }

    @Test
    void manufacturingDivisionGivesEntrepreneurshipAndInnovation() {
        assertThat(NkdAreaMapper.forActivityCode("10.72"))
                .containsExactly(Area.ENTREPRENEURSHIP, Area.INNOVATION, Area.EMPLOYMENT);
    }

    @Test
    void electricityDivisionGivesEnergy() {
        assertThat(NkdAreaMapper.forActivityCode("35.11")).containsExactly(Area.ENERGY, Area.EMPLOYMENT);
    }

    @Test
    void constructionDivisionGivesEntrepreneurshipAndEnergy() {
        assertThat(NkdAreaMapper.forActivityCode("41.20"))
                .containsExactly(Area.ENTREPRENEURSHIP, Area.ENERGY, Area.EMPLOYMENT);
    }

    @Test
    void transportDivisionGivesTransport() {
        assertThat(NkdAreaMapper.forActivityCode("49.41")).containsExactly(Area.TRANSPORT, Area.EMPLOYMENT);
    }

    @Test
    void accommodationDivisionGivesTourism() {
        assertThat(NkdAreaMapper.forActivityCode("55.10")).containsExactly(Area.TOURISM, Area.EMPLOYMENT);
    }

    @Test
    void softwareDivisionGivesDigitalizationAndInnovation() {
        assertThat(NkdAreaMapper.forActivityCode("62.01"))
                .containsExactly(Area.DIGITALIZATION, Area.INNOVATION, Area.EMPLOYMENT);
    }

    @Test
    void healthDivisionGivesHealth() {
        assertThat(NkdAreaMapper.forActivityCode("86.21")).containsExactly(Area.HEALTH, Area.EMPLOYMENT);
    }

    @Test
    void divisionOutsideTheTableFallsBackToEntrepreneurship() {
        assertThat(NkdAreaMapper.forActivityCode("99.00")).containsExactly(Area.ENTREPRENEURSHIP, Area.EMPLOYMENT);
    }

    @Test
    void companyWithoutActivityCodeGetsEntrepreneurshipAndEmployment() {
        assertThat(NkdAreaMapper.forActivityCode(null)).containsExactly(Area.ENTREPRENEURSHIP, Area.EMPLOYMENT);
    }
}
