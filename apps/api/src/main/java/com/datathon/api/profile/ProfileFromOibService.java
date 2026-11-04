package com.datathon.api.profile;

import com.datathon.api.association.Association;
import com.datathon.api.association.AssociationRepository;
import com.datathon.api.fundingcall.TargetGroup;
import com.datathon.api.integration.sudreg.CompanyInfo;
import com.datathon.api.integration.sudreg.NkdAreaMapper;
import com.datathon.api.integration.sudreg.SudregClient;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileFromOibService {

    private final SudregClient sudregClient;
    private final AssociationRepository associationRepository;

    @Transactional(readOnly = true)
    public OibProfileResponse fromOib(String oib) {
        Optional<Association> association = associationRepository.findByOib(oib);
        if (association.isPresent()) {
            return fromAssociation(association.get());
        }
        CompanyInfo company = sudregClient.findByOib(oib);
        String county = company.seat().county();
        var profile = new ProfileRequest(
                targetGroupFor(company.legalForm()),
                List.of(),
                NkdAreaMapper.forActivityCode(company.activityCode()),
                county,
                List.of(),
                company.activityCode() == null ? List.of() : List.of(company.activityCode()));
        return new OibProfileResponse(
                company.name(),
                new OibProfileResponse.Seat(company.seat().municipality(), county),
                company.activityCode(),
                profile,
                OibProfileResponse.SUDREG_SOURCE);
    }

    private static OibProfileResponse fromAssociation(Association association) {
        var profile = new ProfileRequest(
                TargetGroup.ASSOCIATION,
                List.of(),
                association.getAreas().stream().sorted().toList(),
                association.getCounty(),
                List.of(),
                List.of());
        return new OibProfileResponse(
                association.getName(),
                new OibProfileResponse.Seat(association.municipality(), association.getCounty()),
                null,
                profile,
                OibProfileResponse.ASSOCIATION_REGISTRY_SOURCE);
    }

    private static TargetGroup targetGroupFor(String legalForm) {
        String abbreviation = legalForm == null ? "" : legalForm.toLowerCase(Locale.ROOT);
        return switch (abbreviation) {
            case "udruga" -> TargetGroup.ASSOCIATION;
            case "ustanova" -> TargetGroup.LOCAL_GOVERNMENT;
            default -> TargetGroup.COMPANY;
        };
    }
}
