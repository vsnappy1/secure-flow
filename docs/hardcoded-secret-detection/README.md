# Hardcoded Secret Detection

SecureFlow detects suspicious hardcoded secrets in source and configuration files.

This check is intended to catch values that should usually be supplied through secure configuration, environment variables, CI secrets, backend-issued tokens, or platform-specific secret management instead of being committed to source control.

## Status

Implemented in the SecureFlow Gradle plugin.

Findings from this check are reported as `CRITICAL`.

## Supported Files

The scanner currently checks:

* Kotlin files: `.kt`, `.kts`
* Java files: `.java`
* XML files: `.xml`
* Gradle files: `.gradle`
* Properties files: `.properties`

The scanner skips generated and local build directories:

* `.git`
* `.gradle`
* `.idea`
* `build`
* `.externalNativeBuild`
* `.cxx`

## Detected Patterns

SecureFlow looks for suspicious secret names and recognizable token formats.

Examples include:

* API keys
* access tokens
* bearer tokens
* client secrets
* private keys
* AI provider keys
* Firebase server keys
* GitHub tokens
* Slack tokens
* AWS access key IDs
* SendGrid keys

## Examples

Kotlin:

```kotlin
private const val OPENAI_API_KEY = "sk-1234567890abcdefghijklmnop"
```

Properties:

```properties
firebase_server_key=AIzaSyAbCdEfGhIjKlMnOpQrStUvWxYz123456789
```

XML:

```xml
<meta-data
    android:name="firebase_server_key"
    android:value="AIzaSyAbCdEfGhIjKlMnOpQrStUvWxYz123456789" />
```

Bearer token:

```kotlin
val authorization = "Bearer abcdefghijklmnopqrstuvwxyz123456"
```

## Placeholder Handling

The scanner avoids reporting common placeholder values such as:

* `todo`
* `sample`
* `example`
* `placeholder`
* `your_...`
* `replace_me`
* Gradle placeholders like `${...}`
* Android string references like `@string/...`

## Reports

Findings are written to both report formats:

```text
build/reports/secure-flow/privacy-report.md
build/reports/secure-flow/privacy-report.json
```

Example terminal output:

```text
SecureFlow found 1 issue(s). Reports written to /path/to/project/build/reports/secure-flow
[CRITICAL] Suspicious hardcoded secret detected at src/main/java/NetworkModule.kt:12
```

Example Markdown report row:

```markdown
| CRITICAL | HardcodedSecret | src/main/java/NetworkModule.kt:12 | Suspicious hardcoded secret detected | `private const val OPENAI_API_KEY = "sk-1...mnop"` |
```

Secret evidence is redacted in reports by keeping a short prefix and suffix.

## Configuration

Configure SecureFlow in the Gradle project where the plugin is applied:

```kotlin
secureFlow {
    scanDirectory.set(layout.projectDirectory)
    reportDirectory.set(layout.buildDirectory.dir("reports/secure-flow"))
    failOnFindings.set(true)
}
```

Defaults:

* `scanDirectory`: current Gradle project directory
* `reportDirectory`: `build/reports/secure-flow`
* `failOnFindings`: `true`

When `failOnFindings` is `true`, `secureFlowCheck` fails the build if a hardcoded secret is detected.

Set it to `false` if you want to generate reports without failing the build:

```kotlin
secureFlow {
    failOnFindings.set(false)
}
```

## Running The Check

Run:

```bash
./gradlew secureFlowCheck
```

Generate reports:

```bash
./gradlew secureFlowReport
```

## Local Plugin Publishing

From this repository, publish the plugin to Maven local:

```bash
./gradlew -p secure-flow publishToMavenLocal
```

Then use it in another local project:

```kotlin
pluginManagement {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
```

```kotlin
plugins {
    id("dev.randos.secure-flow") version "0.1.0"
}
```
