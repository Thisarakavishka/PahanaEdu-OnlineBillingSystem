package com.icbt.pahanaeduonlinebillingsystem.common.util;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * @author Thisara Kavishka
 * @date 2025-07-24
 * @since 1.0
 */
public class SendResponse {

    public static void sendPlainText(HttpServletResponse httpServletResponse, int statusCode, String message) throws IOException {
        httpServletResponse.setStatus(statusCode);
        httpServletResponse.setContentType("text/plain");
        httpServletResponse.setCharacterEncoding("UTF-8");
        httpServletResponse.getWriter().write(message);
    }

}
