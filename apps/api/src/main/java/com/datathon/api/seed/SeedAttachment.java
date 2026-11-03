package com.datathon.api.seed;

import com.datathon.api.fundingcall.Attachment;

public record SeedAttachment(String name, String url) {

    static SeedAttachment from(Attachment attachment) {
        return new SeedAttachment(attachment.getName(), attachment.getUrl());
    }

    Attachment toAttachment() {
        return new Attachment(name, url);
    }
}
