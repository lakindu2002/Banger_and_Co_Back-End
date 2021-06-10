package com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions;

/**
 * Thrown when a resource fails to update due to conflicts or logical errors.
 *
 * @author Lakindu Hewawasam
 */
public class ResourceNotUpdatedException extends Exception {
    public ResourceNotUpdatedException(String s) {
        super(s);
    }
}
