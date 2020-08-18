package my.suveng.istioclouduserdemo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.config.annotation.authentication.configuration.EnableGlobalAuthentication;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;

/**
 * 资源服务配置
 * 配置jwt方式resource server
 *
 * @author suwenguang
 **/
@Configuration
@EnableResourceServer
@EnableGlobalMethodSecurity(securedEnabled = true,proxyTargetClass = true)
public class SercurityConfig extends ResourceServerConfigurerAdapter {

	@Autowired
	@Qualifier("resourceTokenStore")
	TokenStore tokenStore;

	/**
	 * 资源安全权限配置
	 * @author suwenguang
	 */
	@Override
	public void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
			.antMatchers("/api/**").permitAll()
			.anyRequest()
			.authenticated()
			.and()
			.requestMatchers()
			.antMatchers("/**");
	}

	/**
	 * 配置resource使用jwt公钥解密
	 * @author suwenguang
	 */
	@Override
	public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
		resources.tokenStore(tokenStore);
	}


	/**
	 * resource的TokenStore
	 * @author suwenguang
	 */
	@Bean
	@Qualifier("resourceTokenStore")
	public TokenStore tokenStore() {
		return new JwtTokenStore(jwtTokenEnhancer());
	}


	/**
	 * resource的jwt的RSA公钥解密配置
	 * @author suwenguang
	 */
	@Bean(name = "publicJwtTokenEnhancer")
	public JwtAccessTokenConverter jwtTokenEnhancer() {
		// 用作JWT转换器
		JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
		Resource resource = new ClassPathResource("oauth-public.cert");
		String publicKey;
		try {
			publicKey = new String(FileCopyUtils.copyToByteArray(resource.getInputStream()));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		//设置公钥
		converter.setVerifierKey(publicKey);
		return converter;
	}
}
