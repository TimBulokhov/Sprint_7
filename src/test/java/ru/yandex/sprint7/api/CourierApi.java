package ru.yandex.sprint7.api;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import ru.yandex.sprint7.constants.Constants;
import ru.yandex.sprint7.dto.CourierDeleteRequest;
import ru.yandex.sprint7.dto.CourierLoginRequest;
import ru.yandex.sprint7.dto.CourierRequest;
import ru.yandex.sprint7.utils.LoggerHelper;

import static io.restassured.RestAssured.given;

public class CourierApi {
    private final RequestSpecification requestSpec;

    public CourierApi(RequestSpecification requestSpec) {
        this.requestSpec = requestSpec;
    }

    @Step("Создать курьера")
    public Response createCourier(String login, String password, String firstName) {
        CourierRequest courierRequest = new CourierRequest(login, password, firstName);
        
        LoggerHelper.logRequest("POST", Constants.COURIER_PATH, "CourierRequest{login=" + login + ", firstName=" + firstName + "}");
        LoggerHelper.logInfo("Используемые данные: login={}, firstName={}", login, firstName);

        Response response = given()
                .spec(requestSpec)
                .body(courierRequest)
                .when()
                .post(Constants.COURIER_PATH);

        LoggerHelper.logResponse(response);
        return response;
    }

    @Step("Авторизовать курьера")
    public Response loginCourier(String login, String password) {
        CourierLoginRequest loginRequest = new CourierLoginRequest(login, password);
        
        LoggerHelper.logRequest("POST", Constants.COURIER_LOGIN_PATH, "CourierLoginRequest{login=" + login + "}");
        LoggerHelper.logInfo("Используемые данные: login={}", login);

        Response response = given()
                .spec(requestSpec)
                .body(loginRequest)
                .when()
                .post(Constants.COURIER_LOGIN_PATH);

        LoggerHelper.logResponse(response);
        return response;
    }

    @Step("Получить ID курьера")
    public int getCourierId(String login, String password) {
        Response response = loginCourier(login, password);
        int courierId = response.then().extract().path("id");
        LoggerHelper.logInfo("Получен courierId: {}", courierId);
        return courierId;
    }

    @Step("Удалить курьера")
    public Response deleteCourier(int id) {
        String path = Constants.COURIER_PATH + "/" + id;
        CourierDeleteRequest deleteRequest = new CourierDeleteRequest(id);
        
        LoggerHelper.logRequest("DELETE", path, "CourierDeleteRequest{id=" + id + "}");
        LoggerHelper.logInfo("Удаляем курьера с id: {}", id);

        Response response = given()
                .spec(requestSpec)
                .body(deleteRequest)
                .when()
                .delete(path);

        LoggerHelper.logResponse(response);
        return response;
    }
}

