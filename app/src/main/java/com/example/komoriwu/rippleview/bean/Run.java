package com.example.komoriwu.rippleview.bean;

/**
 * Created by KomoriWu on 2017/11/1.
 */

public class Run {
    private String account;
    private String endpointId;
    private String name;
    private String namespace;

    public Run(String account, String endpointId, String name, String namespace) {
        this.account = account;
        this.endpointId = endpointId;
        this.name = name;
        this.namespace = namespace;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getEndpointId() {
        return endpointId;
    }

    public void setEndpointId(String endpointId) {
        this.endpointId = endpointId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
}
