package my.suveng.istiocloudpaydemo.config;

import com.alibaba.fastjson.JSON;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 自定义获取权限 converter
 * @author suwenguang
 **/
public class GrpcJwtConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

	/**
	 * Convert the source object of type {@code S} to target type {@code T}.
	 * @param source the source object to convert, which must be an instance of {@code S} (never {@code null})
	 * @return the converted object, which must be an instance of {@code T} (potentially {@code null})
	 * @throws IllegalArgumentException if the source cannot be converted to the desired target type
	 */
	@Override
	public Collection<GrantedAuthority> convert(Jwt source) {
		ArrayList<GrantedAuthority> authorities = new ArrayList<>();
		// 直接读取source的 map, 这里建议自行打断点进来看看, 我也是翻看了源码才知道可以自定义这个
		Map<String, Object> claims = source.getClaims();
		if (claims == null || claims.isEmpty()) {
			return authorities;
		}

		Object authoritiesObj = claims.get("authorities");
		if (authoritiesObj == null){
			return authorities;
		}

		List<String> array = JSON.parseArray(JSON.toJSONString(authoritiesObj), String.class);
		if (array == null || array.isEmpty()){
			return authorities;
		}

		for (String a : array) {
			authorities.add(new SimpleGrantedAuthority(a));
		}

		//source.get
		//OAuth2Authentication authentication = tokenStore.readAuthentication(source.getTokenValue());
		//Authentication userAuthentication = authentication.getUserAuthentication();
		//Collection<? extends GrantedAuthority> grantedAuthorities = userAuthentication.getAuthorities();
		return authorities;
	}

}
