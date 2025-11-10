package ru.yandex.sprint7;

import java.util.UUID;
import java.util.Random;

public class TestDataGenerator {
    private static final Random random = new Random();
    
    // Мужские имена
    private static final String[] MALE_FIRST_NAMES = {"Иван", "Александр", "Дмитрий", "Сергей", "Андрей", "Михаил", "Алексей", "Владимир"};
    // Женские имена
    private static final String[] FEMALE_FIRST_NAMES = {"Мария", "Анна", "Елена", "Ольга", "Екатерина", "Наталья", "Татьяна", "Ирина"};
    
    // Мужские фамилии
    private static final String[] MALE_LAST_NAMES = {"Иванов", "Петров", "Сидоров", "Козлов", "Новиков", "Морозов", "Волков", "Соколов"};
    // Женские фамилии
    private static final String[] FEMALE_LAST_NAMES = {"Иванова", "Петрова", "Сидорова", "Козлова", "Новикова", "Морозова", "Волкова", "Соколова"};
    
    private static final String[] ADDRESSES = {"Москва, ул. Ленина, д. 1", "СПб, Невский пр., д. 10", "Казань, ул. Баумана, д. 5"};
    private static final String[] METRO_STATIONS = {"1", "2", "3", "4", "5", "10", "15", "20"};
    private static final String[] COMMENTS = {"Срочно", "Осторожно", "Хрупкое", "Без комментариев"};

    /**
     * Генерирует случайный логин для курьера
     */
    public static String generateRandomLogin() {
        return "courier_" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Генерирует случайный пароль
     */
    public static String generateRandomPassword() {
        return "pass_" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Генерирует случайное имя (мужское или женское)
     */
    public static String generateRandomFirstName() {
        boolean isMale = random.nextBoolean();
        if (isMale) {
            return MALE_FIRST_NAMES[random.nextInt(MALE_FIRST_NAMES.length)];
        } else {
            return FEMALE_FIRST_NAMES[random.nextInt(FEMALE_FIRST_NAMES.length)];
        }
    }

    /**
     * Генерирует случайную фамилию, соответствующую имени
     */
    public static String generateRandomLastName() {
        // Генерируем случайную фамилию (мужскую или женскую)
        boolean isMale = random.nextBoolean();
        if (isMale) {
            return MALE_LAST_NAMES[random.nextInt(MALE_LAST_NAMES.length)];
        } else {
            return FEMALE_LAST_NAMES[random.nextInt(FEMALE_LAST_NAMES.length)];
        }
    }

    /**
     * Генерирует случайную фамилию, соответствующую переданному имени
     */
    public static String generateRandomLastNameForFirstName(String firstName) {
        // Определяем пол по имени
        boolean isMale = isMaleName(firstName);
        if (isMale) {
            return MALE_LAST_NAMES[random.nextInt(MALE_LAST_NAMES.length)];
        } else {
            return FEMALE_LAST_NAMES[random.nextInt(FEMALE_LAST_NAMES.length)];
        }
    }

    /**
     * Определяет, является ли имя мужским
     */
    private static boolean isMaleName(String firstName) {
        for (String maleName : MALE_FIRST_NAMES) {
            if (maleName.equals(firstName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Генерирует случайный адрес
     */
    public static String generateRandomAddress() {
        return ADDRESSES[random.nextInt(ADDRESSES.length)];
    }

    /**
     * Генерирует случайную станцию метро
     */
    public static String generateRandomMetroStation() {
        return METRO_STATIONS[random.nextInt(METRO_STATIONS.length)];
    }

    /**
     * Генерирует случайный телефон
     */
    public static String generateRandomPhone() {
        return "+7" + (9000000000L + random.nextInt(999999999));
    }

    /**
     * Генерирует случайное количество дней аренды (1-10)
     */
    public static int generateRandomRentTime() {
        return 1 + random.nextInt(10);
    }

    /**
     * Генерирует случайную дату доставки (в будущем)
     */
    public static String generateRandomDeliveryDate() {
        int daysAhead = 1 + random.nextInt(30);
        java.time.LocalDate date = java.time.LocalDate.now().plusDays(daysAhead);
        return date.toString();
    }

    /**
     * Генерирует случайный комментарий
     */
    public static String generateRandomComment() {
        return COMMENTS[random.nextInt(COMMENTS.length)];
    }
}

