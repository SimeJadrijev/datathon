package com.datathon.api.ingest.source;

import static com.datathon.api.ingest.source.SourceFixtures.json;
import static com.datathon.api.ingest.source.SourceFixtures.text;
import static org.assertj.core.api.Assertions.assertThat;

import com.datathon.api.fundingcall.FundingCall;
import com.datathon.api.fundingcall.TargetGroup;
import com.datathon.api.ingest.RawItem;
import com.datathon.api.ingest.source.ApprrrParser.FeedEntry;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ApprrrParserTest {

    private List<RawItem> posts;

    @BeforeEach
    void parseFixture() {
        posts = ApprrrParser.parsePosts(json("apprrr/posts.json"));
    }

    @Test
    void parsesPost() {
        RawItem post = posts.getFirst();

        assertThat(post.externalId()).isEqualTo("43887");
        assertThat(post.url()).startsWith("https://www.apprrr.hr/");
        assertThat(post.title()).contains("intervencije 73.10");
        assertThat(post.provider()).startsWith("Agencija za plaćanja u poljoprivredi");
        assertThat(post.publishedAt()).isEqualTo(LocalDate.of(2026, 9, 24));
        assertThat(post.targetGroupsHint()).containsExactly(TargetGroup.FARMER);
        assertThat(post.countiesHint()).containsExactly(FundingCall.ALL_COUNTIES);
        assertThat(post.text()).isNotBlank();
    }

    @Test
    void postLinksToTheInterventionPageThatHoldsTheDocuments() {
        RawItem post = posts.getFirst();

        assertThat(post.attachments()).isEmpty();
        assertThat(ApprrrParser.interventionPage(post)).hasValueSatisfying(
                page -> assertThat(page).startsWith("https://www.apprrr.hr/intervencije/"));
    }

    @Test
    void postWithoutInterventionLinkHasNoPageToFetch() {
        assertThat(ApprrrParser.interventionPage(posts.get(2))).isEmpty();
    }

    @Test
    void documentsAreReadFromTheInterventionPage() {
        RawItem withDocuments = ApprrrParser.applyInterventionPage(posts.getFirst(), text("apprrr/intervention.html"));

        assertThat(withDocuments.attachments()).hasSize(6)
                .allSatisfy(attachment -> assertThat(attachment.url()).matches("(?i).*\\.(zip|pdf|docx?|xlsx?)"));
    }

    @Test
    void interventionPageDoesNotReplaceDocumentsTheFinePostAlreadyHas() {
        RawItem alreadyHasDocument = posts.getFirst().toBuilder()
                .attachments(List.of(new com.datathon.api.ingest.RawAttachment("Poziv", "https://example.hr/poziv.pdf")))
                .build();

        RawItem result = ApprrrParser.applyInterventionPage(alreadyHasDocument, text("apprrr/intervention.html"));

        assertThat(result.attachments()).hasSize(1);
    }

    @Test
    void feedHoldsOnlyTheLatestPostsAndTheirIds() {
        List<FeedEntry> entries = ApprrrParser.parseFeed(text("apprrr/feed.xml"));

        assertThat(entries).hasSize(10);
        assertThat(entries).allSatisfy(entry -> assertThat(entry.id()).matches("\\d+"));
        assertThat(entries).extracting(FeedEntry::title).anyMatch(title -> title.startsWith("Najava natječaja"));
    }
}
