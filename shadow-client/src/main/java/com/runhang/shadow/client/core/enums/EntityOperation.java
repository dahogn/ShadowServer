package com.runhang.shadow.client.core.enums;

public enum EntityOperation {

    ADD("add"),
    DELETE("delete"),
    UPDATE("update");

    private String operation;

    EntityOperation(String operation) {
        this.operation = operation;
    }

    public String getOperation() {
        return operation;
    }

}
