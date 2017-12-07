package com.example.komoriwu.rippleview.bean;

/**
 * Created by KomoriWu on 2017/11/1.
 */

public class TurnOn {
    private Directive directive;

    public TurnOn(Directive directive) {
        this.directive = directive;
    }

    public Directive getDirective() {
        return directive;
    }

    public void setDirective(Directive directive) {
        this.directive = directive;
    }
}
