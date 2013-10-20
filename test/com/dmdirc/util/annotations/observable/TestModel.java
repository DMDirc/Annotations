package com.dmdirc.util.annotations.observable;

@ObservableModel
public class TestModel {

    private String test;

    public TestModel(final String test) {
        this.test = test;
    }

    public String getString() {
        return test;
    }

    public void setString(final String test) {
        this.test = test;
    }

    public void doSomething() {
    }

}
