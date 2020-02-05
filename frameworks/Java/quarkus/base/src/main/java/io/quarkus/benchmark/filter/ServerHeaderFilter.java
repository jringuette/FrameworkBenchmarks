package io.quarkus.benchmark.filter;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.util.HttpHeaderNames;

@Provider
public class ServerHeaderFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        responseContext.getHeaders().add("Server", "Quarkus");
        responseContext.getHeaders()
                .add(HttpHeaderNames.DATE, DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now()));
    }

}