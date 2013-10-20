package com.dmdirc.util.annotations.observable;

@ObservableModel(oldValue = false)
public class NewValueTestModel {

    private String test;

    public NewValueTestModel(final String test) {
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
