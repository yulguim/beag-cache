package org.olguin.beag.example;

import org.olguin.beag.BeagMultiCache;

public class Main {

	public static void main(String[] args) throws InterruptedException {
		//2 seconds duration
		BeagMultiCache<Long, User> userCache = BeagMultiCache.withDuration(2000);

		Long userId = 1L;

		//Tries to get from cache, otherwise try to load from dabatase
		User element = userCache.getElement(userId, () -> loadUserFromDb());
		System.out.println(element.toString());

		Thread.sleep(1000);

		//Now it's cached, so will not call method loadUserFromDb
		element = userCache.getElement(userId, () -> loadUserFromDb());
		System.out.println(element.toString());

		//Wait to expire
		Thread.sleep(10000);

		//Now it must call method bacause it already expired
		element = userCache.getElement(userId, () -> loadUserFromDb());
		System.out.println(element.toString());

		System.exit(0);
	}

	private static User loadUserFromDb() {
		System.out.println("Loading from database...");

		User user = new User();
		user.setId(1L);
		user.setName("Magnus");
		user.setEmail("magnus@goat.com");

		return user;
	}

}
