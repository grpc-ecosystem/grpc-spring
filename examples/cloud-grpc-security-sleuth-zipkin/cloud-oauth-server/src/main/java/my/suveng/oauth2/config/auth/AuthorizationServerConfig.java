package my.suveng.oauth2.config.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

import javax.sql.DataSource;
import java.security.PublicKey;
import java.util.Map;

/**
 * 认证中心配置
 * @author suwenguang
 **/
@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {


	/**
	 * 注入默认实现了UserDetailsService的实现类
	 */
	@Autowired
	private UserDetailsService userDetailsService;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private AuthenticationManager authenticationManager;

	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
		// 设置令牌
		endpoints
				.userDetailsService(userDetailsService)
				.tokenStore(tokenStore())
				// 配置用于JWT私钥加密的增强器
				.tokenEnhancer(jwtTokenEnhancer())
				.authenticationManager(authenticationManager)
		;
	}


	@Override
	public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
		// 读取客户端配置
		clients.inMemory().withClient("client")
				.secret(passwordEncoder.encode("secret"))
				.authorizedGrantTypes("authorization_code","password","refresh_token")
				.scopes("A","B","C")
				.redirectUris("https://www.baidu.com");
	}


	@Override
	public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
		security.tokenKeyAccess("permitAll()")
				.checkTokenAccess("isAuthenticated()")//
				.allowFormAuthenticationForClients()//允许check_token
		;
	}


	/**
	 * 配置token JWT存储
	 * @author suwenguang
	 */
	@Bean
	public TokenStore tokenStore(){
		return new JwtTokenStore(jwtTokenEnhancer());
	}

	/**
	 * 配置JWT秘钥
	 * @author suwenguang
	 */
	@Bean
	protected JwtAccessTokenConverter jwtTokenEnhancer() {
		// 配置jks文件
		KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(new ClassPathResource("oauth-jwt.jks"), "suveng".toCharArray());
		JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
		converter.setKeyPair(keyStoreKeyFactory.getKeyPair("oauth-jwt"));
		PublicKey aPublic = keyStoreKeyFactory.getKeyPair("oauth-jwt").getPublic();
		Map<String, String> key = converter.getKey();
		return converter;
	}




}
