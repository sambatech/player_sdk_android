package com.sambatech.player.event;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Leandro Zanol - 10/12/15
 */
public class SambaEventBus {

	private static EventBus _eventBus = new EventBus();

	private SambaEventBus() {}

	public static void subscribe(Object listener) {
		_eventBus.subscribe(listener);
	}

	public static void unsubscribe(Object listener) {
		_eventBus.unsubscribe(listener);
	}

	public static void post(SambaEvent e) {
		_eventBus.post(e);
	}

	private static class EventBus {
		private HashMap<String, List<Object>> listeners = new HashMap<>();

		public void subscribe(Object listener) {
			String k;

			for (Method m : listener.getClass().getDeclaredMethods()) {
				if (Modifier.isPublic(m.getModifiers())) {
					k = m.getName().substring(2).toLowerCase();

					if (!listeners.containsKey(k))
						listeners.put(k, new ArrayList<>());

					listeners.get(k).add(listener);
				}
			}
		}

		public void unsubscribe(Object listener) {
			/*for (Method m : listener.getClass().getDeclaredMethods()) {
				k = m.getName().substring(2).toUpperCase();

				if (listeners.containsKey(k))
					listeners.remove(k);
			}*/
		}

		public void post(SambaEvent e) {
			String k = e.getType().toString().toLowerCase();

			if (!listeners.containsKey(k))
				return;

			try {
				for (Object listener : listeners.get(k))
					listener.getClass().getDeclaredMethod("on" + k.substring(0, 1).toUpperCase() + k.substring(1), SambaEvent.class).invoke(listener, e);
			}
			catch (Exception exp) {
				exp.printStackTrace();
			}
		}
	}
}
