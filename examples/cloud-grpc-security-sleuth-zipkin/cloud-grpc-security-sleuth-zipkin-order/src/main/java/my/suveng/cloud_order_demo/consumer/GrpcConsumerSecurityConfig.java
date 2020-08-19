package my.suveng.cloud_order_demo.consumer;

import io.grpc.CallCredentials;
import net.devh.boot.grpc.client.security.CallCredentialsHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 这里的grpc客户端是使用basic auth
 * @author suwenguang
 **/
@Configuration
public class GrpcConsumerSecurityConfig {

	private final String username = "user" ;

	/**
	 * Create credentials for username + password.
	 */
	@Bean
	CallCredentials grpcCredentials() {
		return CallCredentialsHelper.basicAuth(this.username, this.username + "Password");
	}

}
