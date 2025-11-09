package ru.yandex.sprint7.courier;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.junit.AfterClass;
import org.junit.Test;
import ru.yandex.sprint7.BaseTest;
import ru.yandex.sprint7.utils.TestDataGenerator;

import static org.hamcrest.Matchers.*;

public class CourierCreateTest extends BaseTest {
    private static String login;
    private static String password;
    private static String firstName;

    @AfterClass
    public static void tearDown() {
        if (login != null && password != null) {
            try {
                CourierCreateTest test = new CourierCreateTest();
                test.setUp();
                int courierId = test.getCourierId(login, password);
                test.deleteCourier(courierId);
            } catch (Exception e) {
                // Курьер уже удален или не существует
            }
        }
    }

    @Test
    @Step("Тест успешного создания курьера")
    public void testCreateCourierSuccess() {
        login = TestDataGenerator.generateRandomLogin();
        password = TestDataGenerator.generateRandomPassword();
        firstName = TestDataGenerator.generateRandomFirstName();

        System.out.println("Создаем курьера с данными: login=" + login + ", firstName=" + firstName);

        Response response = createCourier(login, password, firstName);

        checkCreateCourierSuccess(response);
    }

    @Test
    @Step("Тест создания дубликата курьера")
    public void testCreateDuplicateCourier() {
        // Используем курьера из testCreateCourierSuccess или создаем нового
        if (login == null) {
            login = TestDataGenerator.generateRandomLogin();
            password = TestDataGenerator.generateRandomPassword();
            firstName = TestDataGenerator.generateRandomFirstName();
            System.out.println("Создаем курьера для проверки дубликата: login=" + login);
            // Создаем курьера первый раз
            createCourier(login, password, firstName);
        } else {
            System.out.println("Используем существующего курьера: login=" + login);
            // Если курьер уже создан в другом тесте, убеждаемся что он существует
            // Если был удален, создаем заново
            try {
                getCourierId(login, password);
            } catch (Exception e) {
                // Курьер не существует, создаем заново
                createCourier(login, password, firstName);
            }
        }

        // Попытка создать дубликат с теми же данными
        System.out.println("Попытка создать дубликат курьера с login=" + login);
        Response response = createCourier(login, password, firstName);

        checkCreateDuplicateCourierError(response);
    }

    @Test
    @Step("Тест создания курьера без логина")
    public void testCreateCourierWithoutLogin() {
        // Используем пароль из основного теста или генерируем новый
        if (password == null) {
            password = TestDataGenerator.generateRandomPassword();
        }
        if (firstName == null) {
            firstName = TestDataGenerator.generateRandomFirstName();
        }

        System.out.println("Тест создания курьера без логина, используем password и firstName из других тестов");
        Response response = createCourier("", password, firstName);

        checkCreateCourierMissingFieldError(response);
    }

    @Test
    @Step("Тест создания курьера без пароля")
    public void testCreateCourierWithoutPassword() {
        // Используем логин из основного теста или создаем новый
        if (login == null) {
            login = TestDataGenerator.generateRandomLogin();
        }
        if (firstName == null) {
            firstName = TestDataGenerator.generateRandomFirstName();
        }

        System.out.println("Тест создания курьера без пароля, используем login=" + login + " из других тестов");
        Response response = createCourier(login, "", firstName);

        checkCreateCourierMissingFieldError(response);
    }

    @Test
    @Step("Тест создания курьера без имени")
    public void testCreateCourierWithoutFirstName() {
        // Создаем нового курьера с уникальным логином для этого теста
        String testLogin = TestDataGenerator.generateRandomLogin();
        String testPassword = TestDataGenerator.generateRandomPassword();

        System.out.println("Тест создания курьера без имени: login=" + testLogin);
        Response response = createCourier(testLogin, testPassword, "");

        checkCreateCourierSuccess(response); // firstName может быть необязательным
        
        // Сохраняем данные для очистки
        if (login == null) {
            login = testLogin;
            password = testPassword;
        }
    }

    @Step("Проверить успешное создание курьера")
    private void checkCreateCourierSuccess(Response response) {
        response.then()
                .statusCode(201)
                .body("ok", equalTo(true));
    }

    @Step("Проверить ошибку при создании дубликата курьера")
    private void checkCreateDuplicateCourierError(Response response) {
        response.then()
                .statusCode(409); // API возвращает 409 вместо 400
    }

    @Step("Проверить ошибку при отсутствии обязательных полей")
    private void checkCreateCourierMissingFieldError(Response response) {
        response.then()
                .statusCode(400);
    }
}