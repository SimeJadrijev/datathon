package com.datathon.api.fundingcall;

import com.datathon.api.source.SourceCode;
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
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "funding_call",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_funding_call_source_external_id",
                columnNames = {"source", "external_id"}))
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FundingCall {

    public static final String ALL_COUNTIES = "ALL";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private SourceCode source;

    @Column(name = "external_id", nullable = false, length = 200)
    private String externalId;

    @Column(length = 2000)
    private String url;

    @Column(nullable = false, length = 1000)
    private String title;

    @Column(length = 500)
    private String provider;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private FundingCallType type;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private FundingCallStatus status;

    private LocalDate publishedAt;

    private LocalDate deadline;

    @Column(length = 500)
    private String deadlineNote;

    @Column(columnDefinition = "boolean not null default false")
    private boolean deadlineFromSource;

    @Column(precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(precision = 15, scale = 2)
    private BigDecimal minAmount;

    @Column(precision = 15, scale = 2)
    private BigDecimal maxAmount;

    @ElementCollection
    @CollectionTable(name = "funding_call_target_group", joinColumns = @JoinColumn(name = "funding_call_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "target_group", length = 30)
    private Set<TargetGroup> targetGroups = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "funding_call_area", joinColumns = @JoinColumn(name = "funding_call_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "area", length = 30)
    private Set<Area> areas = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "funding_call_county", joinColumns = @JoinColumn(name = "funding_call_id"))
    @Column(name = "county", length = 100)
    private Set<String> counties = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "funding_call_activity_code", joinColumns = @JoinColumn(name = "funding_call_id"))
    @Column(name = "activity_code", length = 10)
    private Set<String> activityCodes = new HashSet<>();

    @Column(columnDefinition = "TEXT")
    private String conditionsSummary;

    @ElementCollection
    @CollectionTable(name = "funding_call_required_document", joinColumns = @JoinColumn(name = "funding_call_id"))
    @OrderColumn(name = "position")
    @Column(name = "document", length = 1000)
    private List<String> requiredDocuments = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "funding_call_attachment", joinColumns = @JoinColumn(name = "funding_call_id"))
    @OrderColumn(name = "position")
    private List<Attachment> attachments = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "funding_call_legal_reference", joinColumns = @JoinColumn(name = "funding_call_id"))
    @OrderColumn(name = "position")
    private List<LegalReference> legalReferences = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String eligibilityQuestions;

    @Column(length = 64)
    private String rawTextHash;

    private boolean extractedByLlm;

    @ElementCollection
    @CollectionTable(name = "funding_call_ai_field", joinColumns = @JoinColumn(name = "funding_call_id"))
    @Column(name = "field_name", length = 50)
    private Set<String> aiFields = new HashSet<>();

    private boolean fieldsReviewed;

    private boolean hidden;

    @Column(length = 500)
    private String hiddenReason;

    private Long duplicateOf;

    private Instant fetchedAt;

    private Instant firstSeenAt;

    private Instant contentChangedAt;

    @Column(columnDefinition = "TEXT")
    private String sourceText;

    public FundingCall(SourceCode source, String externalId, String title) {
        this.source = source;
        this.externalId = externalId;
        this.title = title;
    }
}
