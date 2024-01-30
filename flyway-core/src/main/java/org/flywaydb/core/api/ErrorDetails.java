package org.flywaydb.core.api;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ErrorDetails {
    public final ErrorCode errorCode;
    public final String errorMessage;
}