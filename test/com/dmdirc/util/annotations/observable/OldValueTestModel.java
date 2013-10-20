package com.dmdirc.util.annotations.observable;

@ObservableModel
public class OldValueTestModel {

    private String test;

    public OldValueTestModel(final String test) {
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
