from pathlib import Path
import sys
import unittest
sys.path.insert(0, str(Path(__file__).resolve().parents[1] / 'ci'))
from api_reference import Signature, parameter, slug, code

class ApiReferenceTest(unittest.TestCase):
    def test_dokka_nested_types_defaults_and_entities_are_preserved(self):
        parser = Signature()
        parser.feed('''<div class="symbol monospace">constructor(<span class="parameters">
            <span class="parameter">items: <a>List</a>&lt;String&gt;, </span>
            <span class="parameter">onSelect: (String) -&gt; Unit = {}, </span>
            <span class="parameter">enabled: Boolean = true</span></span>)</div>''')
        self.assertEqual(('items', 'List<String>', None), parameter(parser.parameters[0]))
        self.assertEqual(('onSelect', '(String) -> Unit', '{}'), parameter(parser.parameters[1]))
        self.assertEqual(('enabled', 'Boolean', 'true'), parameter(parser.parameters[2]))
        self.assertEqual(1, len(parser.symbols))
    def test_all_overloads_are_kept_and_unrelated_text_is_ignored(self):
        parser = Signature()
        parser.feed('<p>Ignore this</p><div class="symbol monospace">fun size(all: Length)</div><div class="symbol monospace">fun size(width: Length, height: Length)</div>')
        self.assertEqual(['fun size(all: Length)', 'fun size(width: Length, height: Length)'], parser.symbols)
    def test_invalid_parameters_fail_instead_of_generating_incomplete_tables(self):
        with self.assertRaises(ValueError): parameter('not a parameter')
        self.assertEqual('-domposable-button-props', slug('DomposableButtonProps'))
        self.assertEqual('`A&#124;B`', code('A|B'))
