package net.devh.oauth2.config.auth;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * user信息获取的逻辑
 * @author suwenguang
 **/
@Service
@Slf4j
public class UserDetailServiceImpl implements UserDetailsService {

    /**
     * 默认的加密器
     */
    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (StrUtil.isBlank(username)) {
            log.error("用户名不能为空");
            throw new UsernameNotFoundException("用户名不能为空");
        }

        //user信息, 写死, 后续根据自己的需求, 使用MySQL或者其他数据库中获取用户信息
        ArrayList<GrantedAuthority> authorities = new ArrayList<>();
        // 必须带前缀, 否则在grpc server 的 @secured 不生效, 具体看看源码
        authorities.add(new SimpleGrantedAuthority("ROLE_admin"));
        authorities.add(new SimpleGrantedAuthority("ROLE_guest"));
        User user = new User("admin", passwordEncoder.encode("admin"), authorities);

        if (ObjectUtil.isEmpty(user)) {
            log.error("用户信息为空");
            throw new UsernameNotFoundException("用户信息为空");
        }

        return user;
    }

}
