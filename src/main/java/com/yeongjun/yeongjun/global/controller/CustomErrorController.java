package com.yeongjun.yeongjun.global.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/error")
public class CustomErrorController implements ErrorController {

    private static final Logger log = LoggerFactory.getLogger(CustomErrorController.class);

    @RequestMapping(produces = MediaType.TEXT_HTML_VALUE)
    public String handleErrorHtml(HttpServletRequest request, Model model) {
        Integer statusCode = getStatusCode(request);
        logError(request, statusCode);

        if (statusCode != null) {
            switch (statusCode) {
                case 403:
                    model.addAttribute("message", "You don't have permission to access this page.");
                    return "error/403";
                case 404:
                    model.addAttribute("message", "The page you are looking for does not exist.");
                    return "error/404";
                case 500:
                    model.addAttribute("message", "An unexpected error occurred. Please try again later.");
                    return "error/500";
                default:
                    model.addAttribute("message", "An error occurred. Please try again.");
                    return "error/error";
            }
        }

        model.addAttribute("message", "An error occurred. Please try again.");
        return "error/error";
    }

    @RequestMapping
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleErrorJson(HttpServletRequest request) {
        Integer statusCode = getStatusCode(request);
        logError(request, statusCode);

        int status = statusCode != null ? statusCode : 500;

        Map<String, Object> body = new HashMap<>();
        body.put("status", status);

        Object messageAttr = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        String message = (messageAttr != null && !messageAttr.toString().isEmpty())
                ? messageAttr.toString()
                : "An unexpected error occurred.";
        body.put("message", message);

        Object requestUri = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        if (requestUri != null) {
            body.put("path", requestUri.toString());
        }

        return ResponseEntity.status(status).body(body);
    }

    private Integer getStatusCode(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (status != null) {
            try {
                return Integer.valueOf(status.toString());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private void logError(HttpServletRequest request, Integer statusCode) {
        Object requestUri = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        Throwable exception = (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);

        if (exception != null) {
            log.error("Error occurred at [{}], status code [{}]: ", requestUri, statusCode, exception);
        } else {
            log.warn("Error occurred at [{}], status code [{}] (No exception stack trace available)", requestUri, statusCode);
        }
    }
}
