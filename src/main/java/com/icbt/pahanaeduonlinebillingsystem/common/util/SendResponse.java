package com.icbt.pahanaeduonlinebillingsystem.common.util;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Thisara Kavishka
 * @date 2025-07-24
 * @since 1.0
 */
public class SendResponse {

    private static final Logger LOGGER = LogUtil.getLogger(SendResponse.class);
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    private SendResponse() {
    }

    public static void sendPlainText(HttpServletResponse httpServletResponse, int statusCode, String message) throws IOException {
        httpServletResponse.setStatus(statusCode);
        httpServletResponse.setContentType("text/plain");
        httpServletResponse.setCharacterEncoding("UTF-8");
        httpServletResponse.getWriter().write(message);
        LOGGER.log(Level.INFO, "Sent plain text response with status " + statusCode + ": " + message);
    }

    public static void sendJson(HttpServletResponse httpServletResponse, int statusCode, Object data) throws IOException {
        httpServletResponse.setStatus(statusCode);
        httpServletResponse.setContentType("application/json");
        httpServletResponse.setCharacterEncoding("UTF-8");
        try (PrintWriter out = httpServletResponse.getWriter()) {
            String json = convertObjectToJsonString(data);
            out.print(json);
            out.flush();
            LOGGER.log(Level.INFO, "Sent JSON response with status " + statusCode + ": " + json);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to send JSON response: " + e.getMessage(), e);

            httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            httpServletResponse.setContentType("text/plain");
            httpServletResponse.getWriter().write("Internal server error: Failed to serialize response data.");
        }
    }

    private static String convertObjectToJsonString(Object obj) {
        if (obj == null) {
            return "null";
        }
        if (obj instanceof Map) {
            return mapToJsonString((Map<String, Object>) obj);
        }
        if (obj instanceof List) {
            return listToJsonString((List<?>) obj);
        }
        if (obj instanceof String) {
            return "\"" + escapeJsonString((String) obj) + "\"";
        }
        if (obj instanceof Number || obj instanceof Boolean) {
            return obj.toString();
        }
        if (obj instanceof Timestamp) {
            return "\"" + DATE_FORMAT.format((Timestamp) obj) + "\"";
        }
        if (obj instanceof Date) {
            return "\"" + DATE_FORMAT.format((Date) obj) + "\"";
        }

        LOGGER.log(Level.WARNING, "Attempted to serialize unsupported object type to JSON: " + obj.getClass().getName());
        return "\"" + escapeJsonString(obj.toString()) + "\"";
    }

    private static String mapToJsonString(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) {
                sb.append(",");
            }
            sb.append("\"").append(escapeJsonString(entry.getKey())).append("\":");
            sb.append(convertObjectToJsonString(entry.getValue()));
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    private static String listToJsonString(List<?> list) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Object item : list) {
            if (!first) {
                sb.append(",");
            }
            sb.append(convertObjectToJsonString(item));
            first = false;
        }
        sb.append("]");
        return sb.toString();
    }

    private static String escapeJsonString(String text) {
        return text.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
