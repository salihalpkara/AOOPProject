package com.aoopproject.framework.core;

/**
 * Represents an event that occurs within the game, typically originating
 * from the {@link AbstractGameModel}.
 * Observers (implementing {@link GameObserver}) receive these events
 * to update themselves or perform actions.
 * <p>
 * This class can be extended to create more specific event types
 * if needed, or an event type enum can be added here.
 */
public class GameEvent {
    private final Object source;
    private final String type;
    private final Object payload;

    /**
     * Constructs a new GameEvent.
     *
     * @param source  The source of the event (e.g., the game model instance).
     * @param type    A string describing the type of event.
     * @param payload An optional payload carrying data relevant to the event. Can be null.
     */
    public GameEvent(Object source, String type, Object payload) {
        this.source = source;
        this.type = type;
        this.payload = payload;
    }

    /**
     * Constructs a new GameEvent without a payload.
     *
     * @param source The source of the event.
     * @param type   A string describing the type of event.
     */
    public GameEvent(Object source, String type) {
        this(source, type, null);
    }

    /**
     * Gets the source of the event.
     *
     * @return The object that originated the event.
     */
    public Object getSource() {
        return source;
    }

    /**
     * Gets the type of the event.
     * This can be used by observers to filter or switch on event types.
     *
     * @return A string identifier for the event type.
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the payload associated with this event.
     * The nature of the payload depends on the event type.
     *
     * @return The payload object, or null if no payload is associated.
     * The recipient might need to cast this to a more specific type.
     */
    public Object getPayload() {
        return payload;
    }

    @Override
    public String toString() {
        return "GameEvent{" +
                "source=" + (source != null ? source.getClass().getSimpleName() : "null") +
                ", type='" + type + '\'' +
                ", payload=" + payload +
                '}';
    }
}