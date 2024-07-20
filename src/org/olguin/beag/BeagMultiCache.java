package org.olguin.beag;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class BeagMultiCache<K, V> {

	private Map<K, Element<V>> map = new ConcurrentHashMap<>();

	private long ttl;

	private BeagMultiCache(long ttl) {
		this.ttl = ttl;

		//Cleaner task
		new Timer("Timer-" + System.currentTimeMillis()).scheduleAtFixedRate(new BeagMultiCacheCleanerTask(this.map, this.ttl), 10000L, 10000L);
	}

	private BeagMultiCache(int hour, int minute, int second) {
		this.ttl = -1;

		ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
		ZonedDateTime nextRun = now.withHour(hour).withMinute(minute).withSecond(second);
		if (now.compareTo(nextRun) > 0) {
			nextRun = nextRun.plusDays(1);
		}

		Duration duration = Duration.between(now, nextRun);
		long initialDelay = duration.getSeconds();

		//Cleaner task
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleAtFixedRate(new BeagMultiCacheCleanerTask(this.map, this.ttl), initialDelay, TimeUnit.DAYS.toSeconds(1), TimeUnit.SECONDS);
	}

	public V getElement(K key, ThrowingRunnable<V> query) {
		V element = this.getElement(key);

		if (element != null) {
			return element;
		}

		V obj = query.execute();
		if (obj != null) {
			return this.addElement(key, obj).getElement();
		}

		return null;
	}

	@FunctionalInterface
	public interface ThrowingRunnable<T> {
		T execute();
	}

    public static <T, S> BeagMultiCache<T, S> withDuration(long millis) {
        return new BeagMultiCache<T, S>(millis);
    }

    public static <T, S> BeagMultiCache<T, S> withDuration(long amount, ChronoUnit chronoUnit) {
        return new BeagMultiCache<T, S>(Duration.of(amount, chronoUnit).toMillis());
    }

	public static <T, S> BeagMultiCache<T, S> atSpecificTime(int hour, int minute, int second) {
		return new BeagMultiCache<T, S>(hour, minute, second);
	}

	public V getElement(K key) {
		Element<V> element = this.map.get(key);
		if (element == null) {
			return null;
		}

        return element.getElement();
    }

	public void removeElement(K key) {
		synchronized (map) {
			this.map.remove(key);
		}
	}

    public V getOrElse(K key, Supplier<V> valueSupplier) {
        synchronized (map){
        	if (map.get(key) == null)
				map.put(key, new Element<>(valueSupplier.get()));
			return map.get(key).getElement();
		}
    }

    public Element<V> addElement(K key, V obj) {
        Element<V> element = new Element<>(obj);
        this.map.put(key, element);

        return element;
    }

	public int getSize() {
		return map.size();
	}

	public void clear() {
		this.map = new HashMap<>();
	}

	public static class Element<V> {

		private V element;

		private long createdAt;

		public Element(V element) {
			this.element = element;
			this.createdAt = System.currentTimeMillis();
		}

		public V getElement() {
			return element;
		}

		public long getCreatedAt() {
			return createdAt;
		}

	}

	public static class BeagMultiCacheCleanerTask extends TimerTask {

		private Map<Object, Element> map;

		private long ttl;

		public BeagMultiCacheCleanerTask(Map map, long ttl) {
			this.map = map;
			this.ttl = ttl;
		}

		@Override
		public void run() {
			synchronized (map) {
				map.values().removeIf(v -> ttl == -1 || (v.getCreatedAt() < (System.currentTimeMillis() - ttl)));
			}
		}

	}

}
