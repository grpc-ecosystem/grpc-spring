# Versions

[<- Back to Index](index.md)

This page shows the additional information about our Versioning Policy and lifecycles.

## Table of Contents <!-- omit in toc -->

- [Versioning Policy](#versioning-policy)
- [Version Table](#version-table)
  - [Version 2.x](#version-2x)
  - [Version 1.x](#version-1x)
  - [Upgrading Dependencies](#upgrading-dependencies)
  - [Release Notes](#release-notes)

## Versioning Policy

The major version of this project defines which spring-boot version we are compatible with.

- 1.x.x versions are EOL and won't receive any updates
- 2.x.x is the current version and will be updated if there are spring-boot or gRPC releases.

The minor version defines the feature version of this project. Every time we bump spring-boot's or gRPC's version,
we will also increment our feature version. The same applies if we add/change major features.
In most cases you will not get any incompatibilities by upgrading, but since gRPC evolves just like its API,
this cannot be ruled out. We try to minimize such influences, but can't rule them out.
If you don't use advanced features, you won't usually notice.

We usually don't release patch versions, but include these patches in the next release.
If you need a patched version, please open an issue.

## Version Table

This table shows the spring and gRPC version that this library ships.
In most cases you can upgrade to newer versions, but especially gRPC changes its API more frequently.
Please report any issues to our [repo](https://github.com/yidongnan/grpc-spring-boot-starter/issues).

> **Note**
>
> If you are using the non-shaded netty (and related libraries) please stick **exactly** to the version that is
> [documented](https://github.com/grpc/grpc-java/blob/master/SECURITY.md#netty) by gRPC.
> (grpc-netty-shaded avoids these issues by keeping these versions in sync.)

### Version 2.x

Current version.

| Version | spring-boot | gRPC | Date |
|:-------:|:-----------:|:----:| ---: |
| 2.10.0* | 2.3.0 | 1.29.0 | TBA |
| 2.9.0* | 2.3.0 | 1.29.0 | Jun, 2020 |
| 2.8.0* | 2.2.7 | 1.29.0 | Jun, 2020 |
| 2.7.0 | 2.2.4 | 1.27.1 | Feb, 2020 |
| 2.6.2 | 2.2.1 | 1.25.0 | Jan, 2020 |
| 2.6.1 | 2.2.1 | 1.25.0 | NOV, 2019 |
| 2.6.0 | 2.2.1 | 1.24.2 | NOV, 2019 |
| 2.5.1 | 2.1.6 | 1.22.2 | Aug, 2019 |
| 2.5.0 | 2.1.6 | 1.22.1 | Aug, 2019 |
| 2.4.0 | 2.1.5 | 1.20.0 | Jun, 2019 |
| 2.3.0 | 2.1.4 | 1.18.0 | Apr, 2019 |
| 2.2.1 | 2.0.7 | 1.17.1 | Jan, 2019 |
| 2.2.0 | 2.0.6 | 1.17.1 | Dec, 2018 |
| 2.1.0 | 2.0.? | 1.14.0 | Oct, 2018 |
| 2.0.1 | 2.0.? | 1.14.0 | Aug, 2018 |
| 2.0.0 | 2.0.? | 1.13.1 | Aug, 2018 |

(* Future versions)

### Version 1.x

End of life - No more updates planned.

| Version | spring-boot | gRPC | Date |
|:-------:|:-----------:|:----:| ---: |
| 1.4.2 | 1.?.? | 1.12.0 | Jun, 2019 |
| 1.4.1 | 1.?.? | 1.12.0 | Jun, 2018 |
| ... | 1.?.? | N/A |

### Upgrading Dependencies

If you upgrade any of the versions we strongly recommend doing so using a bom:

- [spring-boot](https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-parent)
- [grpc-java](https://mvnrepository.com/artifact/io.grpc/grpc-bom)

### Release Notes

Refer to the release notes for more information on the changes for each version:

- [grpc-spring-boot-starter](https://github.com/yidongnan/grpc-spring-boot-starter/releases)
- [spring-boot](https://github.com/spring-projects/spring-boot/releases)
- [grpc-java](https://github.com/grpc/grpc-java/releases)

----------

[<- Back to Index](index.md)
