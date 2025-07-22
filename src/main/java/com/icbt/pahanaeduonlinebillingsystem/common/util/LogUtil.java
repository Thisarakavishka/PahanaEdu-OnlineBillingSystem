package com.icbt.pahanaeduonlinebillingsystem.common.util;

import java.util.logging.Logger;

/**
 * @author Thisara Kavishka
 * @date 2025-07-22
 * @since 1.0
 */
public class LogUtil {

    public static Logger getLogger(Class<?> clazz) {
        return Logger.getLogger(clazz.getName());
    }
}
