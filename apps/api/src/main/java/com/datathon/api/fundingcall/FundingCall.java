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

    /** Value in {@link #counties} meaning the call is open to the whole country. */
    public static final String ALL_COUNTIES = "ALL";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // source + externalId is the natural key, so re-imports update instead of duplicating
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

    /** Free text when there is no fixed date, for example "do utroška sredstava". */
    @Column(length = 500)
    private String deadlineNote;

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

    /** NKD activity codes, for example "62.01". */
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

    // Raw JSON array written by AI-4. Kept as text so the model does not depend on
    // Hibernate's JSON mapper (Jackson 2 vs 3 on the classpath); AI-4 owns its shape.
    @Column(columnDefinition = "TEXT")
    private String eligibilityQuestions;

    /** SHA-256 of the source text; ingest updates a record only when it changes. */
    @Column(length = 64)
    private String rawTextHash;

    private boolean extractedByLlm;

    /** Names of the fields that were filled by the LLM, so the UI can label them "AI". */
    @ElementCollection
    @CollectionTable(name = "funding_call_ai_field", joinColumns = @JoinColumn(name = "funding_call_id"))
    @Column(name = "field_name", length = 50)
    private Set<String> aiFields = new HashSet<>();

    private boolean fieldsReviewed;

    private boolean hidden;

    @Column(length = 500)
    private String hiddenReason;

    /** Id of the funding call this one duplicates (from another source). */
    private Long duplicateOf;

    private Instant fetchedAt;

    /** Raw page and PDF text used for extraction and the LLM. Never sent by the API. */
    @Column(columnDefinition = "TEXT")
    private String sourceText;

    public FundingCall(SourceCode source, String externalId, String title) {
        this.source = source;
        this.externalId = externalId;
        this.title = title;
    }
}
