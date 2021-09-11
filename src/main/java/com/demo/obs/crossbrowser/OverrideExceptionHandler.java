package com.demo.obs.crossbrowser;

import org.springframework.cloud.servicebroker.annotation.ServiceBrokerRestController;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerInvalidParametersException;
import org.springframework.cloud.servicebroker.exception.ServiceDefinitionDoesNotExistException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceDoesNotExistException;
import org.springframework.cloud.servicebroker.model.error.ErrorMessage;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Это временное решение для переопределения кода ошибки.
 */
@ControllerAdvice(annotations = ServiceBrokerRestController.class)
@ResponseBody
@Order(Ordered.LOWEST_PRECEDENCE - 11)
public class OverrideExceptionHandler {

    @ExceptionHandler(ServiceInstanceDoesNotExistException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorMessage handleException(ServiceInstanceDoesNotExistException ex) {
        return getErrorResponse(ex);
    }

    private static final String MISSING_REQUEST_BODY = "Required request body is missing";

    @ExceptionHandler(ServiceBrokerInvalidParametersException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMessage handleException(ServiceBrokerInvalidParametersException ex) {
        return getErrorResponse(ex);
    }

    @ExceptionHandler(ServiceDefinitionDoesNotExistException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMessage handleException(ServiceDefinitionDoesNotExistException ex) {
        return getErrorResponse(ex);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMessage handleException(HttpMessageNotReadableException ex) {
        if (ex.getMessage() != null && ex.getMessage().contains(MISSING_REQUEST_BODY)) {
            return new ErrorMessage(MISSING_REQUEST_BODY);
        }

        return new ErrorMessage(ex.getMessage());
    }

    protected ErrorMessage getErrorResponse(ServiceBrokerException ex) {
        return ex.getErrorMessage();
    }
}
