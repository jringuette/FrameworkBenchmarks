package io.helidon.benchmark.models;

import io.helidon.config.Config;
import io.reactivex.Single;
import org.davidmoten.rx.jdbc.Database;

import javax.sql.DataSource;
import java.util.List;

public class RxJdbcRepository implements DbRepository {
    private final Database database;

    public RxJdbcRepository(DataSource dataSource) {
        this.database = Database
                .nonBlocking()
                .maxPoolSize(Runtime.getRuntime().availableProcessors() * 2)
                .connectionProvider(dataSource)
                .build();
    }

    public RxJdbcRepository(Config config) {
        this.database = Database
                .nonBlocking()
                .maxPoolSize(Runtime.getRuntime().availableProcessors() * 2)
                .url(config.get("jdbcUrl").asString())
                .property("user", config.get("username").asString())
                .property("password", config.get("password").asString())
                .build();
    }

    @Override
    public Single<World> getWorld(int id) {
        return database
                .select("SELECT id, randomnumber FROM world WHERE id = ?")
                .parameters(id)
                .get(rs -> new World(rs.getInt(1), rs.getInt(2)))
//                .doOnEach(worldNotification -> System.out.println("database on each on " + Thread.currentThread().getName()))
                .firstOrError();
//                .doOnSubscribe(disposable -> System.out.println("database subscribing on " + Thread.currentThread().getName()))
//                .doOnSuccess(world -> System.out.println("database completed on " + Thread.currentThread().getName()));
    }

    @Override
    public Single<World> updateWorld(World world) {
        return database
                .update("UPDATE world SET randomnumber = ? WHERE id = ?")
                .parameters(world.randomNumber, world.id)
                .counts()
                .firstOrError()
                .map(integer -> world);
    }

    @Override
    public Single<List<Fortune>> getFortunes() {
        return database
                .select("SELECT id, message FROM fortune")
                .get(rs -> new Fortune(rs.getInt(1), rs.getString(2)))
                .toList();
    }
}
