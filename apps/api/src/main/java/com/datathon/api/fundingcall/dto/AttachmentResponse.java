package com.datathon.api.fundingcall.dto;

import com.datathon.api.fundingcall.Attachment;

public record AttachmentResponse(String name, String url) {

    public static AttachmentResponse from(Attachment attachment) {
        return new AttachmentResponse(attachment.getName(), attachment.getUrl());
    }
}
