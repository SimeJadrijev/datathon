package com.datathon.api.regulation;

import com.datathon.api.fundingcall.Area;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "regulation")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Regulation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", nullable = false, unique = true, length = 50)
    private String externalId;

    @Column(nullable = false, length = 2000)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private RegulationType type;

    @Column(length = 20)
    private String gazetteNumber;

    private Integer documentNumber;

    private LocalDate publishedAt;

    private LocalDate enactedAt;

    @Column(length = 2000)
    private String url;

    @Column(length = 2000)
    private String eliUrl;

    @Column(length = 2000)
    private String pdfUrl;

    @ElementCollection
    @CollectionTable(name = "regulation_area", joinColumns = @JoinColumn(name = "regulation_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "area", length = 30)
    private Set<Area> areas = new HashSet<>();

    @Column(columnDefinition = "TEXT")
    private String summary;

    private boolean eliMetadata;

    private Instant fetchedAt;

    public Regulation(String externalId, String title) {
        this.externalId = externalId;
        this.title = title;
    }
}
