
# BeagCache

This simple pure Java cache library provides developers with an easy-to-use solution for storing and managing data in memory. This library offers a straightforward way for storing key-value pairs with configurable expiration times. By using this library, developers can improve the performance of their applications by reducing the number of expensive database or network calls.

The cache library uses Java's built-in HashMap data structures to efficiently store and retrieve cached data. It allows developers to set Time-To-Live (TTL) to control the cache size and ensure that only the most relevant data is retained.
## Usage/Example

```java
//Created a user cache with 10 seconds duration
//User id attribute is the key to recover data from cache
BeagMultiCache<Long, User> userCache = BeagMultiCache.withDuration(10000);

Long userId = 1L;

//Tries to get user with id #1 from cache, otherwise try to load from the dabatase
User element = userCache.getElement(userId, () -> loadUserFromDb());
System.out.println(element.toString());
```