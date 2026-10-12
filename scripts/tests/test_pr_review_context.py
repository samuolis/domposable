import importlib.util
from pathlib import Path
import unittest

spec = importlib.util.spec_from_file_location('review_context', Path(__file__).resolve().parents[1]/'pr_review_context.py')
review = importlib.util.module_from_spec(spec); spec.loader.exec_module(review)

class ReviewContractTest(unittest.TestCase):
    body = '''## Summary
- Add a product flow.
## Architecture
### Architecture at a glance
- The application owns models; renderers own platform nodes.
```mermaid
flowchart LR
  Feature --> Renderer
```
## API example
```kotlin
DomposableButton("Add", onAdd)
```
## Build changes
```sh
./gradlew checkKotlinAbi
```
## Visual evidence
![Android added storefront](https://raw.githubusercontent.com/samuolis/domposable/aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa/previews/android/shop.png)
## Verification
- Relevant platform tests pass.
'''
    def test_doc_only_does_not_require_unrelated_sections(self):
        self.assertEqual([], review.validate_description('## Summary\n- Explain installation.\n## Verification\n- Strict docs pass.', ['docs/getting-started.md']))
    def test_required_api_architecture_build_and_visual_sections(self):
        paths = ['domposable-ui/src/commonMain/Components.kt','demo/src/commonMain/Shop.kt','demo/build.gradle.kts']
        self.assertEqual([], review.validate_description(self.body, paths))
        for heading in ['Architecture','API example','Build changes','Visual evidence']:
            self.assertTrue(review.validate_description(self.body.replace('## '+heading+'\n','## Other\n'),paths),heading)
    def test_mutable_image_url_and_comment_only_example_are_rejected(self):
        self.assertTrue(review.validate_description(self.body.replace('a'*40,'main'),['demo/src/commonMain/Shop.kt']))
        self.assertTrue(review.validate_description(self.body.replace('DomposableButton("Add", onAdd)','// TODO add an example'),['domposable-ui/src/commonMain/Components.kt']))
    def test_titles_accept_ranker_action_style(self):
        self.assertEqual([],review.validate_title('Add adaptive storefront and web routes'))
        self.assertTrue(review.validate_title('WIP new stuff'))

    def test_baseline_updates_require_compatibility_migration_and_release_context(self):
        paths = ['domposable-ui/api/jvm/domposable-ui.api', 'domposable-ui/api/domposable-ui.klib.api']
        compatibility = '''## API compatibility
- Classification: additive; existing signatures remain available.
- Migration: none; the overload is optional.
- Release: minor; adds a public component.
'''
        self.assertEqual([], review.validate_description(self.body + compatibility, paths))
        self.assertTrue(review.validate_description(self.body, paths))
        for field in ['Classification', 'Migration', 'Release']:
            self.assertTrue(review.validate_description(self.body + compatibility.replace(field + ':', 'Other:'), paths))
        self.assertTrue(review.validate_description(self.body + compatibility.replace('additive;', 'compatible;'), paths))
        self.assertTrue(review.validate_description(self.body + compatibility.replace('minor;', 'alpha;'), paths))
        self.assertTrue(review.validate_description(self.body + compatibility.replace('Migration: none; the overload is optional.', 'Migration:'), paths))

    def test_processor_baseline_and_baseline_deletion_require_a_consumer_example(self):
        body = '## Summary\n- Remove an entry.\n## Verification\n- ABI diff reviewed.'
        self.assertTrue(review.requires_api_example(['domposable-ksp/api/domposable-ksp.api']))
        self.assertTrue(review.validate_description(body, ['domposable-ksp/api/domposable-ksp.api']))
        self.assertFalse(review.is_api_baseline('consumer-fixture/api/sample.api'))

    def test_release_previews_require_repository_identity_and_checksum(self):
        old = 'https://raw.githubusercontent.com/samuolis/domposable/' + 'a'*40 + '/previews/android/shop.png'
        new = 'https://github.com/samuolis/domposable/releases/download/visual-evidence/pr-9-run-1234-android-shop-' + 'a'*64 + '.png'
        self.assertEqual([], review.validate_description(self.body.replace(old, new), ['demo/src/commonMain/Shop.kt']))
        for invalid in [new.replace('samuolis/domposable', 'someone/else'), new.replace('a'*64, 'latest'), new.replace('visual-evidence', 'v0.1.0')]:
            self.assertTrue(review.validate_description(self.body.replace(old, invalid), ['demo/src/commonMain/Shop.kt']))
