package com.westminster.smartcampus.mapper;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Safety-net mapper. Catches any Throwable that is NOT handled by a more
 * specific ExceptionMapper, preventing raw stack traces from leaking to clients.
 * The full stack trace is logged server-side for diagnostics.
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable ex) {
        // Let JAX-RS handle framework-level responses (e.g., 404 route miss)
        if (ex instanceof WebApplicationException wae) {
            return wae.getResponse();
        }

        LOGGER.log(Level.SEVERE, "Unhandled server exception", ex);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "Internal Server Error");
        body.put("message", "An unexpected error occurred. Please contact the administrator.");
        body.put("status", 500);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }
}