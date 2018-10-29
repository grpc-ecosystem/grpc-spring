# How to contribute

We definitely welcome your patches and contributions to gRPC-Spring-Boot-Starter!

If you are new to github, please start by reading [Pull Request howto](https://help.github.com/articles/about-pull-requests/)

##Code Formatting
Code formatting is enforced using the [Spotless](https://github.com/diffplug/spotless)
Gradle plugin or Maven plugin. You can use `gradle spotlessJavaApply` or `mvn spotless:apply`
 to format new code. Formatter and import order settings for Eclipse are
available in the repository under
[extra/eclipse-formatter.xml](extra/eclipse/eclipse-formatter.xml)
and [extra/eclipse.importorder](extra/eclipse/eclipse.importorder),
respectively. 

For IntelliJ IDEA there's a
[plugin](https://plugins.jetbrains.com/plugin/6546) you can use in conjunction with the
Eclipse settings.
