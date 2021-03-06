/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.helidon.benchmark;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.helidon.benchmark.models.DbRepository;
import io.helidon.benchmark.models.JdbcRepository;
import io.helidon.benchmark.models.RxJdbcRepository;
import io.helidon.benchmark.services.DbService;
import io.helidon.benchmark.services.FortuneService;
import io.helidon.benchmark.services.JsonService;
import io.helidon.benchmark.services.PlainTextService;
import io.helidon.config.Config;
import io.helidon.webserver.ConnectionClosedException;
import io.helidon.webserver.Routing;
import io.helidon.webserver.ServerConfiguration;
import io.helidon.webserver.WebServer;
import io.reactivex.Scheduler;
import io.reactivex.exceptions.UndeliverableException;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.logging.LogManager;

/**
 * Simple Hello World rest application.
 */
public final class Main {

    /**
     * Cannot be instantiated.
     */
    private Main() {
    }

    private static Scheduler getScheduler() {
        return Schedulers.from(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2));
    }

    private static DataSource getDataSource(Config config) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(config.get("jdbcUrl").asString());
        hikariConfig.setUsername(config.get("username").asString());
        hikariConfig.setPassword(config.get("password").asString());
        hikariConfig.setMaximumPoolSize(Runtime.getRuntime().availableProcessors() * 2);

        return new HikariDataSource(hikariConfig);
    }

    private static DbRepository getJdbcRepository(Config config) {
        DataSource dataSource = getDataSource(config.get("dataSource"));
        Scheduler scheduler = getScheduler();
        return new JdbcRepository(dataSource, scheduler);
    }

    private static DbRepository getRxJdbcRepository(Config config) {
        /*DataSource dataSource = getDataSource(config.get("dataSource"));
        return new RxJdbcRepository(dataSource);*/
        return new RxJdbcRepository(config.get("dataSource"));
    }

    private static Mustache getTemplate() {
        MustacheFactory mf = new DefaultMustacheFactory();
        return mf.compile("fortunes.mustache");
    }

    /**
     * Creates new {@link Routing}.
     *
     * @return the new instance
     */
    private static Routing createRouting(Config config) {
        Routing.Builder routingBuilder = Routing.builder()
                .any((req, res) -> {
                    res.headers().add("Server", "Helidon");
                    req.next();
                })
                .register(new JsonService())
                .register(new PlainTextService());

        if (config.get("profile").hasValue()) {
            String activeProfile = config.get("profile").asString();
            DbRepository repository = null;

            if (activeProfile.equals("jdbc")) {
                repository = getJdbcRepository(config);


            } else if (activeProfile.equals("rx-jdbc")) {
                RxJavaPlugins.setErrorHandler(e -> {
                    if (e instanceof UndeliverableException) {
                        e = e.getCause();
                    }
                    if (e instanceof NullPointerException && e.getCause() instanceof ConnectionClosedException) {
                        return;
                    }
                    if (e instanceof ConnectionClosedException) {
                        return;
                    }
                    Thread.currentThread().getUncaughtExceptionHandler()
                            .uncaughtException(Thread.currentThread(), e);
                    return;
                });
                repository = getRxJdbcRepository(config);
            }

            if (repository != null) {
                routingBuilder
                        .register(new DbService(repository))
                        .register(new FortuneService(repository, getTemplate()));
            }
        }

        return routingBuilder.build();
    }

    /**
     * Application main entry point.
     *
     * @param args command line arguments.
     * @throws IOException if there are problems reading logging properties
     */
    public static void main(final String[] args) throws IOException {
        startServer();
    }

    /**
     * Start the server.
     *
     * @return the created {@link WebServer} instance
     * @throws IOException if there are problems reading logging properties
     */
    protected static WebServer startServer() throws IOException {

        // load logging configuration
        LogManager.getLogManager().readConfiguration(
                Main.class.getResourceAsStream("/logging.properties"));

        // By default this will pick up application.yaml from the classpath
        Config config = Config.create();

        // Get webserver config from the "server" section of application.yaml
        ServerConfiguration serverConfig =
                ServerConfiguration.fromConfig(config.get("server"));

        WebServer server = WebServer.create(serverConfig, createRouting(config));

        // Start the server and print some info.
        server.start().thenAccept(ws -> System.out.println("WEB server is up! http://localhost:" + ws.port()));

        // Server threads are not demon. NO need to block. Just react.
        server.whenShutdown().thenRun(() -> System.out.println("WEB server is DOWN. Good bye!"));

        return server;
    }
}
