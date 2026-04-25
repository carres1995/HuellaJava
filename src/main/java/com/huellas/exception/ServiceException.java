package com.huellas.exception;

/**
 * Base exception for service-layer errors.
 *
 * <p>All custom business exceptions should extend {@link RuntimeException}
 * directly (not this class), keeping each exception specific to its
 * business rule. This exception is reserved for unexpected infrastructure
 * errors during service operations (e.g., failed commit/rollback in the Repository layer).</p>
 *
 * <p>Note: Refer to CONSTITUTION.md §2.4 — Custom Exception Pattern</p>
 */
public class ServiceException extends RuntimeException {

    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
