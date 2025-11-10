package ru.yandex.sprint7.api;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import ru.yandex.sprint7.constants.Constants;
import ru.yandex.sprint7.dto.OrderCancelRequest;
import ru.yandex.sprint7.dto.OrderFinishRequest;
import ru.yandex.sprint7.dto.OrderRequest;
import ru.yandex.sprint7.utils.LoggerHelper;

import java.util.List;

import static io.restassured.RestAssured.given;

public class OrderApi {
    private final RequestSpecification requestSpec;

    public OrderApi(RequestSpecification requestSpec) {
        this.requestSpec = requestSpec;
    }

    @Step("Создать заказ")
    public Response createOrder(String firstName, String lastName, String address,
                               String metroStation, String phone, int rentTime,
                               String deliveryDate, String comment, List<String> color) {
        OrderRequest orderRequest = new OrderRequest(
                firstName, lastName, address, metroStation, phone, rentTime,
                deliveryDate, comment, color
        );

        LoggerHelper.logRequest("POST", Constants.ORDERS_PATH, "OrderRequest{firstName=" + firstName + ", lastName=" + lastName + "}");
        LoggerHelper.logInfo("Используемые данные: firstName={}, lastName={}, metroStation={}, color={}",
                firstName, lastName, metroStation, color);

        Response response = given()
                .spec(requestSpec)
                .body(orderRequest)
                .when()
                .post(Constants.ORDERS_PATH);

        LoggerHelper.logResponse(response);
        return response;
    }

    @Step("Получить трек-номер заказа")
    public int getOrderTrack(Response createOrderResponse) {
        int track = createOrderResponse.then().extract().path("track");
        LoggerHelper.logInfo("Получен track: {}", track);
        return track;
    }

    @Step("Получить заказ по трек-номеру")
    public Response getOrderByTrack(int track) {
        String path = Constants.ORDERS_TRACK_PATH + "?t=" + track;
        LoggerHelper.logRequest("GET", path, null);
        LoggerHelper.logInfo("Используемый track: {}", track);

        Response response = given()
                .spec(requestSpec)
                .queryParam("t", track)
                .when()
                .get(Constants.ORDERS_TRACK_PATH);

        LoggerHelper.logResponse(response);
        return response;
    }

    @Step("Принять заказ")
    public Response acceptOrder(int orderId, int courierId) {
        String path = Constants.ORDERS_ACCEPT_PATH + orderId + "?courierId=" + courierId;
        LoggerHelper.logRequest("PUT", path, null);
        LoggerHelper.logInfo("Используемые данные: orderId={}, courierId={}", orderId, courierId);

        Response response = given()
                .spec(requestSpec)
                .queryParam("courierId", courierId)
                .when()
                .put(Constants.ORDERS_ACCEPT_PATH + orderId);

        LoggerHelper.logResponse(response);
        return response;
    }

    @Step("Завершить заказ")
    public Response finishOrder(int orderId) {
        String path = Constants.ORDERS_FINISH_PATH + orderId;
        OrderFinishRequest finishRequest = new OrderFinishRequest(orderId);
        
        LoggerHelper.logRequest("PUT", path, "OrderFinishRequest{id=" + orderId + "}");
        LoggerHelper.logInfo("Используемый orderId: {}", orderId);

        Response response = given()
                .spec(requestSpec)
                .body(finishRequest)
                .when()
                .put(path);

        LoggerHelper.logResponse(response);
        return response;
    }

    @Step("Отменить заказ")
    public Response cancelOrder(int track) {
        OrderCancelRequest cancelRequest = new OrderCancelRequest(track);
        
        LoggerHelper.logRequest("PUT", Constants.ORDERS_CANCEL_PATH, "OrderCancelRequest{track=" + track + "}");
        LoggerHelper.logInfo("Используемый track: {}", track);

        Response response = given()
                .spec(requestSpec)
                .body(cancelRequest)
                .when()
                .put(Constants.ORDERS_CANCEL_PATH);

        LoggerHelper.logResponse(response);
        return response;
    }

    @Step("Получить список заказов")
    public Response getOrders(Integer courierId, List<String> nearestStation,
                             Integer limit, Integer page) {
        RequestSpecification request = given().spec(requestSpec);
        StringBuilder queryParams = new StringBuilder("?");

        if (courierId != null) {
            request.queryParam("courierId", courierId);
            queryParams.append("courierId=").append(courierId).append("&");
        }
        if (nearestStation != null) {
            request.queryParam("nearestStation", nearestStation);
            queryParams.append("nearestStation=").append(nearestStation).append("&");
        }
        if (limit != null) {
            request.queryParam("limit", limit);
            queryParams.append("limit=").append(limit).append("&");
        }
        if (page != null) {
            request.queryParam("page", page);
            queryParams.append("page=").append(page).append("&");
        }

        String path = Constants.ORDERS_PATH + (queryParams.length() > 1 ? queryParams.toString().replaceAll("&$", "") : "");
        LoggerHelper.logRequest("GET", path, null);
        LoggerHelper.logInfo("Используемые параметры: courierId={}, nearestStation={}, limit={}, page={}",
                courierId, nearestStation, limit, page);

        Response response = request.get(Constants.ORDERS_PATH);
        LoggerHelper.logResponse(response);
        return response;
    }

    @Step("Поиск станций метро")
    public Response searchStations(String searchQuery) {
        String path = Constants.STATIONS_SEARCH_PATH + "?s=" + searchQuery;
        LoggerHelper.logRequest("GET", path, null);
        LoggerHelper.logInfo("Используемый поисковый запрос: {}", searchQuery);

        Response response = given()
                .spec(requestSpec)
                .queryParam("s", searchQuery)
                .when()
                .get(Constants.STATIONS_SEARCH_PATH);

        LoggerHelper.logResponse(response);
        return response;
    }
}

