package com.open.su.exceptions;

public class CrudServiceException extends RuntimeException {

    public static CrudServiceException DATABASE_ERROR = new CrudServiceException(Type.DATABASE_ERROR, "Database error");

    public static CrudServiceException NOT_FOUND = new CrudServiceException(Type.NOT_FOUND, "Not found");
    final Type type;

    CrudServiceException(Type type, String message) {
        super(message);
        this.type = type;
    }

    CrudServiceException(Type type, String message, Throwable cause) {
        super(message, cause);
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public CrudServiceException withMessage(String message) {
        return new CrudServiceException(type, message, this);
    }

    public CrudServiceException withCause(Throwable cause) {
        return new CrudServiceException(type, getMessage(), cause);
    }

    public enum Type {
        DATABASE_ERROR,
        NOT_FOUND,
    }
}
