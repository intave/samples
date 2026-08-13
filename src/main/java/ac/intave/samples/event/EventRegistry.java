package ac.intave.samples.event;

import java.util.*;

public final class EventRegistry {
	private static final Map<Class<? extends Event>, EventType<? extends Event>> TYPES_BY_CLASS = new LinkedHashMap<>();
	private static final Map<String, EventType<? extends Event>> TYPES_BY_NAME = new LinkedHashMap<>();

	static {
		register("header", HeaderEvent.class);
		register("combat.attack", AttackEvent.class);
		register("input.click", ClickEvent.class);
		register("player.move", PlayerMoveEvent.class);
		register("entity.move", EntityMoveEvent.class);
		register("player.slot_switch", SlotSwitchEvent.class);
		register("environment.properties", PropertiesEvent.class);
		register("player.init", PlayerInitEvent.class);
		register("entity.spawn", EntitySpawnEvent.class);
		register("entity.remove", EntityRemoveEvent.class);
		register("block.place", BlockPlaceEvent.class);
		register("block.interact", BlockInteractEvent.class);
		register("window.click", WindowClickEvent.class);
		register("window.items", WindowItemsEvent.class);
		register("window.action", WindowActionEvent.class);
	}

	private EventRegistry() {
	}

	public static synchronized <T extends Event> EventType<T> register(
		String name, Class<T> eventClass
	) {
		String normalizedName = normalizeName(name);
		if (TYPES_BY_NAME.containsKey(normalizedName)) {
			throw new IllegalArgumentException("Duplicate Nayoro event name: " + normalizedName);
		}
		if (TYPES_BY_CLASS.containsKey(eventClass)) {
			throw new IllegalArgumentException("Duplicate Nayoro event class: " + eventClass.getName());
		}
		EventType<T> eventType = new EventType<>(normalizedName, eventClass);
		TYPES_BY_NAME.put(normalizedName, eventType);
		TYPES_BY_CLASS.put(eventClass, eventType);
		return eventType;
	}

	public static EventType<? extends Event> typeOf(Event event) {
		EventType<? extends Event> eventType = TYPES_BY_CLASS.get(event.getClass());
		if (eventType == null) {
			throw new IllegalArgumentException("Unregistered Nayoro event class: " + event.getClass().getName());
		}
		return eventType;
	}

	public static EventType<? extends Event> typeNamed(String name) {
		if (name == null) {
			return null;
		}
		return TYPES_BY_NAME.get(normalizeName(name));
	}

	static Collection<EventType<? extends Event>> eventTypes() {
		return Collections.unmodifiableCollection(TYPES_BY_NAME.values());
	}

	private static String normalizeName(String name) {
		if (name == null) {
			throw new NullPointerException("name");
		}
		return name.toLowerCase(Locale.ROOT);
	}
}
