package com.dantonov.wiley.objectscache;

/**
 * Extending {@link ConfigurationValue} to add support of setting current state
 */
public interface ChangeableConfigurationValue<T> extends ConfigurationValue {
    /**
     * Set current state for ConfigurationValue
     * @param currentState new state for {@link ConfigurationValue}
     */
    void setCurrentState(T currentState);
}
