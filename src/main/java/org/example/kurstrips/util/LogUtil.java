package org.example.kurstrips.util;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class LogUtil {
    private static final ResourceBundle messages;

    static {
        ResourceBundle tempBundle = null;
        try {
            tempBundle = ResourceBundle.getBundle("logmessages", new Locale("ru"));
        } catch (Exception e) {
            System.err.println("Не удалось загрузить ResourceBundle: " + e.getMessage());
        }
        messages = tempBundle;
    }

    private LogUtil() {
        // приватный конструктор
    }

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

    public static String getMessage(String key) {
        if (messages == null || !messages.containsKey(key)) {
            return key; // Возвращаем ключ, если сообщение не найдено
        }
        return messages.getString(key);
    }
}