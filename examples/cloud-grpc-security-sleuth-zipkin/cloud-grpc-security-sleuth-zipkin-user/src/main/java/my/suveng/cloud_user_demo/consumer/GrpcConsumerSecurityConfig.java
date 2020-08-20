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
	private final String token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE1OTc5NDY1MjksInVzZXJfbmFtZSI6ImFkbWluIiwiYXV0aG9yaXRpZXMiOlsiUk9MRV9hZG1pbiIsIlJPTEVfZ3Vlc3QiXSwianRpIjoiMjY0ODkxMTEtNDcwZi00OGNlLTlhNTAtMmZlOWQ0MmU4NjRhIiwiY2xpZW50X2lkIjoiY2xpZW50Iiwic2NvcGUiOlsiQSJdfQ.Kigmz97RLGtCyja8XA3dYvFEa4XBp2_Xe5e9zHghXLkc8wwXiUq-XX7sJndmTY5LZBed1WthZmU7eAwQCHADjEAjSZO8fKT_j4ET6wCXSPqMS649GeitLg8JDc_nhdvRKyjCv4wLqbvlCEPAA0jBje_RpI4ameA64Xrmi3APVmqOMqNbv8-60BztYbbJ-wqInfoeIOSvV8JkI8KPlUqT4-7dIHg1fJHz82yFuON_MHPFGpDLXp5u1jgmUAUsQI2-ryB1-s9AcR5awX4kiiOlyVYTV5T9nIHF63U8uk8C4GvV1RamCXqziiV0mFAJ5cVsjIFOtNvyNvYSCMtE1PUlTA";

	@Bean
	CallCredentials grpcCredentials() {
		// Create credentials bearer Token
		// todo:// 这里写入token, 但是像这种有时效性的token怎么在刷新这个token呢? 看了这个对象开放的api没有替换token的操作,可能还没看到底儿
		CallCredentials callCredentials = CallCredentialsHelper.bearerAuth(this.token);
		return callCredentials;
	}

}
