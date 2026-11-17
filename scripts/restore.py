#!/usr/bin/env python3
"""Undoes the setup from a backup folder: turns back on every app the setup disabled, and puts the original Home
app back. Use --remove-noor to also uninstall the Noor cards and the new launcher (apps this setup added).

    python scripts/restore.py <backup-folder> [--remove-noor]
"""
import json
import pathlib
import sys

sys.path.insert(0, __import__("os").path.dirname(__file__))
import tvlib  # noqa: E402

NOOR_APPS = ["app.noortv.kahf", "app.noortv.baqara", "app.noortv.sabah", "app.noortv.masa", "app.noortv.ruqya",
             "app.noortv.nudba", "app.noortv.kumayl"]


def main():
    args = [a for a in sys.argv[1:] if not a.startswith("--")]
    if len(args) != 1:
        sys.exit(__doc__)
    folder = pathlib.Path(args[0])
    info = json.loads((folder / "tv.json").read_text(encoding="utf-8"))
    ip = info["ip"]
    if not tvlib.answers(ip):
        sys.exit(f"The TV at {ip} does not answer over adb. Run: adb connect {tvlib.target(ip)}")
    pkgs = [p.strip() for p in (folder / "disabled-by-us.txt").read_text(encoding="utf-8").splitlines() if p.strip()]
    for p in reversed(pkgs):
        out = tvlib.shell(ip, f"pm enable --user 0 {p}")
        print(("✅ " if "enabled" in out else "⚠️ ") + p)
    if info.get("home"):
        tvlib.shell(ip, f"cmd package set-home-activity {info['home']}")
        print("Home:", info["home"])
    if "--remove-noor" in sys.argv:
        for p in NOOR_APPS + ["com.gathin.tvhome"]:
            print(p, tvlib.adb("uninstall", p, ip=ip).strip())
    print("Done. Press Home on the remote; a reboot settles everything.")


if __name__ == "__main__":
    main()
