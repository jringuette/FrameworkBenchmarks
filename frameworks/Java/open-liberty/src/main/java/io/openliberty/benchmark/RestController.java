package io.openliberty.benchmark;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Collections;

@Path("/")
public class RestController {

    @GET
    @Path("plaintext")
    @Produces(MediaType.TEXT_PLAIN)
    public String plaintext() {
        return "Hello, World!";
    }

    @GET
    @Path("json")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject json() {
        return Json.createObjectBuilder(Collections.singletonMap("message", "Hello, World!")).build();
    }
}