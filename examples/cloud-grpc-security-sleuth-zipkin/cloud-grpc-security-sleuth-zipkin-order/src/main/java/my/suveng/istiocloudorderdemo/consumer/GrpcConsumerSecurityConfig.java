package my.suveng.istiocloudorderdemo.consumer;

import io.grpc.CallCredentials;
import net.devh.boot.grpc.client.security.CallCredentialsHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author suwenguang
 **/
@Configuration
public class GrpcConsumerSecurityConfig {

	private final String username = "user" ;

	@Bean
		// Create credentials for username + password.
	CallCredentials grpcCredentials() {
		return CallCredentialsHelper.basicAuth(this.username, this.username + "Password");
	}

	// This token will usually be created by a login endpoint (e.g. from Keycloak).
	//private final String token = "1eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE1OTU1MTM1NzUsInVzZXJfbmFtZSI6IjM2OTgiLCJhdXRob3JpdGllcyI6WyJ0ZXN0LXVwZGF0ZSJdLCJqdGkiOiI1ZmY0NGU2Zi04MjBmLTRlMTctOTBiOS05M2Y5ZDFmODg5MTgiLCJjbGllbnRfaWQiOiJjbGllbnQiLCJzY29wZSI6WyJhcHAiXX0.jlC8OUlh-VsFUNfTD49kJfO_HD0Itaru5L0ngbA-NFO3ko285XUks9jpxzJeaoFOPDLR9zfTLb0FD3rlaMlq17xEX7e1ECGw3xRzcCt02A7hAcpDaka0Aaau5F-u_yl6emjPKJ0-bK6bktW9d6nXIimlaHJm7AUa9f2xTqucQLKEua51wCgbVKwY8G5wPhfNeKyXe5n7lm0XNURMSOmGb-XKcwMRo0TsQ1Td9jPsqSl3faqGEnUr4av6WSNj3psDoUZLlBAwToaeYHY6HeyrmCmxLCFSlTG9D2HZCxAinfylYprowrb9ECdMAQkeroyE66x8eXmusF_ZkQw4znM8jQ";
	//
	//@Bean
	//	// Create credentials bearer Token
	//CallCredentials grpcCredentials() {
	//	return CallCredentialsHelper.bearerAuth(this.token);
	//}

}
