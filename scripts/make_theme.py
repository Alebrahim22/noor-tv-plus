#!/usr/bin/env python3
"""Builds a theme folder ready for CleanYourTV's tools/theme.sh: the shape (themes/<name>) on top of the base dock
(themes/_base.json), plus this TV's remote keys from its CleanYourTV example config.

    python scripts/make_theme.py <zakhrafa|masjid-layl|awqat|plain> <out-folder> [brand-example-config.json]
"""
import json
import pathlib
import shutil
import sys

root = pathlib.Path(__file__).resolve().parent.parent
if len(sys.argv) not in (3, 4):
    sys.exit(__doc__)
name, out = sys.argv[1], pathlib.Path(sys.argv[2])
config = json.loads((root / "themes/_base.json").read_text(encoding="utf-8"))
shutil.rmtree(out, ignore_errors=True)
if name == "plain":
    out.mkdir(parents=True)
else:
    shutil.copytree(root / "themes" / name, out)
    config.update(json.loads((root / "themes" / name / "config.json").read_text(encoding="utf-8")))
if len(sys.argv) == 4:
    brand = json.loads(pathlib.Path(sys.argv[3]).read_text(encoding="utf-8"))
    for key in ("live_tv_package", "settings_key_keycodes", "settings_key_panel", "app_keys"):
        if key in brand:
            config[key] = brand[key]
(out / "config.json").write_text(json.dumps(config, ensure_ascii=False, indent=2), encoding="utf-8")
print(out)
