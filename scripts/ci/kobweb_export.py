"""Export the integration app with Kobweb's own build lifecycle and stop its server."""
from pathlib import Path
import os
import subprocess

ROOT = Path(__file__).resolve().parents[2]


def main():
    environment = dict(os.environ)
    browser = subprocess.check_output(
        ["node", "--input-type=module", "-e",
         "import {chromium} from 'playwright'; console.log(chromium.executablePath());"],
        cwd=ROOT, text=True,
    ).strip()
    environment["KOBWEB_EXPORT_BROWSER_PATH"] = browser
    try:
        subprocess.run(
            [str(ROOT / "gradlew"), ":kobweb-example:kobwebExport",
             "-PkobwebExportLayout=STATIC", "-PkobwebBuildTarget=RELEASE", "--console=plain"],
            cwd=ROOT, env=environment, check=True,
        )
    finally:
        subprocess.run(
            [str(ROOT / "gradlew"), ":kobweb-example:kobwebStop", "--console=plain"],
            cwd=ROOT, env=environment, check=True,
        )
    output = ROOT / "kobweb-example/.kobweb/site"
    for name, heading in (("index.html", "Domposable with Kobweb"),
                          ("contacts.html", "Contacts"),
                          ("contacts/alex.html", "Alex")):
        text = (output / name).read_text()
        if heading not in text or "<h1" not in text:
            raise ValueError(f"Shared content was not exported: {name}")
    print("Kobweb export contains all three expected pre-rendered routes")


if __name__ == "__main__":
    main()
