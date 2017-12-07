package com.example.komoriwu.rippleview.bean;

/**
 * Created by KomoriWu on 2017/11/1.
 */

public class Directive {
    private Header header;
    private Endpoint endpoint;

    public Directive(Header header, Endpoint endpoint) {
        this.header = header;
        this.endpoint = endpoint;
    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(Endpoint endpoint) {
        this.endpoint = endpoint;
    }
}
