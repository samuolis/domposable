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
