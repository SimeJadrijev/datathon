package com.datathon.api.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.datathon.api.association.Association;
import com.datathon.api.association.AssociationRecord;
import com.datathon.api.association.AssociationRepository;
import com.datathon.api.common.NotFoundException;
import com.datathon.api.fundingcall.Area;
import com.datathon.api.fundingcall.TargetGroup;
import com.datathon.api.integration.sudreg.CompanyInfo;
import com.datathon.api.integration.sudreg.FixtureSudregClient;
import com.datathon.api.integration.sudreg.SudregClient;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(OutputCaptureExtension.class)
class ProfileFromOibServiceTest {

    private static final AssociationRepository NO_ASSOCIATIONS = mock(AssociationRepository.class);

    private static final String MICROBLINK = "21173725829";
    private static final String VALAMAR = "36201212847";
    private static final String KRAS = "94989605030";
    private static final String VALID_BUT_UNKNOWN = "12345678903";

    private final ProfileFromOibService service =
            new ProfileFromOibService(new FixtureSudregClient(JsonMapper.builder().build()), NO_ASSOCIATIONS);

    @Test
    void itCompanyFromZagrebGetsDigitalAreasAndItsActivityCode() {
        var response = service.fromOib(MICROBLINK);

        assertThat(response.companyName()).startsWith("Microblink");
        assertThat(response.seat()).isEqualTo(new OibProfileResponse.Seat("Zagreb", "Grad Zagreb"));
        assertThat(response.activityCode()).isEqualTo("62.01");
        assertThat(response.source()).isEqualTo("sudreg");
        assertThat(response.profile().type()).isEqualTo(TargetGroup.COMPANY);
        assertThat(response.profile().areas()).containsExactly(Area.DIGITALIZATION, Area.INNOVATION, Area.EMPLOYMENT);
        assertThat(response.profile().county()).isEqualTo("Grad Zagreb");
        assertThat(response.profile().activityCodes()).containsExactly("62.01");
    }

    @Test
    void tourismCompanyFromIstriaGetsTourismAreas() {
        var response = service.fromOib(VALAMAR);

        assertThat(response.seat().county()).isEqualTo("Istarska županija");
        assertThat(response.activityCode()).isEqualTo("55.10");
        assertThat(response.profile().type()).isEqualTo(TargetGroup.COMPANY);
        assertThat(response.profile().areas()).containsExactly(Area.TOURISM, Area.EMPLOYMENT);
    }

    @Test
    void foodProducerGetsManufacturingAreas() {
        var response = service.fromOib(KRAS);

        assertThat(response.activityCode()).isEqualTo("10.82");
        assertThat(response.profile().areas()).containsExactly(Area.ENTREPRENEURSHIP, Area.INNOVATION, Area.EMPLOYMENT);
    }

    @Test
    void unknownOibIsNotFound() {
        assertThatThrownBy(() -> service.fromOib(VALID_BUT_UNKNOWN)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void oibIsNeverWrittenToTheLog(CapturedOutput output) {
        service.fromOib(MICROBLINK);
        assertThatThrownBy(() -> service.fromOib(VALID_BUT_UNKNOWN)).isInstanceOf(NotFoundException.class);

        assertThat(output.getAll()).doesNotContain(MICROBLINK).doesNotContain(VALID_BUT_UNKNOWN);
    }

    @Test
    void legalFormDecidesTheTargetGroup() {
        assertThat(profileFor("d.o.o.", "62.01").type()).isEqualTo(TargetGroup.COMPANY);
        assertThat(profileFor("j.d.o.o.", "62.01").type()).isEqualTo(TargetGroup.COMPANY);
        assertThat(profileFor("zadruga", "01.11").type()).isEqualTo(TargetGroup.COMPANY);
        assertThat(profileFor("udruga", "94.99").type()).isEqualTo(TargetGroup.ASSOCIATION);
        assertThat(profileFor("ustanova", "85.20").type()).isEqualTo(TargetGroup.LOCAL_GOVERNMENT);
        assertThat(profileFor(null, "62.01").type()).isEqualTo(TargetGroup.COMPANY);
    }

    @Test
    void companyWithoutActivityCodeGetsEntrepreneurshipAndEmployment() {
        var profile = profileFor("d.o.o.", null);

        assertThat(profile.areas()).containsExactly(Area.ENTREPRENEURSHIP, Area.EMPLOYMENT);
        assertThat(profile.activityCodes()).isEmpty();
    }

    @Test
    void activityCodeFromAnUnmappedDivisionFallsBackToEntrepreneurship() {
        assertThat(profileFor("d.o.o.", "84.11").areas()).containsExactly(Area.ENTREPRENEURSHIP, Area.EMPLOYMENT);
    }

    private static ProfileRequest profileFor(String legalForm, String activityCode) {
        var company = new CompanyInfo("Test d.o.o.", new CompanyInfo.Seat("Zagreb", "Grad Zagreb"), legalForm, activityCode);
        return new ProfileFromOibService(oib -> company, NO_ASSOCIATIONS).fromOib("00000000000").profile();
    }

    @Test
    void associationFromTheRegisterGetsAnAssociationProfileWithoutAskingSudreg() {
        Association association = new AssociationRecord("42128339233", "Udruga studenata informatike", "Ulica 1, Split",
                "Splitsko-dalmatinska", "17000001", Set.of(Area.EDUCATION, Area.DIGITALIZATION)).toEntity();
        AssociationRepository associations = mock(AssociationRepository.class);
        when(associations.findByOib("42128339233")).thenReturn(Optional.of(association));
        SudregClient sudreg = oib -> {
            throw new AssertionError("Sudreg must not be called for a registered association");
        };

        OibProfileResponse response = new ProfileFromOibService(sudreg, associations).fromOib("42128339233");

        assertThat(response.source()).isEqualTo("registar-udruga");
        assertThat(response.companyName()).isEqualTo("Udruga studenata informatike");
        assertThat(response.seat().municipality()).isEqualTo("Split");
        assertThat(response.profile().type()).isEqualTo(TargetGroup.ASSOCIATION);
        assertThat(response.profile().county()).isEqualTo("Splitsko-dalmatinska");
        assertThat(response.profile().areas()).containsExactly(Area.EDUCATION, Area.DIGITALIZATION);
    }
}
