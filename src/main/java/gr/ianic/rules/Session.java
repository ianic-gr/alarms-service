package gr.ianic.rules;


/**
 * An abstract base class for managing sessions.
 * This class provides common functionality for loading rules and water meters,
 * as well as abstract methods for initializing and managing the rule engine.
 */
public abstract class Session {


    /**
     * Starts the rule engine.
     */
    protected abstract void startRulesEngine();

    /**
     * Stops the rule engine.
     */
    protected abstract void stopRulesEngine();

    /**
     * Initializes the session.
     */
    protected abstract void init();

}