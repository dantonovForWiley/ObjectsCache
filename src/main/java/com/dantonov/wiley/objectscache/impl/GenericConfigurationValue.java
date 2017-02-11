package com.dantonov.wiley.objectscache.impl;

import com.dantonov.wiley.objectscache.ChangeableConfigurationValue;

import java.util.Optional;

/**
 * Generic implementation for {@link ChangeableConfigurationValue}
 */
public class GenericConfigurationValue<T> implements ChangeableConfigurationValue<T> {

    /**
     * Constructor for {@GenericConfigurationValue}
     *
     * @param specialState          generic type instance for special state
     * @param checker               implementation for {@link Checker}
     * @param warnLevel             {@link WARN_LEVEL} level
     * @param specialStatePresenter implementation for {@link Presenter} for special state
     * @param currentStatePresenter implementation for {@link Presenter} for current state
     */
    public GenericConfigurationValue(T specialState, Checker<T> checker, WARN_LEVEL warnLevel, Presenter<T> specialStatePresenter, Presenter<T> currentStatePresenter) {
        this.specialState = Optional.ofNullable(specialState);
        this.checker = checker;
        this.warnLevel = warnLevel;
        this.specialStatePresenter = specialStatePresenter;
        this.currentStatePresenter = currentStatePresenter;
    }

    @Override
    public WARN_LEVEL getWarnLevel() {
        return warnLevel;
    }

    @Override
    public Boolean isResponding() {
        if (currentState.isPresent() && specialState.isPresent()) {
            return checker.isFine(currentState.get(), specialState.get());
        } else {
            return false;
        }
    }

    @Override
    public String presentCurrentState() {
        return currentStatePresenter.present(specialState.get());
    }

    @Override
    public String presentSpecialState() {
        return specialStatePresenter.present(currentState.orElse(null));
    }

    @Override
    public void setCurrentState(T currentValue) {
        this.currentState = Optional.ofNullable(currentValue);
    }


    private Optional<T> specialState;
    private Presenter<T> specialStatePresenter;

    private Optional<T> currentState = Optional.empty();
    private Presenter<T> currentStatePresenter;

    private WARN_LEVEL warnLevel;
    private Checker<T> checker;
}
