# Publishing to Maven Central

Releases are published automatically when a version tag is pushed. Supported tag formats are `1.2.3`, `v1.2.3`, and prerelease variants such as `v1.2.3-rc.1`.

## One-time setup

1. Sign in to the [Central Publisher Portal](https://central.sonatype.com/) and verify the `ac.intave` namespace.
2. Generate a Central Portal user token.
3. Create a password-protected OpenPGP signing key and publish its public key to a public keyserver.
4. Add these GitHub Actions repository secrets:

   - `MAVEN_CENTRAL_USERNAME`: the generated Portal token username
   - `MAVEN_CENTRAL_PASSWORD`: the generated Portal token password
   - `SIGNING_KEY`: the ASCII-armored private OpenPGP key, including its header and footer
   - `SIGNING_PASSWORD`: the private key passphrase

## Release

Create and push a tag for the release commit:

```shell
git tag -a v1.2.3 -m "Release 1.2.3"
git push origin v1.2.3
```

The GitHub Actions workflow validates the tag, runs the test suite, builds the main, sources, and Javadoc JARs, signs the publication, uploads it to the Central Publisher Portal, and requests automatic publication. Maven Central releases are immutable, so never reuse or move a version tag after it has been published.

For local checks without publishing, run `./gradlew clean check publishToMavenLocal`. The default local version is `0.0.1-SNAPSHOT`; pass `-PreleaseVersion=1.2.3` to inspect a release-versioned build.
