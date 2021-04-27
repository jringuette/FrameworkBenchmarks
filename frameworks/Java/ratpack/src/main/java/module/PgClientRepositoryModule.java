package module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import models.PgClientRepository;


public class PgClientRepositoryModule extends AbstractModule {
    @Provides
    @Singleton
    public PgClientRepository pgClientRepository(PgClients pgClients) {
        return new PgClientRepository(pgClients);
    }

    @Override
    protected void configure() {
    }
}
