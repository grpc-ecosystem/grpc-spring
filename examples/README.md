# gRPC spring boot starter Examples

## Local Mode

1. Try the local-grpc-server example first run:

    ````sh
    ./gradlew :example:local-grpc-server:bootRun
    ````

2. In a different terminal window run:

    ````sh
    ./gradlew :example:local-grpc-client:bootRun
    ````

3. Visit http://localhost:8080/ to see the result.

## Cloud Mode

1. Try the cloud-eureka-server example first run:

    ````sh
    ./gradlew :example:cloud-eureka-server:bootRun
    ````

2. Run zipkin-server

    https://github.com/openzipkin/zipkin#quick-start

3. In a different terminal window run:

    ````sh
    ./gradlew :example:cloud-grpc-server:bootRun
    ````

4. In a different terminal window run:

    ````sh
    ./gradlew :example:cloud-grpc-client:bootRun
    ````

5. Visit http://localhost:8080/ to see the result.

## With Basic auth security

1. Try the security-grpc-server example first run:

    ````sh
    ./gradlew :example:security-grpc-server:bootRun
    ````

2. In a different terminal window run:

    ````sh
    ./gradlew :example:security-grpc-client:bootRun
    ````

3. Visit http://localhost:8080/ to see the result.

*You can configure the client's username in the application.yml.*

## With Bearer auth security

1. Try the security-grpc-bearerAuth-server example first run:

    ````sh
    ./gradlew :example:security-grpc-bearerAuth-server:bootRun
    ````

2. In a different terminal window run:

    ````sh
    ./gradlew :example:security-grpc-bearerAuth-client:bootRun
    ````

3. Visit http://localhost:8080/ to see the result.

This will not run out of the box since one needs to set up an identity provider service, like
for example Keycloak. Keycloak provides an endpoint to retrieve the necessary configuration (Public RSA key, etc).
The URI to this endpoint needs to be provided in the server's `SecurityConfiguration.java` in the `jwtDecoder()` method.

Additionally you will need to obtain a valid access token from the Keycloak server. This token has to be provided in
the client's `SecurityConfiguration.java`

To obtain an access token you can use Postman and perform an HTTP POST call to:
`http://127.0.0.1:8080/auth/realms/YOURREALM/protocol/openid-connect/token`
with basic authentication. Username and password are the client id and secret of the client you configured in the
Keycloak admin panel (http://127.0.0.1:8080/).

*You can configure the bearer token in the `SecurityConfiguration.java`*

**Advice for testing/development:**

When testing/developing it is not always possible to have an IDP service ready. In that case you can add the following
line:

````java
providers.add(anonymousAuthenticationProvider());
````

right above (your actual authentication providers)

````java
providers.add(jwtAuthenticationProvider());
````

in the `authenticationManager()` bean method of the server's `SecurityConfiguration.java`
This will of course require an appropriate Bean like such:

````java
@Bean
AnonymousAuthenticationProvider anonymousAuthenticationProvider() {
   return new AnonymousAuthenticationProvider("dev");
}
````

and in the authenticationReader() Bean replace the return with:

````java
return new AnonymousAuthenticationReader("dev", "developer", AuthorityUtils.createAuthorityList("ROLE_TEST"));
````

You can add/change the roles there to your liking.

Overall what happens here is that the BearerAuthenticationReader is replaced by AnonymousAuthenticationReader, which
ignores the Bearer token and creates an AnonymousAuthenticationToken which is processed by the
`AnonymousAuthenticationProvider`. This way you can temporarily bypass the bearer token auth.
You might want to toggle this behavior with a `dev` or `debug` property.
