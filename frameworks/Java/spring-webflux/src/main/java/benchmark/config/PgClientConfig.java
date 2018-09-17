package benchmark.config;

import io.reactiverse.pgclient.PgClient;
import io.reactiverse.pgclient.PgPool;
import io.reactiverse.pgclient.PgPoolOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("pgclient")
@ConfigurationProperties(prefix = "database")
public class PgClientConfig {
    private String name;
    private String host;
    private int port;
    private String username;
    private String password;

    @Bean
    public PgPool pgClient() {
        PgPoolOptions options = new PgPoolOptions();
        options.setDatabase(name);
        options.setHost(host);
        options.setPort(port);
        options.setUser(username);
        options.setPassword(password);
        options.setCachePreparedStatements(true);
        options.setMaxSize(1);
        return PgClient.pool(options);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
