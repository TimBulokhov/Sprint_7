package ru.yandex.sprint7.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OrderCancelRequest {
    @JsonProperty("track")
    private int track;

    public OrderCancelRequest() {
    }

    public OrderCancelRequest(int track) {
        this.track = track;
    }

    public int getTrack() {
        return track;
    }

    public void setTrack(int track) {
        this.track = track;
    }
}

