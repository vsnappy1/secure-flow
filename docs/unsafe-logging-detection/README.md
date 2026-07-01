# Unsafe Logging Detection

SecureFlow detects logging statements that may expose sensitive values in source code.

This check is intended to catch values that should usually not be written to Android logs, console output, CI logs, or crash collection pipelines.

## Status

Implemented in the SecureFlow Gradle plugin.

Findings from this check are reported as `WARNING`.

## Supported Files

The scanner currently checks:

* Kotlin files: `.kt`, `.kts`
* Java files: `.java`

The scanner skips generated and local build directories:

* `.git`
* `.gradle`
* `.idea`
* `build`
* `.externalNativeBuild`
* `.cxx`

## Detected Logging APIs

SecureFlow checks common Android and JVM logging calls:

* `Log.v`, `Log.d`, `Log.i`, `Log.w`, `Log.e`, `Log.wtf`
* `Timber.v`, `Timber.d`, `Timber.i`, `Timber.w`, `Timber.e`, `Timber.wtf`
* `println`, `print`
* `System.out.print`, `System.out.println`, `System.out.printf`
* `System.err.print`, `System.err.println`, `System.err.printf`

## Sensitive References

The scanner flags logging calls when the logged expression references sensitive identifiers such as:

* access, auth, bearer, ID, or refresh tokens
* API keys, private keys, and secrets
* passwords, passcodes, PINs, and credentials
* authorization headers, cookies, sessions, and JWTs
* email, phone, address, SSN, date of birth, and similar PII
* latitude, longitude, GPS, and location fields
* AI prompts, raw user input, raw requests, and raw responses

## Examples

Kotlin:

```kotlin
Log.d("User", user.email)
Log.e("AI_REQUEST", prompt)
println(accessToken)
Log.d("Auth", "token=$accessToken")
```

Java:

```java
System.out.println(refreshToken);
```

## Reports

Findings are written to both report formats:

```text
build/reports/secure-flow/privacy-report.md
build/reports/secure-flow/privacy-report.json
```

Example terminal output:

```text
SecureFlow found 1 issue(s). Reports written to /path/to/project/build/reports/secure-flow
[WARNING] Sensitive value may be written to logs at src/main/java/ProfileScreen.kt:12
```

Example Markdown report row:

```markdown
| WARNING | UnsafeLogging | src/main/java/ProfileScreen.kt:12 | Sensitive value may be written to logs | `Log.d("User", user.email)` |
```

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

When `failOnFindings` is `true`, `secureFlowCheck` fails the build if unsafe logging is detected.

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
