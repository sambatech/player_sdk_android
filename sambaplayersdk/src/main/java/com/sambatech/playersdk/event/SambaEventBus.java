package com.sambatech.playersdk.event;

import android.util.Log;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

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
		private List<Object> postponedUnsubscribes;

		public void subscribe(Object listener) {
			String type = listener.getClass().getSuperclass().getSimpleName();
			String k;

			for (Method m : listener.getClass().getDeclaredMethods()) {
				if (!Modifier.isPublic(m.getModifiers()))
					continue;

				k = String.format("%s:%s", type, m.getName().substring(2).toLowerCase());

				if (!listeners.containsKey(k))
					listeners.put(k, new ArrayList<>());

				listeners.get(k).add(listener);
			}
		}

		public void unsubscribe(Object listener) {
			if (postponedUnsubscribes != null) {
				postponedUnsubscribes.add(listener);
				return;
			}

			String type = listener.getClass().getSuperclass().getSimpleName();
			String k;
			List<Object> ltnList;

			for (Method m : listener.getClass().getDeclaredMethods()) {
				k = String.format("%s:%s", type, m.getName().substring(2).toLowerCase());

				if (listeners.containsKey(k)) {
					ltnList = listeners.get(k);

					if (ltnList.contains(listener))
						ltnList.remove(listener);

					if (ltnList.size() == 0)
						listeners.remove(k);
				}
			}
		}

		public void post(SambaEvent e) {
			String t = e.getType().toString().toLowerCase();
			String k = String.format("%s:%s", e.getType().getClass().getEnclosingClass().getSimpleName(), t);

			t = t.substring(0, 1).toUpperCase() + t.substring(1);

			if (!listeners.containsKey(k))
				return;

			try {
				// postpone call for lock purposes
				postponedUnsubscribes = new ArrayList<>();

                Object listener;

				for (ListIterator<Object> iterator = listeners.get(k).listIterator(); iterator.hasNext();) {
                    listener = iterator.next();
					listener.getClass().getDeclaredMethod("on" + t, SambaEvent.class).invoke(listener, e);
                }

				List<Object> unsubs = postponedUnsubscribes;

				// release locker
				postponedUnsubscribes = null;

				if (unsubs.size() > 0) {
					for (Object ltn : unsubs)
						unsubscribe(ltn);

					unsubs.clear();
				}
			}
			catch (Exception exp) {
				Log.e(getClass().getSimpleName(), "Error trying to lookup or invoke method.", exp);
			}
		}
	}
}
