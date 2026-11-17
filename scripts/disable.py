#!/usr/bin/env python3
"""Disables (never uninstalls) the given packages, one shell call each, logs each one for restore.py, then checks
the TV still answers. Refuses Android's core packages and factory/test packages with no override, and stops
before the stock launcher or Google TV's setup app unless their check has passed.

    python scripts/disable.py <tv-ip> <backup-folder> <package>...
    python scripts/disable.py <tv-ip> <backup-folder> --home-checked <package>...   (stock launcher / setupwraith)
"""
import pathlib
import re
import sys

sys.path.insert(0, __import__("os").path.dirname(__file__))
import tvlib  # noqa: E402


def wifi_owner(ip):
    """The app that owns the saved Wi-Fi network (on Google TV it can be the setup app)."""
    out = tvlib.shell(ip, "dumpsys wifi")
    names = set(re.findall(r"cname=([\w.]+)", out)) | set(re.findall(r"creatorName=([\w.]+)", out))
    return names


def main():
    args = sys.argv[1:]
    home_checked = "--home-checked" in args
    args = [a for a in args if a != "--home-checked"]
    if len(args) < 3:
        sys.exit(__doc__)
    ip, folder, pkgs = args[0], pathlib.Path(args[1]), args[2:]
    log = folder / "disabled-by-us.txt"
    if not log.exists():
        sys.exit(f"{log} is missing. Run backup.py first and pass its folder.")

    refused = [(p, tvlib.is_protected(p)) for p in pkgs if tvlib.is_protected(p)]
    if refused:
        for p, why in refused:
            print(f"⛔ {p}: {why}")
        sys.exit("Nothing was changed. Take these out of the list.")

    guarded = [p for p in pkgs if p in tvlib.HOME_GUARD or p.startswith(tvlib.STOCK_LAUNCHERS)]
    if guarded and not home_checked:
        print("⚠️ These decide which app is Home:", " ".join(guarded))
        print("Disable them only after the new launcher is installed and confirmed as Home:")
        print("  adb shell dumpsys activity activities | grep mFocusedApp   (press Home first)")
        sys.exit("Then run again with --home-checked. Nothing was changed.")
    if "com.google.android.tungsten.setupwraith" in pkgs and "com.google.android.tungsten.setupwraith" in wifi_owner(ip):
        sys.exit("⛔ The Google TV setup app owns the saved Wi-Fi network: disabling it would take the TV off Wi-Fi "
                 "and off adb. Forget the network and add it again from Settings, then retry. Nothing was changed.")

    for p in pkgs:
        out = tvlib.shell(ip, f"pm disable-user --user 0 {p}")
        if "disabled-user" in out:
            with log.open("a", encoding="utf-8") as f:
                f.write(p + "\n")
            print(f"✅ {p}")
        else:
            print(f"⚠️ {p} (not changed: {out.strip() or 'no answer'})")
    if tvlib.answers(ip):
        print("TV is answering")
    else:
        print(f"⛔ TV stopped answering. Run: python scripts/restore.py {folder}")


if __name__ == "__main__":
    main()
