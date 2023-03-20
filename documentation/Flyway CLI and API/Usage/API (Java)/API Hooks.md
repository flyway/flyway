---
pill: hooks
subtitle: API hooks
---
# API: hooks

There are three ways you can hook into the Flyway API.

## Java-based Migrations

The first one is the the most common one: [Java-based Migrations](Concepts/migrations#java-based-migrations)
when you need more power than SQL can offer you. This is great to for dealing with LOBs or performing advanced
data transformations.

In order to be picked up by Flyway, Java-based Migrations must implement the
[`JavaMigration`](https://javadoc.io/doc/org.flywaydb/flyway-core/latest/org/flywaydb/core/api/migration/JavaMigration.html) interface. Most users
however should inherit from the convenience class [`BaseJavaMigration`](https://javadoc.io/doc/org.flywaydb/flyway-core/latest/org/flywaydb/core/api/migration/BaseJavaMigration.html)
instead as it encourages Flyway's default naming convention, enabling Flyway to automatically extract the version and
the description from the class name.

### Java-based migrations as Spring Beans

By default Java-based migrations discovered through classpath scanning and instantiated by Flyway. In a dependency
injection container it is sometimes useful to let the container instantiate the class and wire up its dependencies for you.

The Flyway API lets you pass pre-instantiated Java-based migrations using the `javaMigrations` property.

Spring users can use this to automatically use all `JavaMigration` Spring beans with Flyway:

```java
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.migration.JavaMigration;
import org.springframework.context.ApplicationContext;

...
ApplicationContext applicationContext = ...; // obtain a reference to Spring's ApplicationContext.

Flyway flyway = Flyway.configure()
    .dataSource(url, user, password)
    // Add all Spring-instantiated JavaMigration beans
    .javaMigrations(applicationContext.getBeansOfType(JavaMigration.class).values().toArray(new JavaMigration[0]))
    .load();
flyway.migrate();
```

## Java-based Callbacks

Building upon that are the Java-based [Callbacks](Concepts/Callback concept)
when you need more power or flexibility in a Callback than SQL can offer you.

They can be created by implementing the [**Callback**](https://javadoc.io/doc/org.flywaydb/flyway-core/latest/org/flywaydb/core/api/callback/Callback.html)
interface:

```java
package org.flywaydb.core.api.callback;

/**
 * This is the main callback interface that should be implemented to handle Flyway lifecycle events.
 */
public interface Callback {
    /**
     * Whether this callback supports this event or not. This is primarily meant as a way to optimize event handling
     * by avoiding unnecessary connection state setups for events that will not be handled anyway.
     *
     * @param event   The event to check.
     * @param context The context for this event.
     * @return {@code true} if it can be handled, {@code false} if not.
     */
    boolean supports(Event event, Context context);

    /**
     * Whether this event can be handled in a transaction or whether it must be handled outside a transaction instead.
     * In the vast majority of the cases the answer will be
     * {@code true}. Only in the rare cases where non-transactional statements are executed should this return {@code false}.
     * This method is called before {@link #handle(Event, Context)} in order to determine in advance whether a transaction
     * can be used or not.
     *
     * @param event   The event to check.
     * @param context The context for this event.
     * @return {@code true} if it can be handled within a transaction (almost all cases). {@code false} if it must be
     * handled outside a transaction instead (very rare).
     */
    boolean canHandleInTransaction(Event event, Context context);

    /**
     * Handles this Flyway lifecycle event.
     *
     * @param event   The event to handle.
     * @param context The context for this event.
     */
    void handle(Event event, Context context);

    /**
     * The callback name, Flyway will use this to sort the callbacks alphabetically before executing them
     * @return The callback name
     */
    String getCallbackName();
}
```

The `event` argument tells you which [`Event`](https://javadoc.io/doc/org.flywaydb/flyway-core/latest/org/flywaydb/core/api/callback/Event.html) 
(`beforeClean`, `afterMigrate`, ...) is being handled and the `context` argument gives you access to things
like the database connection and the Flyway configuration.

It is possible for a Java callback to handle multiple events; for example, if you wanted to write a callback to
fire off a notification to a third party service at the end of a migration, whether successful or not, and didn't 
want to duplicate the code, then you could achieve this by handling both `afterMigrate` and `afterMigrateError`:

```java
public class MyNotifierCallback implements Callback {
    
    // Ensures that this callback handles both events
    @Override
    public boolean supports(Event event, Context context) {
        return event.equals(Event.AFTER_MIGRATE) || event.equals(Event.AFTER_MIGRATE_ERROR);
    }
    
    // Not relevant if we don't interact with the database
    @Override
    public boolean canHandleInTransaction(Event event, Context context) {
        return true;
    }
    
    // Send a notification when either event happens.
    @Override
    public void handle(Event event, Context context) {
        String notification = event.equals(Event.AFTER_MIGRATE) ? "Success" : "Failed";
        // ... Notification logic ...
        notificationService.send(notification);
    }

    String getCallbackName() {
        return "MyNotifier";
    }
}
``` 

In order to be picked up by Flyway, Java-based Callbacks must implement the Callback interface. 
Flyway will automatically scan for and load all callbacks found in the `db/callback` package. Additional callback classes or scan locations can be specified by the `flyway.callbacks` configuration property.

## Custom Migration resolvers &amp; executors

For those that need more than what the SQL and Java-based migrations offer, you also have the possibility to
implement your own [`MigrationResolver`](https://javadoc.io/doc/org.flywaydb/flyway-core/latest/org/flywaydb/core/api/resolver/MigrationResolver.html)
coupled with a custom [`MigrationExecutor`](https://javadoc.io/doc/org.flywaydb/flyway-core/latest/org/flywaydb/core/api/executor/MigrationExecutor.html).

These can then be used for loading things like CSV-based migrations or other custom formats.

By using the `skipDefaultResolvers` property, these custom resolvers can also be used
to completely replace the built-in ones (by default, custom resolvers will run in addition to
built-in ones).
