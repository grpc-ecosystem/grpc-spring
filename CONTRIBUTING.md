# How to contribute

We definitely welcome your patches and contributions to gRPC-Spring-Boot-Starter!

If you are new to github, please start by reading [Pull Request howto](https://help.github.com/articles/about-pull-requests/)

## Code Formatting

Code formatting is enforced using the [Spotless](https://github.com/diffplug/spotless) Gradle plugin.
You can use `gradle spotlessJavaApply` (java only) or `gradle spotlessApply` (all files)
to format new code. Please run this task before submitting your pull request.

### Eclipse

For the eclipse IDE we use the following formatter files:

* [extra/eclipse-formatter.xml](extra/eclipse/eclipse-formatter.xml)
* [extra/eclipse.importorder](extra/eclipse/eclipse.importorder)

These will help you maintaing the files order, if you run the formatter from eclipse.
There are slight differences to the `spotless` plugin so please run it before submitting your PR anyway.

### IntelliJ IDEA

For IntelliJ IDEA there's a [Eclipse Code Formatter plugin](https://plugins.jetbrains.com/plugin/6546) you can use in
conjunction with the Eclipse setting files.
