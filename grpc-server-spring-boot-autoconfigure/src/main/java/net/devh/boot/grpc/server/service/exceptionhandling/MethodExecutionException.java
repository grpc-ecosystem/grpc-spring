
package net.devh.boot.grpc.server.service.exceptionhandling;

/**
 * TODO..
 *
 * @author Andjelko (andjelko.perisic@gmail.com)
 */
class MethodExecutionException extends RuntimeException {

    MethodExecutionException(String msg, Throwable e) {
        super(msg, e);
    }
}
