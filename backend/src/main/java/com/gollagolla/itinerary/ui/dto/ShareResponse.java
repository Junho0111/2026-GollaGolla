package com.gollagolla.itinerary.ui.dto;

import lombok.Getter;

@Getter
public class ShareResponse {
    private String shareToken;
    private String url;

    private ShareResponse() {}

    private ShareResponse(String shareToken, String url) {
        this.shareToken = shareToken;
        this.url = url;
    }

    public static ShareResponse of(String shareToken, String url) {
        return new ShareResponse(shareToken, url);
    }
}
