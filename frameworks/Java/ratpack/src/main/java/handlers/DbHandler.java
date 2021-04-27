package handlers;

import models.DbRepository;
import ratpack.handling.Context;


import static ratpack.jackson.Jackson.json;

public class DbHandler extends BaseWorldHandler {
    public void handle(Context ctx, DbRepository repository) {
        repository.getWorld(randomWorldNumber()).then(result -> ctx.render(json(result)));
    }
}
