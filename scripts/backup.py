#!/usr/bin/env python3
"""Saves everything needed to undo the setup, before any change: device info, the full app list, what is already
disabled, the settings, and which app is Home. Prints the folder; restore.py takes it.

    python scripts/backup.py <tv-ip>        -> ~/noor-tv-backup-<date>/
"""
import datetime
import json
import pathlib
import re
import sys

sys.path.insert(0, __import__("os").path.dirname(__file__))
import tvlib  # noqa: E402


def packages(ip, flag=""):
    out = tvlib.shell(ip, f"pm list packages {flag}".strip())
    return sorted(line.split(":", 1)[1].strip() for line in out.splitlines() if line.startswith("package:"))


def home_activity(ip):
    out = tvlib.shell(ip, "cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME")
    lines = [l.strip() for l in out.splitlines() if "/" in l]
    return lines[-1] if lines else ""


def main():
    if len(sys.argv) != 2:
        sys.exit(__doc__)
    ip = sys.argv[1]
    if not tvlib.answers(ip):
        sys.exit(f"The TV at {ip} does not answer over adb. Run: adb connect {tvlib.target(ip)}")
    out = pathlib.Path.home() / f"noor-tv-backup-{datetime.datetime.now():%Y%m%d-%H%M}"
    out.mkdir(parents=True, exist_ok=True)
    props = tvlib.shell(ip, "getprop")
    (out / "props.txt").write_text(props, encoding="utf-8")
    (out / "packages-all.txt").write_text("\n".join(packages(ip)) + "\n", encoding="utf-8")
    (out / "packages-disabled.txt").write_text("\n".join(packages(ip, "-d")) + "\n", encoding="utf-8")
    for ns in ("system", "secure", "global"):
        (out / f"settings-{ns}.txt").write_text(tvlib.shell(ip, f"settings list {ns}"), encoding="utf-8")
    (out / "disabled-by-us.txt").touch()

    def prop(name):
        m = re.search(rf"\[{re.escape(name)}\]: \[(.*?)\]", props)
        return m.group(1) if m else ""

    info = {"ip": ip, "home": home_activity(ip), "model": prop("ro.product.model"),
            "brand": prop("ro.product.manufacturer"), "android": prop("ro.build.version.release"),
            "sdk": prop("ro.build.version.sdk"), "characteristics": prop("ro.build.characteristics")}
    (out / "tv.json").write_text(json.dumps(info, indent=2, ensure_ascii=False), encoding="utf-8")
    print(json.dumps(info, ensure_ascii=False))
    print(out)


if __name__ == "__main__":
    main()
