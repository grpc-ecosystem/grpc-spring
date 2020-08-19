# grpc security jwt with rsa public key demo

## 服务间的调用逻辑

`cloud-grpc-sleuth-zipkin-user`用`grpc`调用`cloud-grpc-sleuth-zipkin-pay`
`cloud-grpc-sleuth-zipkin-user`用`grpc`调用`cloud-grpc-sleuth-zipkin-order`
`cloud-grpc-api`是存放`grpc`的`proto`文件
查看`cloud-grpc-sleuth-zipkin-user`中的`controller`, 里面简单写了一些测试逻辑

## 整合了sleuth和zipkin做链路追踪

查看`cloud-grpc-sleuth-zipkin-user`的`application.yml`配置文件
其他模块类似, 查看配置即可, 这里没有使用服务发现组件, 写死了`ip`

## 整合了`oauth2`的`jwt`令牌

使用`RSA`的公钥解密`JWT`
重载了解析`token`的逻辑, 获取到更细的权限, 详细查看`cloud-grpc-security-sleuth-zipkin-pay`的`SercurityConfig.java`和`GrpcJwtConverter.java`
`cloud-grpc-security-sleuth-zipkin-pay`支持多种方式的认证授权, `basicAuth`和`JWT`
其他模块,我没有配置,但是可以参考 cloud-grpc-security-sleuth-zipkin-pay 的配置
其中测试的过程, 我是写在了 `cloud-grpc-security-sleuth-zipkin-user 的 controller` 的逻辑中, 具体可查看 `PayController`
建议打断点,观察, 这只是`demo`, 注意`@secured`注解, 还有`oauth-server`的用户的权限必须是`ROLE_`开头, 1. 在授权就定义好格式 2. 可以在`converter`里面拼接, 两种实现, 这里我使用第一种方式
注意: `token`的获取需要自行从`oauth server`中获取

测试步骤:
1. 启动`oauth-server`, 获取一个`token`, 查看`oauth-server`的配置
2. 在`cloud-grpc-sleuth-zipkin-user`中替换`token`
3. 启动所有服务, 测试`cloud-grpc-sleuth-zipkin-user`的`controller`的逻辑
4. 访问接口, 默认没有权限访问, 所以结果会是Permission denied, 但是修改源码, 给到对应权限之后, 就可以访问

![LyOQbX](https://gitee.com/suveng/upic/raw/master/uPic/LyOQbX.png)
