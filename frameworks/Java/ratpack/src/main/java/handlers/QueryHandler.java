package handlers;

import models.DbRepository;
import ratpack.handling.Context;


import static ratpack.jackson.Jackson.json;

public class QueryHandler extends BaseWorldHandler {
    public void handle(Context ctx, DbRepository repository) {
        int queries = parseQueryCount(ctx);

        repository.getWorlds(getNumbers(queries)).then(result -> ctx.render(json(result)));
    }
}
