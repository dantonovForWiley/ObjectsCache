package com.dantonov.wiley.objectscache;

/**
 * Models a configuration property value for {@link Cache}. <br>
 * It used to verify {@link Cache} is fine or not from processing perspective. <br>
 * Each {@link ConfigurationValue} can present current state and the state that is mentioned as a special state. <br>
 * If {@link ConfigurationValue} of {@link Cache} is not a special state - it is mentioned as responding (OK). <br>
 * {@link ConfigurationValue} objects are used to configure and track {@link Cache} configuration status.
 */
public interface ConfigurationValue {

    /**
     * Describes the level of importance for a case when {@link ConfigurationValue} is not responding
     */
    enum WARN_LEVEL {
        /**
         * Means the {@link ConfigurationValue} value is not important,
         * just for tracking some property of {@link Cache} instance
         */
        INFO,
        /**
         * Means the {@link ConfigurationValue} value is quite important,
         * but not critical for {@link Cache} instance
         */
        WARN,
        /**
         * Means the {@link ConfigurationValue} value is critical
         * for {@link Cache} instance
         */
        CRITICAL
    }

    /**
     * Functional interface for checking that current state of {@link ConfigurationValue} is a special state or not.
     *
     * @param <T>
     */
    interface Checker<T> {
        /**
         * Method to decide if the current state is a special state or not
         *
         * @param currentState of {@link ConfigurationValue}
         * @param specialState of {@link ConfigurationValue}
         * @return {@link Boolean} <br>
         * <code>true</code>: current state is not a special one <br>
         * <code>false</code>: current state is a special
         */
        Boolean isFine(T currentState, T specialState);
    }

    /**
     * Functional interface to represent {@link ConfigurationValue} state
     *
     * @param <T> the state of {@link ConfigurationValue}
     */
    interface Presenter<T> {
        String present(T value);
    }

    /**
     * Present special state
     *
     * @return {@link String} representation of the specia state
     */
    String presentSpecialState();

    /**
     * Present current state
     *
     * @return {@link String} representation of the current state
     */
    String presentCurrentState();

    /**
     * Provide {@link WARN_LEVEL} for this {@link ConfigurationValue}
     *
     * @return
     */
    WARN_LEVEL getWarnLevel();

    /**
     * Check the {@link ConfigurationValue} is not a special state
     *
     * @return
     */
    boolean isResponding();
}
