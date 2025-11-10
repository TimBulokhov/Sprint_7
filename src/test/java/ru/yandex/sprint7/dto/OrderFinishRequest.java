package ru.yandex.sprint7.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OrderFinishRequest {
    @JsonProperty("id")
    private int id;

    public OrderFinishRequest() {
    }

    public OrderFinishRequest(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}

