package com.datathon.api.seed;

import com.datathon.api.fundingcall.Attachment;

public record SeedAttachment(String name, String url) {

    Attachment toAttachment() {
        return new Attachment(name, url);
    }
}
