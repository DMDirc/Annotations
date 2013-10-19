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
    public void testTestModel() {
        final String oldValue = "Foo";
        final String newValue = "Bar";
        ObservableTestModel model = new ObservableTestModel(oldValue);
        model.addStringListener(new ObservableTestModel.StringListener() {
            public void stringChanged(String oldValue, String newValue) {
                assertEquals(oldValue, oldValue);
                assertEquals(newValue, newValue);
            }
        });
        model.setString(newValue);
    }
}
