package ru.yandex.sprint7.utils;

import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.junit.Test;
import ru.yandex.sprint7.BaseTest;
import ru.yandex.sprint7.constants.Constants;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.*;
import static org.hamcrest.Matchers.*;

public class StationSearchTest extends BaseTest {

    @Test
    @Description("Тест успешного поиска станций метро")
    public void testSearchStationsSuccess() {
        String searchQuery = "Сокол";
        
        Response response = orderApi.searchStations(searchQuery);

        checkSearchStationsSuccess(response);
    }

    @Test
    @Description("Тест поиска станций метро без параметра")
    public void testSearchStationsWithoutParam() {
        Response response = given()
                .spec(requestSpec)
                .when()
                .get(Constants.STATIONS_SEARCH_PATH);

        response.then().statusCode(anyOf(is(SC_OK), is(SC_BAD_REQUEST)));
    }

    @Test
    @Description("Тест поиска станций метро с несуществующим запросом")
    public void testSearchStationsWithNonExistentQuery() {
        String searchQuery = "НесуществующаяСтанция12345";
        
        Response response = orderApi.searchStations(searchQuery);

        response.then().statusCode(SC_OK);
        response.then().body("$", instanceOf(List.class));
    }

    @Step("Проверить успешный поиск станций метро")
    private void checkSearchStationsSuccess(Response response) {
        response.then()
                .statusCode(SC_OK)
                .body("$", notNullValue())
                .body("$", instanceOf(List.class));
        
        if (response.getBody().jsonPath().getList("$").size() > 0) {
            response.then()
                    .body("[0].number", notNullValue())
                    .body("[0].name", notNullValue())
                    .body("[0].color", notNullValue());
        }
    }
}
