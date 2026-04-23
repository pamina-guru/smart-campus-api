package com.westminster.smartcampus.filter;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Cross-cutting logging for every request/response handled by the API.
 * Implementing both request and response filters in one class keeps
 * a unified view of traffic without scattering Logger calls through
 * every resource method.
 */
@Provider
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOGGER = Logger.getLogger(LoggingFilter.class.getName());

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        LOGGER.info("➡️  REQUEST  "
                + requestContext.getMethod()
                + "  "
                + requestContext.getUriInfo().getRequestUri());
    }

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {
        LOGGER.info("⬅️  RESPONSE "
                + requestContext.getMethod()
                + "  "
                + requestContext.getUriInfo().getRequestUri()
                + "  → "
                + responseContext.getStatus());
    }
}