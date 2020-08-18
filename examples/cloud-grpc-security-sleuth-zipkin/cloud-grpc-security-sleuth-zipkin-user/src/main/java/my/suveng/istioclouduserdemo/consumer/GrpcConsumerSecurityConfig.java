package my.suveng.istioclouduserdemo.consumer;

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
	//
	//private String username = "user" ;
	//
	//@Bean
	//	// Create credentials for username + password.
	//CallCredentials grpcCredentials() {
	//	return CallCredentialsHelper.basicAuth(this.username, this.username + "Password");
	//}

	/**
	 * 	todo:// 从外部的oauth2.0 server 中获取到的token
	 *
 	 */
	private final String token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE1OTc3ODE3NzEsInVzZXJfbmFtZSI6ImFkbWluIiwiYXV0aG9yaXRpZXMiOlsiUk9MRV9hZG1pbiIsIlJPTEVfZ3Vlc3QiXSwianRpIjoiMDJiNzgwMjYtNTMwMS00NmI5LWE3NzQtOTQwYTEyMDgwMmMwIiwiY2xpZW50X2lkIjoiY2xpZW50Iiwic2NvcGUiOlsiQSJdfQ.ePADrSiXxGI3UeYxxwUHlDRBwNVw1QzjjbOGWTR7mABC103StmlUHpc-H_pg02S4RJik_mvuTpc5dh8juD-z53basSq_LF1xH5s2zhmcpAtshDPZ1Ib3DPrkb2sBo51k_-ws2Ed3fgK59jqwRt0iB65_rKnGXDu07kwU83i5jz2dX6i19cpFsqvyYLwppltCUVf1gAME9up7gAZA-ysaTLZCZ_e-8g6pZokqhy38CHhbqMAWT7fR1UQBQA18FtUPNj9s3tWRoxtD7DJVzHYAFyJkn04gMZGglpKiBAQyWUdiWwm3_yRVDOk55A25T2qaYAcNrzpVhq58qlwwaKqpsQ";

	@Bean
	CallCredentials grpcCredentials() {
		// Create credentials bearer Token
		// 这里写入token, 但是像这种有时效性的token怎么在刷新这个token呢? 看了这个对象开放的api没有替换token的操作,可能还没看到底儿
		CallCredentials callCredentials = CallCredentialsHelper.bearerAuth(this.token);
		return callCredentials;
	}

}
