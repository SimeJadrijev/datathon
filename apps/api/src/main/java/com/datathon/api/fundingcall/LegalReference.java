package com.datathon.api.fundingcall;

import com.datathon.api.regulation.GazetteCitationParser;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class LegalReference {

    @Column(length = 300)
    private String citedTitle;

    @Column(length = 20)
    private String gazetteNumber;

    public static List<LegalReference> fromText(String text) {
        return GazetteCitationParser.parse(text).stream()
                .map(citation -> new LegalReference(citation.citedTitle(), citation.gazetteNumber()))
                .toList();
    }
}
