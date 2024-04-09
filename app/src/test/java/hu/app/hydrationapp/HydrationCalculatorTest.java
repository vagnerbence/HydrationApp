package hu.app.hydrationapp;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import hu.app.hydrationapp.model.HydrationCalculator;

public class HydrationCalculatorTest {
    @Test
public void addition_isCorrect() {
    //assign
        double weight=60;
        int age= 40;
        String gender="Female";
        double expected = 2.5;
        //act
        double  result =HydrationCalculator.calculateBaseWater(weight, age, gender);
        //assert

        assertEquals(expected, result);
}
}
