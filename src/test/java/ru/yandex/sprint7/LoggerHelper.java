package ru.yandex.sprint7.utils;

import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.sprint7.constants.Constants;

public class LoggerHelper {
    private static final Logger logger = LoggerFactory.getLogger(LoggerHelper.class);

    public static void logRequest(String method, String path, String body) {
        logger.info("\n=== REQUEST ===");
        logger.info("Method: {}", method);
        logger.info("URL: {}{}", Constants.BASE_URL, path);
        if (body != null && !body.isEmpty()) {
            logger.info("Body: {}", body);
        }
        logger.info("===============\n");
    }

    public static void logResponse(Response response) {
        logger.info("\n=== RESPONSE ===");
        logger.info("Status Code: {}", response.getStatusCode());
        logger.info("Response Body: {}", response.getBody().asString());
        logger.info("================\n");
    }

    public static void logInfo(String message, Object... args) {
        logger.info(message, args);
    }
}

