package com.datathon.api.fundingcall;

import com.datathon.api.fundingcall.dto.FundingCallSummary;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AtomFeedWriter {

    private static final String ATOM = "http://www.w3.org/2005/Atom";
    private static final Locale CROATIAN = Locale.forLanguageTag("hr-HR");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("d. M. yyyy.", CROATIAN);

    private final String publicUrl;

    public AtomFeedWriter(@Value("${app.public-url}") String publicUrl) {
        this.publicUrl = publicUrl.replaceAll("/+$", "");
    }

    public String write(List<FundingCallSummary> fundingCalls, String selfUrl, Instant generatedAt) {
        StringWriter output = new StringWriter();
        try {
            XMLStreamWriter xml = XMLOutputFactory.newFactory().createXMLStreamWriter(output);
            xml.writeStartDocument("UTF-8", "1.0");
            xml.writeStartElement("feed");
            xml.writeDefaultNamespace(ATOM);
            element(xml, "id", selfUrl);
            element(xml, "title", "OtvoreniRadar: novi natječaji");
            element(xml, "subtitle", "Natječaji i javni pozivi prikupljeni iz javnih izvora, najnoviji prvi.");
            element(xml, "updated", latest(fundingCalls, generatedAt).toString());
            link(xml, "self", selfUrl);
            link(xml, "alternate", publicUrl + "/natjecaji");
            for (FundingCallSummary fundingCall : fundingCalls) {
                entry(xml, fundingCall, generatedAt);
            }
            xml.writeEndElement();
            xml.writeEndDocument();
            xml.close();
        } catch (XMLStreamException e) {
            throw new IllegalStateException("Atom feed could not be written", e);
        }
        return output.toString();
    }

    private void entry(XMLStreamWriter xml, FundingCallSummary fundingCall, Instant generatedAt) throws XMLStreamException {
        String detailUrl = publicUrl + "/natjecaji/" + fundingCall.id();
        Instant seenAt = fundingCall.firstSeenAt() != null ? fundingCall.firstSeenAt() : generatedAt;
        xml.writeStartElement("entry");
        element(xml, "id", detailUrl);
        element(xml, "title", fundingCall.title());
        element(xml, "updated", seenAt.toString());
        element(xml, "published", seenAt.toString());
        link(xml, "alternate", detailUrl);
        if (fundingCall.url() != null) {
            link(xml, "related", fundingCall.url());
        }
        xml.writeStartElement("author");
        element(xml, "name", fundingCall.provider() != null ? fundingCall.provider() : fundingCall.source().name());
        xml.writeEndElement();
        element(xml, "summary", summary(fundingCall));
        xml.writeEndElement();
    }

    private static String summary(FundingCallSummary fundingCall) {
        List<String> parts = new ArrayList<>();
        parts.add(fundingCall.provider());
        parts.add(fundingCall.deadline() != null ? "rok " + DATE.format(fundingCall.deadline()) : fundingCall.deadlineNote());
        parts.add(amount(fundingCall.maxAmount() != null ? fundingCall.maxAmount() : fundingCall.totalAmount()));
        parts.add("izvor: " + fundingCall.source().name());
        return String.join(" · ", parts.stream().filter(Objects::nonNull).toList());
    }

    private static String amount(BigDecimal value) {
        if (value == null) {
            return null;
        }
        NumberFormat euros = NumberFormat.getCurrencyInstance(CROATIAN);
        euros.setMaximumFractionDigits(0);
        return "do " + euros.format(value);
    }

    private static Instant latest(List<FundingCallSummary> fundingCalls, Instant fallback) {
        return fundingCalls.stream()
                .map(FundingCallSummary::firstSeenAt)
                .filter(Objects::nonNull)
                .max(Instant::compareTo)
                .orElse(fallback);
    }

    private static void element(XMLStreamWriter xml, String name, String text) throws XMLStreamException {
        xml.writeStartElement(name);
        xml.writeCharacters(text == null ? "" : text);
        xml.writeEndElement();
    }

    private static void link(XMLStreamWriter xml, String relation, String href) throws XMLStreamException {
        xml.writeEmptyElement("link");
        xml.writeAttribute("rel", relation);
        xml.writeAttribute("href", href);
    }
}
