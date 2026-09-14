package com.ashokleyland.wms.exception;

/** Thrown when the scanned location barcode does not match the assigned location (FR-03). */
public class LocationMismatchException extends RuntimeException {
    public LocationMismatchException(String message) {
        super(message);
    }
}
