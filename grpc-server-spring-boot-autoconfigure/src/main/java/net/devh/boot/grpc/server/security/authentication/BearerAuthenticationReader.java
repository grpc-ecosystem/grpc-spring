package net.devh.boot.grpc.server.security.authentication;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

/**
 * Reads {@link PreAuthenticatedAuthenticationToken bearer token} from the request.
 *
 * @author Gregor Eeckels (gregor.eeckels@gmail.com)
 */
@Slf4j
public class BearerAuthenticationReader implements GrpcAuthenticationReader{
    private static final String PREFIX = "bearer ";
    private static final int PREFIX_LENGTH = PREFIX.length();

    @Override
    public Authentication readAuthentication(final ServerCall<?, ?> call, final Metadata headers)
            throws AuthenticationException {
        final String header = headers.get(AUTHORIZATION_HEADER);

        if (header == null || !header.toLowerCase().startsWith(PREFIX)) {
            log.debug("No bearer auth header found");
            throw new BadCredentialsException("Auth Header is not properly formatted");
        }

        // Cut away the "bearer " prefix
        final String accessToken = header.substring(PREFIX_LENGTH);


        return new PreAuthenticatedAuthenticationToken(accessToken, null);
    }
}
