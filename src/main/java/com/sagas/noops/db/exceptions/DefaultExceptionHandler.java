package com.sagas.noops.db.exceptions;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Log4j2
@ControllerAdvice
public class DefaultExceptionHandler {
    public static final String ERROR_MESSAGE_NAME = "errorMessage";

    public static final String SQL_NAME = "sql";

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGlobalException(Throwable ex, RedirectAttributes redirectAttributes) {
        log.error(ex);
        redirectAttributes.addFlashAttribute(ERROR_MESSAGE_NAME, ex.getMessage());
        return "redirect:/";
    }

    @ExceptionHandler(DbQueryException.class)
    public String handleDbQueryException(DbQueryException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute(SQL_NAME, ex.getSql());
        return handleGlobalException(ex, redirectAttributes);
    }
}
