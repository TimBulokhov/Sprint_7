package ru.yandex.sprint7.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CourierLoginRequest {
    @JsonProperty("login")
    private String login;

    @JsonProperty("password")
    private String password;

    public CourierLoginRequest() {
    }

    public CourierLoginRequest(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

