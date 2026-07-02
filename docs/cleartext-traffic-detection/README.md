# Cleartext Traffic Detection

SecureFlow detects Android XML configuration that explicitly allows cleartext network traffic.

This check is intended to catch application and network security settings that can permit unencrypted HTTP connections.

## Status

Implemented in the SecureFlow Gradle plugin.

Findings from this check are reported as `CRITICAL`.

## Supported Files

The scanner currently checks XML files: `.xml`.

The scanner skips generated and local build directories:

* `.git`
* `.gradle`
* `.idea`
* `build`
* `.externalNativeBuild`
* `.cxx`

## Detected Configuration

SecureFlow flags XML attributes that explicitly enable cleartext traffic:

* `android:usesCleartextTraffic="true"`
* `cleartextTrafficPermitted="true"`

## Examples

Android manifest:

```xml
<application
    android:name=".SecureFlowApp"
    android:usesCleartextTraffic="true" />
```

Network security config:

```xml
<domain-config cleartextTrafficPermitted="true">
    <domain includeSubdomains="true">example.com</domain>
</domain-config>
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
[CRITICAL] Cleartext traffic is enabled in Android manifest at src/main/AndroidManifest.xml:4
```

Example Markdown report row:

```markdown
| CRITICAL | CleartextTraffic | src/main/AndroidManifest.xml:4 | Cleartext traffic is enabled in Android manifest | `android:usesCleartextTraffic="true"` |
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

When `failOnFindings` is `true`, `secureFlowCheck` fails the build if cleartext traffic is detected.

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
