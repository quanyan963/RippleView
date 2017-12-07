package com.example.komoriwu.rippleview.bean;

/**
 * Created by KomoriWu on 2017/11/1.
 */

public class Header {
    private int payloadVersion;
    private String namespace;
    private String name;
    private String messageId;

    public Header(int payloadVersion, String namespace, String name, String messageId) {
        this.payloadVersion = payloadVersion;
        this.namespace = namespace;
        this.name = name;
        this.messageId = messageId;
    }

    public int getPayloadVersion() {
        return payloadVersion;
    }

    public void setPayloadVersion(int payloadVersion) {
        this.payloadVersion = payloadVersion;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
}
