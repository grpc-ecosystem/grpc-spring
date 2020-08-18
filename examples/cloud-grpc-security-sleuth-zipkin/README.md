# demo
文档 [中文] [English](spring/cloud-grpc/README-en.md)

## 服务间的调用逻辑

cloud-grpc-sleuth-zipkin-user 用 grpc 调用 cloud-grpc-sleuth-zipkin-pay
cloud-grpc-sleuth-zipkin-user 用 grpc 调用 cloud-grpc-sleuth-zipkin-order
cloud-grpc-api 是存放 grpc 的 proto 文件

查看 cloud-grpc-sleuth-zipkin-user 中的 controller, 里面简单写了一些测试逻辑


## 整合了sleuth和zipkin做链路追踪

查看 cloud-grpc-sleuth-zipkin-user 的 application.yml 配置文件
其他模块类似, 查看配置即可, 这里没有使用服务发现组件, 写死了 ip

## 整合了oauth2的jwt令牌

使用RSA的公钥解密jwt
重载了解析token的逻辑, 获取到更细的权限, 详细查看 cloud-grpc-security-sleuth-zipkin-pay 的 SercurityConfig.java 和 GrpcJwtConverter.java
cloud-grpc-security-sleuth-zipkin-pay 支持多种方式的认证授权, basicAuth 和 JWT
其他模块,我没有配置,但是可以参考 cloud-grpc-security-sleuth-zipkin-pay 的配置

其中测试的过程, 我是写在了 cloud-grpc-security-sleuth-zipkin-user 的 controller 的逻辑中, 具体可查看 PayController
建议打断点,观察, 这只是demo, 注意 @secured 注解, 还有oauth-server的用户的权限必须是 ROLE开头, 1. 在授权就定义好格式 2. 可以在converter里面拼接, 两种实现, 这里我使用第一种方式
注意: token的获取需要自行从oauth server 中获取

测试步骤:
1. 启动 oauth-server , 获取一个token, 查看oauth-server的配置
2. 在 cloud-grpc-sleuth-zipkin-user 中 替换token
3. 启动所有服务, 测试 cloud-grpc-sleuth-zipkin-user 的 controller 的逻辑






