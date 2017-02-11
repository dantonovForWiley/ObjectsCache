package com.dantonov.wiley.objectscache;

import com.dantonov.wiley.objectscache.impl.GenericConfigurationValue;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Test {@link ConfigurationValue} implementations.<br>
 * Also, it contains extended {@link GenericConfigurationValue} implementation to show the
 * flexibility of {@link ConfigurationValue} design.
 */
public class ConfigValuesTest {

    /**
     * Instantiate configuration values<br>
     * Setting {@link ConfigurationValue} and {@link ChangeableConfigurationValue} by the same
     * value to show a difference (one will be used to modify current value and another only to
     * check status)
     */
    @BeforeTest
    public void createConfigurationValues() {
        integerCounterAsChangeableConfigurationValue = new GenericConfigurationValue<>
                (maxIntegerValue, integerCounterChecker,
                        ConfigurationValue.WARN_LEVEL.CRITICAL, value -> String.format("max " +
                        "allowed value is %s", value), value -> String.format("current value: %s",
                        value));
        integerCounterAsConfigurationValue = integerCounterAsChangeableConfigurationValue;

        integerRangeCheckerAsChangeableConfigurationValue = new
                GenericConfigurationValue<>(-1, integerRangeChecker, ConfigurationValue
                .WARN_LEVEL.INFO, value -> String.format("Value should be in range [%s,%s]",
                rangeMin, rangeMax), value -> String.format("Current value is %s", value));
        integerRangeCheckerAsConfigurationValue = integerRangeCheckerAsChangeableConfigurationValue;

        stringCollectionVerifierAsStringCollectionVerifier = new StringCollectionVerifier
                (keyWordInStringCollection);
        stringCollectionVerifierAsConfigurationValue =
                stringCollectionVerifierAsStringCollectionVerifier;
    }

    /**
     * Check all configuration values that has no current state defined yet
     */
    @Test
    public void checkConfigurationValueNoCurrentState() {
        Assert.assertFalse(integerCounterAsConfigurationValue.isResponding(), "Configuration " +
                "value should be not responding if current state is not set");
        Assert.assertFalse(integerRangeCheckerAsConfigurationValue.isResponding(), "Configuration " +
                "value should be not responding if current state is not set");
        Assert.assertFalse(stringCollectionVerifierAsConfigurationValue.isResponding(), "Configuration " +
                "value should be not responding if current state is not set");
    }

    /**
     * verify integer counter responding as expected
     */
    @Test
    public void checkIntegerCounterAsConfigurationValue() {
        for (int i = 0; i < maxIntegerValue + 1; i++) {
            int configurationValue = i;
            integerCounterAsChangeableConfigurationValue.setCurrentState(configurationValue);
            Assert.assertEquals(integerCounterAsConfigurationValue.isResponding(),
                    integerCounterChecker.isFine(configurationValue, maxIntegerValue),
                    "Configuration value integerCounter must respond the same as appropriate " +
                            "checker");
            if (configurationValue > maxIntegerValue) {
                Assert.assertFalse(integerCounterAsConfigurationValue.isResponding(),
                        "Configuration value integerCounter must not be responding when current " +
                                "value greater than defined max value");
            }
        }
    }

    /**
     * verify integer range value is responding as expected
     */
    @Test
    public void checkIntegerRangeCheckerAsConfigurationValue() {
        for (int i = rangeMin - 2; i < rangeMax + 2; i++) {
            int currentState = i;
            integerRangeCheckerAsChangeableConfigurationValue.setCurrentState(currentState);
            Assert.assertEquals(integerRangeCheckerAsConfigurationValue.isResponding(),
                    integerRangeChecker.isFine(currentState, -1), "Configuration value integerRange must " +
                            "respond the same as appropriate " +
                            "checker");
            if (currentState < rangeMin || currentState > rangeMax) {
                Assert.assertFalse(integerRangeCheckerAsConfigurationValue.isResponding(),
                        "Configuration value integerRange must not be responding if current " +
                                "value is out of the defined range");
            } else {
                Assert.assertTrue(integerRangeCheckerAsConfigurationValue.isResponding(),
                        "Configuration value integerRange must be responding if current " +
                                "value is inside the defined range");
            }
        }
    }

    /**
     * verify {@link StringCollectionVerifier} is responding as expected
     */
    @Test
    public void checkStringCollectionVerifierAsConfigurationValue() {

        Boolean testWordNotAdded = true;

        for (String string : testSequence) {
            stringCollectionVerifierAsStringCollectionVerifier.addString(string);

            if (string.equals(keyWordInStringCollection)) {
                testWordNotAdded = false;
            }

            Assert.assertEquals(testWordNotAdded, stringCollectionVerifierAsConfigurationValue
                    .isResponding(), "Configuration value " +
                    "stringCollectionVerifierAsConfigurationValue must be responding till test " +
                    "word is not added as a state");
        }
    }

    /**
     * integer counter definition. numeric. value is responding (is fine) till numeric value is
     * less than defined max value (special state)
     */
    Integer maxIntegerValue = 10;
    ConfigurationValue.Checker<Integer> integerCounterChecker = (currentState, specialState) ->
            currentState <= specialState;
    // reference to check configuration value
    ConfigurationValue integerCounterAsConfigurationValue;
    // reference to modify configuration value
    ChangeableConfigurationValue<Integer> integerCounterAsChangeableConfigurationValue;

    /**
     * integer configuration value that should stay in range. otherwise, configuration value is
     * not responding.
     */
    final int rangeMin = 10;
    final int rangeMax = 20;
    // this example does not use special state, since range can not be set by one integer
    ConfigurationValue.Checker<Integer> integerRangeChecker = ((currentState, specialState) ->
            currentState >= rangeMin && currentState <= rangeMax);
    ConfigurationValue integerRangeCheckerAsConfigurationValue;
    ChangeableConfigurationValue<Integer> integerRangeCheckerAsChangeableConfigurationValue;


    /**
     * configuration value that track strings. value is responding till key word is not set as a
     * current value.
     */
    String keyWordInStringCollection = "Denis";
    List<String> testSequence = Arrays.asList("This", "is", "a", "test", "created", "by",
            "Denis", "for", "Wiley", "interview");

    /**
     * Example of {@link ConfigurationValue} implementation.<br>
     * current state is a string. value is responding since special string is not set as a
     * current value
     */
    class StringCollectionVerifier implements ConfigurationValue {

        public StringCollectionVerifier(String mySpecialString) {
            this.mySpecialString = mySpecialString;
        }

        public void addString(String string) {
            allStringValues.add(string);
        }

        @Override
        public String presentCurrentState() {
            return String.format("Now collection has the following values %s", allStringValues
                    .stream().collect(Collectors.joining(", ")));
        }

        @Override
        public WARN_LEVEL getWarnLevel() {
            return WARN_LEVEL.CRITICAL;
        }

        @Override
        public String presentSpecialState() {
            return String.format("Special key string is: %s", mySpecialString);
        }

        @Override
        public Boolean isResponding() {
            if (allStringValues.size() == 0) {
                // means no current state set
                return false;
            } else {
                return !allStringValues
                        .contains(mySpecialString);
            }
        }

        private String mySpecialString;
        private Set<String> allStringValues = new HashSet<>();
    }

    ConfigurationValue stringCollectionVerifierAsConfigurationValue;
    StringCollectionVerifier stringCollectionVerifierAsStringCollectionVerifier;
}
