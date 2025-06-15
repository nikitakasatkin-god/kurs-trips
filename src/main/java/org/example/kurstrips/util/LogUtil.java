/*
 * Утилитарный класс для работы с логированием и локализацией сообщений
 * Предоставляет функционал:
 * - Инициализация ResourceBundle для локализованных сообщений
 * - Создание и настройка логгеров
 * - Получение локализованных сообщений по ключу
 */
package org.example.kurstrips.util;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class LogUtil {
    /*
     * ResourceBundle для хранения локализованных сообщений
     * Инициализируется статически при загрузке класса
     */
    private static final ResourceBundle messages;

    /*
     * Статический блок инициализации
     * Загружает ResourceBundle с сообщениями для русского языка
     */
    static {
        ResourceBundle tempBundle = null;
        try {
            tempBundle = ResourceBundle.getBundle("logmessages", new Locale("ru"));
        } catch (Exception e) {
            System.err.println("Не удалось загрузить ResourceBundle: " + e.getMessage());
        }
        messages = tempBundle;
    }

    /*
     * Приватный конструктор для предотвращения создания экземпляров класса
     */
    private LogUtil() {
        // приватный конструктор
    }

    /*
     * Создает и настраивает логгер для указанного класса
     * @param clazz класс, для которого создается логгер
     * @return настроенный логгер
     */
    public static Logger getLogger(Class<?> clazz) {
        Logger logger = Logger.getLogger(clazz.getName());
        if (messages != null) {
            try {
                logger.setResourceBundle(messages);
            } catch (IllegalArgumentException e) {
                System.err.println("Ошибка установки ResourceBundle: " + e.getMessage());
            }
        }
        return logger;
    }

    /*
     * Получает локализованное сообщение по ключу
     * @param key ключ сообщения
     * @return локализованное сообщение или ключ, если сообщение не найдено
     */
    public static String getMessage(String key) {
        if (messages == null || !messages.containsKey(key)) {
            return key; // Возвращаем ключ, если сообщение не найдено
        }
        return messages.getString(key);
    }
}