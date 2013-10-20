/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.dmdirc.util.annotations.observable;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class ObservableModelTest {

    @Test
    public void testOldValueTestModel() {
        final String oldValue = "Foo";
        final String newValue = "Bar";
        ObservableOldValueTestModel model = new ObservableOldValueTestModel(oldValue);
        model.addStringListener(new ObservableOldValueTestModel.StringListener() {
            public void stringChanged(String testOldValue, String testNewValue) {
                assertEquals(oldValue, testOldValue);
                assertEquals(newValue, testNewValue);
            }
        });
        model.setString(newValue);
    }

    @Test
    public void testNewValueTestModel() {
        final String oldValue = "Foo";
        final String newValue = "Bar";
        ObservableNewValueTestModel model = new ObservableNewValueTestModel(oldValue);
        model.addStringListener(new ObservableNewValueTestModel.StringListener() {
            public void stringChanged(String testNewValue) {
                assertEquals(newValue, testNewValue);
            }
        });
        model.setString(newValue);
    }
}
