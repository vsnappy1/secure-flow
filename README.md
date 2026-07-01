<p>
  <a href="https://plugins.gradle.org/plugin/dev.randos.secure-flow"><img alt="License" src="https://img.shields.io/maven-metadata/v?label=Gradle%20Plugin%20Portal&metadataUrl=https%3A%2F%2Fplugins.gradle.org%2Fm2%2Fdev%2Frandos%2Fsecure-flow%2Fdev.randos.secure-flow.gradle.plugin%2Fmaven-metadata.xml"/></a>
  <a href="https://github.com/vsnappy1/secure-flow/actions"><img alt="Build Status" src="https://github.com/vsnappy1/secure-flow/workflows/Android%20CI/badge.svg"/></a>
  <a href="https://opensource.org/licenses/Apache-2.0"><img alt="License" src="https://img.shields.io/badge/License-Apache%202.0-blue.svg"/></a>
</p>

# SecureFlow
SecureFlow is an open-source Android security and privacy automation toolkit designed to help developers identify risky patterns before they reach production. The current MVP provides Gradle-based hardcoded secret and unsafe logging scanners, with planned Android lint rules for network configuration, exported components, WebView risks, and AI prompt privacy review.

Modern Android applications often process sensitive user data through analytics, logs, backend APIs, AI workflows, and third-party SDK integrations. SecureFlow brings lightweight, CI-friendly privacy and security checks into the Android development workflow so teams can detect issues earlier, reduce manual review effort, and strengthen release readiness.

## Purpose

The goal of this project is to make privacy and security review more practical for Android engineering teams by converting common review concerns into repeatable automated checks.

SecureFlow focuses on:

* Reducing accidental exposure of sensitive user data,
* Identifying unsafe logging patterns,
* Detecting risky network and manifest configurations,
* Improving release-readiness checks,
* Supporting privacy-aware AI workflows,
* And helping teams adopt consistent secure development practices.

## Why This Matters

AI-enabled and data-driven mobile applications often process user-generated content, prompts, metadata, analytics events, and backend payloads. Without proper safeguards, sensitive information can accidentally appear in logs, prompts, crash reports, analytics events, or misconfigured network requests.

SecureFlow helps developers catch these issues early by scanning Android source code and project configuration for patterns that deserve review.

## Current Status

SecureFlow is currently in early MVP development.

The first implemented capabilities are Gradle-based hardcoded secret detection and unsafe logging detection. These checks scan source and configuration files for suspicious secrets and risky logging statements that reference sensitive values.

Implemented:

* Hardcoded Secret Detection
* Unsafe Logging Detection
* Markdown report generation
* JSON report generation
* Configurable Gradle extension
* Build failure support through failOnFindings
* Local Maven publishing support

Planned next:

* Cleartext traffic detection
* Exported Android component review
* Risky WebView configuration detection
* AI prompt privacy review
* Android lint integration
* Android Studio plugin support

## Planned MVP Checks

### 1. Hardcoded Secret Detection

Detects suspicious hardcoded values in Kotlin, Java, XML, Gradle, and properties files.

Status: implemented. See [Hardcoded Secret Detection](docs/hardcoded-secret-detection/README.md) for details.

Example patterns:

* API keys
* access tokens
* bearer tokens
* client secrets
* AI provider keys
* Firebase server keys

### 2. Unsafe Logging Detection

Flags production logging statements that may expose sensitive values.

Status: implemented. See [Unsafe Logging Detection](docs/unsafe-logging-detection/README.md) for details.

Example patterns:

```kotlin
Log.d("User", user.email)
Log.e("AI_REQUEST", prompt)
println(accessToken)
```

### 3. Cleartext Traffic Detection

Detects potentially unsafe network configuration such as:

```xml
android:usesCleartextTraffic="true"
```

### 4. Exported Component Review

Flags exported Android components that may need additional permission protection.

Example:

```xml
<activity
    android:name=".InternalActivity"
    android:exported="true" />
```

### 5. Risky WebView Configuration

Detects WebView settings that may require additional review, such as JavaScript enablement or JavaScript bridge usage.

Example:

```kotlin
webView.settings.javaScriptEnabled = true
webView.addJavascriptInterface(...)
```

### 6. AI Prompt Privacy Review

Detects code patterns where raw user input may be sent into AI prompts or backend AI workflows without visible minimization or redaction.

Example:

```kotlin
val prompt = "Summarize this user content: $userInput"
```

The goal is not to block AI usage, but to encourage safer AI data handling practices.

## Example Usage

Apply the plugin:

```kotlin
plugins {
    id("dev.randos.secure-flow") version "0.1.0"
}
```

Run the secure flow scan:

```bash
./gradlew secureFlowCheck
```

Generate a report:

```bash
./gradlew secureFlowReport
```

Example output:

```text
SecureFlow Report
----------------------------

Critical: 2
Warning: 4
Info: 3

[Critical] Hardcoded secret detected in NetworkModule.kt
[Critical] Cleartext traffic enabled in AndroidManifest.xml
[Warning] Raw AI prompt logged in RecipeAIService.kt
[Warning] WebView JavaScript enabled in WebViewScreen.kt
```

## Report Formats

The MVP will support:

* Markdown report
* JSON report

Planned output:

```text
build/reports/secure-flow/privacy-report.md
build/reports/secure-flow/privacy-report.json
```

## CI/CD Integration

SecureFlow is designed to run in CI pipelines such as GitHub Actions, Jenkins, Bitrise, and CircleCI.

Example GitHub Actions usage:

```yaml
name: Secure Flow

on:
  pull_request:
  push:
    branches: [ main ]

jobs:
  secure-flow:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: 17

      - name: Run Secure Flow
        run: ./gradlew secureFlowCheck
```

## Project Roadmap

### Phase 1: MVP

* Gradle plugin
* Basic file scanning
* Privacy/security rule engine
* Markdown and JSON reports
* Sample Android app
* GitHub Actions example

### Phase 2: Android Lint Integration

* Custom Android lint rules
* IDE-visible warnings
* Lint baseline support
* Severity configuration

### Phase 3: Android Studio Plugin

* Tool window inside Android Studio
* Project privacy score
* Quick navigation to risky files
* Rule documentation inside IDE
* Suggested remediation steps

### Phase 4: Advanced Privacy-Aware AI Workflow Checks

* Prompt minimization checks
* AI payload logging detection
* Sensitive field redaction suggestions
* AI provider configuration review
* Privacy policy and implementation alignment checklist

## Vision

SecureFlow aims to become a practical open-source tool for Android developers who want to build secure, privacy-aware, and production-ready mobile applications.

The long-term goal is to help engineering teams move privacy and security checks earlier in the software development lifecycle, where issues are easier and less expensive to fix.
