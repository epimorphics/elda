package com.epimorphics.lda.support;

enum RequestStatus {
    Received("received"),
    Processing("processing"),
    Completed("completed"),
    Error("error");

    public final String name;

    RequestStatus(String name) {
        this.name = name;
    }

    ;
}
