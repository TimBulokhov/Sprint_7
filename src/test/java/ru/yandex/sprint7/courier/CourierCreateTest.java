package ru.yandex.sprint7.courier;

import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.junit.AfterClass;
import org.junit.Test;
import ru.yandex.sprint7.BaseTest;
import ru.yandex.sprint7.TestDataGenerator;

import static org.apache.http.HttpStatus.*;
import static org.hamcrest.Matchers.*;

public class CourierCreateTest extends BaseTest {
    private static java.util.List<String> logins = new java.util.ArrayList<>();
    private static java.util.List<String> passwords = new java.util.ArrayList<>();

    @AfterClass
    public static void tearDown() {
        CourierCreateTest test = new CourierCreateTest();
        test.setUp();
        for (int i = 0; i < logins.size(); i++) {
            try {
                String login = logins.get(i);
                String password = passwords.get(i);
                int courierId = test.courierApi.getCourierId(login, password);
                test.courierApi.deleteCourier(courierId);
            } catch (Exception e) {
                // Курьер уже удален или не существует
            }
        }
        logins.clear();
        passwords.clear();
    }

    @Test
    @Description("Тест успешного создания курьера")
    public void testCreateCourierSuccess() {
        String login = TestDataGenerator.generateRandomLogin();
        String password = TestDataGenerator.generateRandomPassword();
        String firstName = TestDataGenerator.generateRandomFirstName();

        Response response = courierApi.createCourier(login, password, firstName);

        checkCreateCourierSuccess(response);
        
        logins.add(login);
        passwords.add(password);
    }

    @Test
    @Description("Тест создания дубликата курьера")
    public void testCreateDuplicateCourier() {
        String testLogin = TestDataGenerator.generateRandomLogin();
        String testPassword = TestDataGenerator.generateRandomPassword();
        String testFirstName = TestDataGenerator.generateRandomFirstName();
        
        // Создаем курьера первый раз
        courierApi.createCourier(testLogin, testPassword, testFirstName);

        // Попытка создать дубликат с теми же данными
        Response response = courierApi.createCourier(testLogin, testPassword, testFirstName);

        checkCreateDuplicateCourierError(response);
        
        logins.add(testLogin);
        passwords.add(testPassword);
    }

    @Test
    @Description("Тест создания курьера без логина")
    public void testCreateCourierWithoutLogin() {
        String testPassword = TestDataGenerator.generateRandomPassword();
        String testFirstName = TestDataGenerator.generateRandomFirstName();

        Response response = courierApi.createCourier("", testPassword, testFirstName);

        checkCreateCourierMissingFieldError(response);
    }

    @Test
    @Description("Тест создания курьера без пароля")
    public void testCreateCourierWithoutPassword() {
        String testLogin = TestDataGenerator.generateRandomLogin();
        String testFirstName = TestDataGenerator.generateRandomFirstName();

        Response response = courierApi.createCourier(testLogin, "", testFirstName);

        checkCreateCourierMissingFieldError(response);
    }

    @Test
    @Description("Тест создания курьера без имени")
    public void testCreateCourierWithoutFirstName() {
        String testLogin = TestDataGenerator.generateRandomLogin();
        String testPassword = TestDataGenerator.generateRandomPassword();

        Response response = courierApi.createCourier(testLogin, testPassword, "");

        checkCreateCourierSuccess(response); // firstName может быть необязательным
        
        logins.add(testLogin);
        passwords.add(testPassword);
    }

    @Step("Проверить успешное создание курьера")
    private void checkCreateCourierSuccess(Response response) {
        response.then()
                .statusCode(SC_CREATED)
                .body("ok", equalTo(true));
    }

    @Step("Проверить ошибку при создании дубликата курьера")
    private void checkCreateDuplicateCourierError(Response response) {
        response.then()
                .statusCode(SC_CONFLICT);
    }

    @Step("Проверить ошибку при отсутствии обязательных полей")
    private void checkCreateCourierMissingFieldError(Response response) {
        response.then()
                .statusCode(SC_BAD_REQUEST);
    }
}
