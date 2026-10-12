# Binary API compatibility

An already compiled consumer can depend on a method's exact binary signature. Removing a function, moving its JVM file facade, or adding a parameter to a public data-class constructor can break that consumer even when Kotlin source still compiles after rebuilding. Adding a default parameter does not preserve the old constructor or `copy` signature automatically.

Domposable uses [Kotlin's built-in ABI validation](https://kotlinlang.org/docs/gradle-binary-compatibility-validation.html). Every published module enables it. CI's dedicated **Binary API compatibility** job is required by **CI passed**, which also gates release candidates.

## What the check covers

| Binary format | Reviewed baselines | Targets |
| --- | ---: | --- |
| JVM class signatures | 15 | Seven Android variants, seven JVM variants, JVM KSP processor |
| Klib signatures | 7 | JS, Wasm JS, iOS Arm64 and iOS simulator Arm64 in each runtime module |

`config/binary-api.json` declares the eight published modules and expected Klib targets. The inventory check rejects missing/empty dumps, absent Klib targets, unexpected baseline paths and published modules omitted from that policy. macOS CI disables target inference so these targets must actually compile.

The Gradle check compares compiled signatures with the checked-in baselines under each module's `api/` directory. Any difference fails, including an additive overload. CI also generates independent diffs against the PR base and the latest semver release tag in the checkout's ancestry, so updating a baseline does not erase the review history. Git tags and full history are required; visual-evidence buckets and prerelease tags are excluded.

The `binary-api` Actions artifact retains the reviewed and generated dumps, JSON/Markdown inventory and full comparison diffs for **30 days**. Compiler mismatch diagnostics appear in the job log. The job summary lists changed files and added/removed line counts. Counts describe text changes; they are not a semantic compatibility classifier.

## Run locally

```sh
# Complete verification on macOS, with the repository's Java/Android/Kotlin toolchains.
./gradlew binaryApiCheck -Pdomposable.abi.strict=true
python3 scripts/ci/binary_api.py --base origin/main

# Other hosts can validate available targets; CI still requires the strict macOS run.
./gradlew binaryApiCheck
```

`checkKotlinAbi` remains available. `binaryApiCheck` explicitly depends on the check for every published module in the policy, so adding a module requires adding its baselines and policy entry.

## Review an intentional change

1. Inspect the failure and retain existing signatures where practical, for example by adding an overload instead of replacing one.
2. On macOS, run `./gradlew updateKotlinAbi -Pdomposable.abi.strict=true`. Inspect every baseline change before committing it.
3. Include a realistic consumer sample and an **API compatibility** section in the PR. `PR review contract` requires the fields below for any changed/deleted baseline.
4. Run strict verification again. Review the release diff as well as the PR diff before choosing the next release version.

```markdown
## API compatibility

- Classification: additive; the new overload leaves existing binary signatures available.
- Migration: none; current callers keep using the existing overload.
- Release: minor; adds an optional public capability.
```

Use `breaking` when an existing binary contract changes, and describe the required consumer migration or recompilation. Use `tooling-only` for a dump-format/compiler change only after inspecting the generated binaries. Before 1.0, intentional breaking changes require a **minor** release with migration notes; patch releases preserve the existing API. From 1.0 onward, breaking changes require a **major** release. Version changes still use the separately approved release workflow.

Updating the dump makes the compiler check pass; it does not make a change compatible. The PR explanation and maintainer review are required for that decision. This checks exposed signatures, not all source compatibility, runtime behavior, reflection, accessibility or Swift-exported interfaces. Published-consumer builds and platform behavior tests remain separate checks.

## API changes since 0.3.0

After 0.3.0, portable product modifiers and typed component extensions add public contracts.
The provider adds six required renderer fields. Expanded text/input/choice props retain source defaults but change their constructor and generated copy
signatures. Consumers must recompile when adopting 0.4.0. Review the exact PR
and latest-release ABI reports and the [migration guide](migration.md) before publication.
