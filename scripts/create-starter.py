#!/usr/bin/env python3
"""Create an independent Android, iOS and HTML starter from the tested Koin example."""
import argparse
import re
from pathlib import Path
import shutil

root = Path(__file__).resolve().parents[1]
parser = argparse.ArgumentParser()
parser.add_argument('destination', type=Path, help='A new empty directory for the starter')
parser.add_argument('--version', help='Published Domposable version; defaults to this checkout version')
args = parser.parse_args()
destination = args.destination.resolve()
if destination.exists() and any(destination.iterdir()):
    parser.error('Destination must be a new or empty directory')
destination.mkdir(parents=True, exist_ok=True)
version = args.version or next(
    line.split('=', 1)[1].strip()
    for line in (root / 'version.properties').read_text().splitlines()
    if line.startswith('VERSION=')
)
if not re.fullmatch(r"(?:0|[1-9]\d*)\.(?:0|[1-9]\d*)\.(?:0|[1-9]\d*)", version):
    parser.error('Version must use MAJOR.MINOR.PATCH')
for relative in ('gradle', 'koin-example/src', 'koinStarterAndroid/src', 'koin-starter-ios'):
    shutil.copytree(root / relative, destination / relative, dirs_exist_ok=True)
for relative in ('gradlew', 'gradlew.bat', 'koin-example/build.gradle.kts', 'koinStarterAndroid/build.gradle.kts'):
    target = destination / relative
    target.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(root / relative, target)
(destination / 'settings.gradle.kts').write_text('''pluginManagement { repositories { google(); mavenCentral(); gradlePluginPortal() } }
dependencyResolutionManagement {
    repositories {
        providers.gradleProperty("domposable.consumer.repository").orNull?.let { maven { url = uri(it) } }
        google()
        mavenCentral()
    }
}
rootProject.name = "contacts-starter"
include(":koin-example", ":koinStarterAndroid")
''')
(destination / 'build.gradle.kts').write_text('''plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.ksp) apply false
}
allprojects { version = providers.gradleProperty("domposable.starter.version").get() }
''')
(destination / 'gradle.properties').write_text(
    f'domposable.starter.version={version}\nkotlin.mpp.applyDefaultHierarchyTemplate=false\n'
    'org.gradle.jvmargs=-Xmx3g -XX:MaxMetaspaceSize=768m\nandroid.useAndroidX=true\n'
)
(destination / 'README.md').write_text('''# Contacts starter

The shared screen uses Koin and a generated Domposable MVI destination. Edit the contact and use
Open Alex or Open Sam to navigate. The web app supports `#/contact/alex` and `#/contact/sam`.

Run `./gradlew :koinStarterAndroid:assembleDebug` for Android and
`./gradlew :koin-example:jsBrowserDevelopmentRun` for HTML. For iOS, run
`./gradlew :koin-example:linkDebugFrameworkIosSimulatorArm64`, then open
`koin-starter-ios/Contacts.xcodeproj` and run the Contacts scheme on an arm64 simulator.
The Release configuration uses `linkReleaseFrameworkIosSimulatorArm64`.

Set `domposable.starter.version` in `gradle.properties` to a published Domposable release.
''')
print(f'Created {destination} with Domposable {version}')
