package com.epimorphics.lda.support;

enum MDCParam {
    RequestStatus("request_status"),
    StatusCode("status"),
    RequestId("request_id"),
    RequestTime("request_time"),
    RequestUri("request_uri");

    public final String name;

    MDCParam(String name) {
        this.name = name;
    }

    ;
}
