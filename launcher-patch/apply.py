#!/usr/bin/env python3
"""Adds the Noor line to the CleanYourTV launcher: the Jafari occasion (or White Days, or «جمعة مباركة») above the
clock, and under it the adjusted Hijri date, the weather and the time to the next adhan. The prayer times, the
Hijri date and the occasions come from the Kahf app (noor-app), so the launcher only reads them.

    python apply.py <cleanyourtv folder> <lat> <lon>

Safe to run twice. Afterwards: build and install the launcher (launcher/build-and-install.sh), and turn on
shamsi_date in the theme (that is the line Noor writes into; tools/make_theme.py does it).
"""
import pathlib
import re
import sys

if len(sys.argv) != 4:
    sys.exit(__doc__)
root, lat, lon = pathlib.Path(sys.argv[1]), sys.argv[2], sys.argv[3]
float(lat), float(lon)  # fail early on a typo
here = pathlib.Path(__file__).resolve().parent
src = root / "launcher/app/src/main"


def patch(path, old, new, done_marker):
    text = path.read_text(encoding="utf-8")
    if done_marker in text:
        return
    if old not in text:
        sys.exit(f"Could not find the spot to change in {path} - the launcher changed upstream; patch it by hand.")
    path.write_text(text.replace(old, new, 1), encoding="utf-8")


noor = (here / "Noor.java").read_text(encoding="utf-8")
noor = noor.replace("@LAT@", lat).replace("@LON@", lon)
(src / "java/com/gathin/tvhome/Noor.java").write_text(noor, encoding="utf-8")

manifest = src / "AndroidManifest.xml"
patch(manifest,
      '    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />',
      '    <uses-permission android:name="android.permission.INTERNET" />\n'
      '    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />',
      'android.permission.INTERNET')
# Android 11+ only lets the launcher see the Kahf app's line if it says so.
text = manifest.read_text(encoding="utf-8")
if "app.noortv.kahf" not in text:
    text = re.sub(r"(\s*)<application", r'\1<queries><package android:name="app.noortv.kahf" /></queries>\1<application',
                  text, count=1)
    manifest.write_text(text, encoding="utf-8")

patch(src / "java/com/gathin/tvhome/MainActivity.java",
      "        if (shamsi) shamsiDate.setText(Shamsi.today(getResources()));",
      "        String noorGreeting = Noor.greeting(this);\n"
      "        if (noorGreeting != null && festiveGreeting.isEmpty()) greeting.setText(noorGreeting);\n"
      "        if (shamsi) {\n"
      "            shamsiDate.setText(Noor.line(this));\n"
      "            shamsiDate.setTextColor(Noor.color(this));\n"
      "        }",
      "Noor.line(this)")

layout = src / "res/layout/activity_main.xml"
text = layout.read_text(encoding="utf-8")
m = re.search(r'android:id="@\+id/shamsiDate".*?/>', text, re.S)
if not m:
    sys.exit(f"Could not find shamsiDate in {layout}")
block = m.group(0)
if 'android:maxLines="2"' not in block:
    new = block.replace('android:singleLine="true"', 'android:maxLines="2"\n                            android:lineSpacingExtra="4dp"')
    layout.write_text(text.replace(block, new), encoding="utf-8")

print("Noor patch applied:", lat, lon)
