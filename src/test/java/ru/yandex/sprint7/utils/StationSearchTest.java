package ru.yandex.sprint7.utils;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.junit.Test;
import ru.yandex.sprint7.BaseTest;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class StationSearchTest extends BaseTest {

    @Test
    @Step("Тест успешного поиска станций метро")
    public void testSearchStationsSuccess() {
        String searchQuery = "Сокол";
        
        Response response = searchStations(searchQuery);

        checkSearchStationsSuccess(response);
    }

    @Test
    @Step("Тест поиска станций метро без параметра")
    public void testSearchStationsWithoutParam() {
        Response response = given()
                .spec(requestSpec)
                .when()
                .get(STATIONS_SEARCH_PATH);

        // API может возвращать 400 или пустой массив
        response.then().statusCode(anyOf(is(200), is(400)));
    }

    @Test
    @Step("Тест поиска станций метро с несуществующим запросом")
    public void testSearchStationsWithNonExistentQuery() {
        String searchQuery = "НесуществующаяСтанция12345";
        
        Response response = searchStations(searchQuery);

        // API может вернуть пустой массив или 200
        response.then().statusCode(200);
        // Проверяем, что это массив (может быть пустым)
        response.then().body("$", instanceOf(List.class));
    }

    @Step("Проверить успешный поиск станций метро")
    private void checkSearchStationsSuccess(Response response) {
        response.then()
                .statusCode(200)
                .body("$", notNullValue())
                .body("$", instanceOf(List.class));
        
        // Проверяем структуру ответа, если есть результаты
        if (response.getBody().jsonPath().getList("$").size() > 0) {
            response.then()
                    .body("[0].number", notNullValue())
                    .body("[0].name", notNullValue())
                    .body("[0].color", notNullValue());
        }
    }
}

