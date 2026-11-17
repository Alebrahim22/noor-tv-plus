#!/usr/bin/env python3
"""Builds the seven Noor cards (Kahf, Baqarah, morning and evening adhkar, ruqyah, Du'a al-Nudba, Du'a Kumayl) and
installs them on the TV. The Kahf app carries the adhan, the duas' recordings, the reminders and the phone page,
so it gets the overlay, usage-access and exact-alarm permissions and is started once.

    python scripts/install_noor.py <tv-ip> [--lat 29.3759 --lon 47.9774]

The city goes into noor-app/gradle.properties. On the first run a random 4-digit PIN for the phone page is written
there too, and printed at the end.
"""
import argparse
import os
import pathlib
import random
import re
import subprocess
import sys
import time

sys.path.insert(0, os.path.dirname(__file__))
import tvlib  # noqa: E402

APP = tvlib.ROOT / "noor-app"
CARDS = ["kahf", "baqara", "sabah", "masa", "ruqya", "nudba", "kumayl"]


def set_props(lat, lon):
    f = APP / "gradle.properties"
    text = f.read_text(encoding="utf-8")
    if lat is not None:
        text = re.sub(r"^noor\.lat=.*$", f"noor.lat={lat}", text, flags=re.M)
        text = re.sub(r"^noor\.lon=.*$", f"noor.lon={lon}", text, flags=re.M)
    pin = re.search(r"^noor\.pin=(\d{4})$", text, re.M)
    if not pin or pin.group(1) == "0000":
        new = f"{random.SystemRandom().randrange(1000, 10000)}"
        text = re.sub(r"^noor\.pin=.*$", f"noor.pin={new}", text, flags=re.M)
    f.write_text(text, encoding="utf-8")
    return re.search(r"^noor\.pin=(\d{4})$", text, re.M).group(1)


def sdk_dir():
    adb = pathlib.Path(tvlib.find_adb()).resolve()
    return adb.parent.parent  # .../Sdk/platform-tools/adb


def build():
    local = APP / "local.properties"
    if not local.exists():
        local.write_text(f"sdk.dir={sdk_dir().as_posix()}\n", encoding="utf-8")
    raw = APP / "app/src/kahf/res/raw"
    missing = [n for n in ("adhan", "nudba", "kumayl") if not (raw / f"{n}.mp3").exists()]
    if missing:
        print("⚠️ Missing recordings (the app works, silently, without them):", ", ".join(missing))
    gradlew = APP / ("gradlew.bat" if os.name == "nt" else "gradlew")
    r = subprocess.run([str(gradlew), "-q", "--no-daemon", "assembleRelease"], cwd=APP)
    if r.returncode != 0:
        sys.exit("Build failed. JDK 17 and Android SDK platform 34 are needed (JAVA_HOME pointing at JDK 17).")


def main():
    p = argparse.ArgumentParser()
    p.add_argument("ip")
    p.add_argument("--lat", type=float)
    p.add_argument("--lon", type=float)
    p.add_argument("--no-build", action="store_true")
    a = p.parse_args()
    if not tvlib.answers(a.ip):
        sys.exit(f"The TV at {a.ip} does not answer over adb. Run: adb connect {tvlib.target(a.ip)}")
    pin = set_props(a.lat, a.lon)
    if not a.no_build:
        build()
    for card in CARDS:
        apk = APP / f"app/build/outputs/apk/{card}/release/app-{card}-release.apk"
        out = tvlib.adb("install", "-r", str(apk), ip=a.ip, timeout=600)
        print(("✅ " if "Success" in out else "⚠️ ") + card + ("" if "Success" in out else f": {out.strip()}"))
    pkg = "app.noortv.kahf"
    for op in ("SYSTEM_ALERT_WINDOW", "GET_USAGE_STATS", "SCHEDULE_EXACT_ALARM"):
        tvlib.shell(a.ip, f"appops set {pkg} {op} allow")
    time.sleep(8)  # Android restarts the app right after an update; start it once that has settled
    tvlib.shell(a.ip, f"am broadcast -n {pkg}/app.noortv.Reminder")
    time.sleep(2)
    line = tvlib.shell(a.ip, "content query --uri content://app.noortv.kahf.noor/line")
    print("Line under the clock:", line.strip())
    print(f"Phone page: http://{a.ip.split(':')[0]}:8080   PIN: {pin}")


if __name__ == "__main__":
    main()
