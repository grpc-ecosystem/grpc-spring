package my.suveng.cloud_user_demo.consumer;

import io.grpc.CallCredentials;
import net.devh.boot.grpc.client.security.CallCredentialsHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 这里的授权方式使用bearerToken JWT格式
 * @author suwenguang
 **/
@Configuration
public class GrpcConsumerSecurityConfig {


	/**
	 * 	todo:// 从外部的oauth2.0 server 中获取到的token
	 *
 	 */
	private final String token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE1OTc4ODA5NDEsInVzZXJfbmFtZSI6ImFkbWluIiwiYXV0aG9yaXRpZXMiOlsiUk9MRV9hZG1pbiIsIlJPTEVfZ3Vlc3QiXSwianRpIjoiMjgxNThiMzUtNDAzMS00ODFhLTlkMzUtMTI4ZWQ5YmRmMmQ4IiwiY2xpZW50X2lkIjoiY2xpZW50Iiwic2NvcGUiOlsiQSJdfQ.S3j47Xo9K--YIYyY4ppHHUZUvXyeaQymU0l6cdnQldEH4tXAoqZJWM8eSl52AngFbHRIApx48_DV_RI7ZNAMsvDJna2tB-gs8XaiQGE4fCuiH3gDxq6yz_EirYsXR0_vc0QVGHY-OxrnQdKuJs5MjQMut6nmGnrVmp3xnYeDHmLVdylRe137fW7mxjLE90GC4U74HX5H356iz5Ih9-jM8-RG6cJjb3aGoBO5jF8DprYB5VXQSSZqTnc_91p_3BxxGLMfrHaQ5Jfw8BlzxawXhCDgsNwtxHR03LklDKd-4U8CkEozsF7y-TVdxKocEbBqGai9FmCoSTXreqgolt1OmA";

	@Bean
	CallCredentials grpcCredentials() {
		// Create credentials bearer Token
		// todo:// 这里写入token, 但是像这种有时效性的token怎么在刷新这个token呢? 看了这个对象开放的api没有替换token的操作,可能还没看到底儿
		CallCredentials callCredentials = CallCredentialsHelper.bearerAuth(this.token);
		return callCredentials;
	}

}
