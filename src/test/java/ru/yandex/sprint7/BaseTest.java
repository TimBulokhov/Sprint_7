package ru.yandex.sprint7;

import io.qameta.allure.Step;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.Before;

import java.util.List;

import static io.restassured.RestAssured.given;

public class BaseTest {
    protected static final String BASE_URL = "http://qa-scooter.praktikum-services.ru";
    protected static final String COURIER_PATH = "/api/v1/courier";
    protected static final String COURIER_LOGIN_PATH = "/api/v1/courier/login";
    protected static final String ORDERS_PATH = "/api/v1/orders";
    protected static final String ORDERS_ACCEPT_PATH = "/api/v1/orders/accept/";
    protected static final String ORDERS_FINISH_PATH = "/api/v1/orders/finish/";
    protected static final String ORDERS_CANCEL_PATH = "/api/v1/orders/cancel";
    protected static final String ORDERS_TRACK_PATH = "/api/v1/orders/track";
    protected static final String STATIONS_SEARCH_PATH = "/api/v1/stations/search";

    protected RequestSpecification requestSpec;

    /**
     * Логирует информацию о запросе
     */
    protected void logRequest(String method, String path, String body) {
        System.out.println("\n=== REQUEST ===");
        System.out.println("Method: " + method);
        System.out.println("URL: " + BASE_URL + path);
        if (body != null && !body.isEmpty()) {
            System.out.println("Body: " + body);
        }
        System.out.println("===============\n");
    }

    /**
     * Логирует информацию об ответе
     */
    protected void logResponse(Response response) {
        System.out.println("\n=== RESPONSE ===");
        System.out.println("Status Code: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody().asString());
        System.out.println("================\n");
    }

    @Before
    public void setUp() {
        RestAssured.baseURI = BASE_URL;
        requestSpec = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .addFilter(new AllureRestAssured())
                .build();
    }

    @Step("Создать курьера")
    protected Response createCourier(String login, String password, String firstName) {
        String body = String.format(
                "{\"login\": \"%s\", \"password\": \"%s\", \"firstName\": \"%s\"}",
                login, password, firstName
        );

        logRequest("POST", COURIER_PATH, body);
        System.out.println("Используемые данные: login=" + login + ", firstName=" + firstName);

        Response response = given()
                .spec(requestSpec)
                .body(body)
                .when()
                .post(COURIER_PATH);

        logResponse(response);
        return response;
    }

    @Step("Авторизовать курьера")
    protected Response loginCourier(String login, String password) {
        String body = String.format(
                "{\"login\": \"%s\", \"password\": \"%s\"}",
                login, password
        );

        logRequest("POST", COURIER_LOGIN_PATH, body);
        System.out.println("Используемые данные: login=" + login);

        Response response = given()
                .spec(requestSpec)
                .body(body)
                .when()
                .post(COURIER_LOGIN_PATH);

        logResponse(response);
        return response;
    }

    @Step("Получить ID курьера")
    protected int getCourierId(String login, String password) {
        Response response = loginCourier(login, password);
        int courierId = response.then().extract().path("id");
        System.out.println("Получен courierId: " + courierId);
        return courierId;
    }

    @Step("Удалить курьера")
    protected Response deleteCourier(int id) {
        String path = COURIER_PATH + "/" + id;
        String body = "{\"id\": \"" + id + "\"}";
        logRequest("DELETE", path, body);
        System.out.println("Удаляем курьера с id: " + id);

        Response response = given()
                .spec(requestSpec)
                .body(body)
                .when()
                .delete(path);

        logResponse(response);
        return response;
    }

    @Step("Создать заказ")
    protected Response createOrder(String firstName, String lastName, String address,
                                   String metroStation, String phone, int rentTime,
                                   String deliveryDate, String comment, List<String> color) {
        // Создаем JSON для цвета
        StringBuilder bodyBuilder = new StringBuilder();
        bodyBuilder.append("{");

        if (color != null && !color.isEmpty()) {
            String colorsJson = "\"" + String.join("\", \"", color) + "\"";
            bodyBuilder.append("\"color\": [").append(colorsJson).append("], ");
        }

        bodyBuilder.append(String.format(
                "\"firstName\": \"%s\", \"lastName\": \"%s\", \"address\": \"%s\", " +
                        "\"metroStation\": \"%s\", \"phone\": \"%s\", \"rentTime\": %d, " +
                        "\"deliveryDate\": \"%s\", \"comment\": \"%s\"}",
                firstName, lastName, address, metroStation, phone, rentTime,
                deliveryDate, comment
        ));

        String body = bodyBuilder.toString();
        logRequest("POST", ORDERS_PATH, body);
        System.out.println("Используемые данные: firstName=" + firstName + ", lastName=" + lastName + 
                          ", metroStation=" + metroStation + ", color=" + color);

        Response response = given()
                .spec(requestSpec)
                .body(body)
                .when()
                .post(ORDERS_PATH);

        logResponse(response);
        return response;
    }

    @Step("Получить трек-номер заказа")
    protected int getOrderTrack(Response createOrderResponse) {
        int track = createOrderResponse.then().extract().path("track");
        System.out.println("Получен track: " + track);
        return track;
    }

    @Step("Получить заказ по трек-номеру")
    protected Response getOrderByTrack(int track) {
        logRequest("GET", ORDERS_TRACK_PATH + "?t=" + track, null);
        System.out.println("Используемый track: " + track);

        Response response = given()
                .spec(requestSpec)
                .queryParam("t", track)
                .when()
                .get(ORDERS_TRACK_PATH);

        logResponse(response);
        return response;
    }

    @Step("Принять заказ")
    protected Response acceptOrder(int orderId, int courierId) {
        String path = ORDERS_ACCEPT_PATH + orderId + "?courierId=" + courierId;
        logRequest("PUT", path, null);
        System.out.println("Используемые данные: orderId=" + orderId + ", courierId=" + courierId);

        Response response = given()
                .spec(requestSpec)
                .queryParam("courierId", courierId)
                .when()
                .put(ORDERS_ACCEPT_PATH + orderId);

        logResponse(response);
        return response;
    }

    @Step("Завершить заказ")
    protected Response finishOrder(int orderId) {
        String path = ORDERS_FINISH_PATH + orderId;
        String body = "{\"id\": " + orderId + "}";
        logRequest("PUT", path, body);
        System.out.println("Используемый orderId: " + orderId);

        Response response = given()
                .spec(requestSpec)
                .body(body)
                .when()
                .put(path);

        logResponse(response);
        return response;
    }

    @Step("Отменить заказ")
    protected Response cancelOrder(int track) {
        String body = "{\"track\": " + track + "}";
        logRequest("PUT", ORDERS_CANCEL_PATH, body);
        System.out.println("Используемый track: " + track);

        Response response = given()
                .spec(requestSpec)
                .body(body)
                .when()
                .put(ORDERS_CANCEL_PATH);

        logResponse(response);
        return response;
    }

    @Step("Получить список заказов")
    protected Response getOrders(Integer courierId, List<String> nearestStation,
                                 Integer limit, Integer page) {
        // Используем RequestSpecification для безопасной передачи параметров
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

        String path = ORDERS_PATH + (queryParams.length() > 1 ? queryParams.toString().replaceAll("&$", "") : "");
        logRequest("GET", path, null);
        System.out.println("Используемые параметры: courierId=" + courierId + ", nearestStation=" + nearestStation + 
                          ", limit=" + limit + ", page=" + page);

        Response response = request.get(ORDERS_PATH);
        logResponse(response);
        return response;
    }

    @Step("Поиск станций метро")
    protected Response searchStations(String searchQuery) {
        String path = STATIONS_SEARCH_PATH + "?s=" + searchQuery;
        logRequest("GET", path, null);
        System.out.println("Используемый поисковый запрос: " + searchQuery);

        Response response = given()
                .spec(requestSpec)
                .queryParam("s", searchQuery)
                .when()
                .get(STATIONS_SEARCH_PATH);

        logResponse(response);
        return response;
    }
}