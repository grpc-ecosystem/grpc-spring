package my.suveng.istiocloudpaydemo.config;

import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.security.authentication.BasicGrpcAuthenticationReader;
import net.devh.boot.grpc.server.security.authentication.BearerAuthenticationReader;
import net.devh.boot.grpc.server.security.authentication.CompositeGrpcAuthenticationReader;
import net.devh.boot.grpc.server.security.authentication.GrpcAuthenticationReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.util.FileCopyUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 资源服务配置
 * 配置jwt方式resource server
 *
 * @author suwenguang
 **/
@Configuration
@EnableResourceServer
@EnableGlobalMethodSecurity(securedEnabled = true, proxyTargetClass = true)
@Slf4j
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


	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(10);
	}


	@Bean
		// Add the authentication providers to the manager.
	AuthenticationManager authenticationManager() {
		final List<AuthenticationProvider> providers = new ArrayList<>();
		// 1. basic username+password 的 授权方式
		providers.add(daoAuthenticationProvider());
		log.info("加入 basic provider 认证");

		// 2. jwt token 授权方式
		Resource resource = new ClassPathResource("oauth-jwt-public.cer");
		try {
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			FileInputStream in = new FileInputStream(resource.getFile());

			//生成一个证书对象并使用从输入流 inStream 中读取的数据对它进行初始化。
			Certificate c = cf.generateCertificate(in);
			PublicKey publicKey = c.getPublicKey();
			JwtAuthenticationProvider jwtAuthenticationProvider = new JwtAuthenticationProvider(NimbusJwtDecoder.withPublicKey((RSAPublicKey) publicKey).build());
			// 自定义token extract
			JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
			// 这里是扩展,获取到权限
			GrpcJwtConverter grpcJwtConverter = new GrpcJwtConverter();
			jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grpcJwtConverter);
			jwtAuthenticationProvider.setJwtAuthenticationConverter(jwtAuthenticationConverter);
			providers.add(jwtAuthenticationProvider);
			log.info("加入 jwt token provider 认证");

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return new ProviderManager(providers);
	}

	@Bean
		// Configure which authentication types you support.
	GrpcAuthenticationReader authenticationReader() {
		//return new BasicGrpcAuthenticationReader();
		final List<GrpcAuthenticationReader> readers = new ArrayList<>();
		log.info("增加basic头读取");
		readers.add(new BasicGrpcAuthenticationReader());
		log.info("增加beartoken头读取");
		readers.add(new BearerAuthenticationReader(BearerTokenAuthenticationToken::new));
		// 复合类型
		log.info("复合认证读取构建完成");
		return new CompositeGrpcAuthenticationReader(readers);
	}


	@Bean
		// This could be your database lookup. There are some complete implementations in spring-security-web.
	UserDetailsService userDetailsService() {
		return username -> {
			log.debug("Searching user: {}", username);
			switch (username) {
				case "guest": {
					return new User(username, passwordEncoder().encode(username + "Password"), Collections.emptyList());
				}
				case "user": {
					final List<SimpleGrantedAuthority> authorities =
						Arrays.asList(new SimpleGrantedAuthority("ROLE_GREET"));
					return new User(username, passwordEncoder().encode(username + "Password"), authorities);
				}
				default: {
					throw new UsernameNotFoundException("Could not find user!");
				}
			}
		};
	}

	@Bean
		// One of your authentication providers.
		// They ensure that the credentials are valid and populate the user's authorities.
	DaoAuthenticationProvider daoAuthenticationProvider() {
		final DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
		provider.setUserDetailsService(userDetailsService());
		provider.setPasswordEncoder(passwordEncoder());
		return provider;
	}
}
