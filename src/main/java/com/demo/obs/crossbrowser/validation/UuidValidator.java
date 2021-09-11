package com.demo.obs.crossbrowser.validation;

import org.slf4j.Logger;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerInvalidParametersException;

import java.util.UUID;

/**
 * Class with static method for UUID strings validation.
 */
public final class UuidValidator {

    private UuidValidator() {
        throw new UnsupportedOperationException();
    }

    public static void validateUuidOrThrowException(final String uuidString, final String parameterName, final Logger logger) {
        logger.debug("Checking a {} is a valid UUID: {}", parameterName, uuidString);
        try {
            UUID.fromString(uuidString);
        } catch (IllegalArgumentException e) {
            logger.error("{} provided is not a valid UUID as specified by the OSB API specification", parameterName, e);
            throw new ServiceBrokerInvalidParametersException(e);
        }
    }
}
