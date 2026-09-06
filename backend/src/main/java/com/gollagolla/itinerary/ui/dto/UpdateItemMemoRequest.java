package com.gollagolla.itinerary.ui.dto;

import lombok.Getter;

@Getter
public class UpdateItemMemoRequest {

    private String memo;

    private UpdateItemMemoRequest() {}

    public UpdateItemMemoRequest(String memo) {
        this.memo = memo;
    }
}
