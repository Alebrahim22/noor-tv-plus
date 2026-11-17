"""Shared helpers for the Noor TV scripts: find adb on Windows, macOS or Linux, and talk to one TV.
Standard library only, so nothing needs installing beyond adb itself."""
import os
import pathlib
import shutil
import subprocess
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent

# Arabic and emoji in the output, also in a Windows console.
for _stream in (sys.stdout, sys.stderr):
    try:
        _stream.reconfigure(encoding="utf-8")
    except (AttributeError, ValueError):
        pass

# Android's core packages: without them the TV may not boot. disable.py refuses them, with no override.
# (From CleanYourTV's safety rules.)
PROTECTED_EXACT = {
    "android", "android.ext.services", "android.ext.shared", "com.android.systemui", "com.android.shell",
    "com.android.phone", "com.android.settings", "com.android.tv.settings", "com.android.permissioncontroller",
    "com.google.android.permissioncontroller", "com.android.packageinstaller",
    "com.google.android.packageinstaller", "com.android.extservices", "com.google.android.ext.services",
    "com.android.ext.shared", "com.google.android.ext.shared", "com.android.modulemetadata",
    "com.google.android.modulemetadata", "com.google.android.gms", "com.google.android.gsf",
    "com.android.vending", "com.android.inputmethod.latin", "com.google.android.inputmethod.latin",
    "com.google.android.tvkeyboard", "com.android.bluetooth", "com.android.providers.settings",
    "com.android.providers.media", "com.android.providers.downloads", "com.android.providers.tv",
}
PROTECTED_PREFIXES = ("com.google.android.overlay.modules", "com.android.providers.")
PROVIDERS_ALLOWED = {"com.android.providers.calendar", "com.android.providers.contacts",
                     "com.android.providers.userdictionary", "com.android.providers.blockednumber"}
# Factory and test menus: on some boards even opening one flips a flag the shell cannot undo.
FACTORY_HINTS = ("factory", "hkcfac", "factorymenu", ".fac.", "testmode", "mtktest")
# Packages that need a check before they go (disable.py explains why and stops).
HOME_GUARD = {"com.google.android.tungsten.setupwraith"}
STOCK_LAUNCHERS = ("com.google.android.apps.tv.launcherx", "com.google.android.tvlauncher",
                   "com.android.tv.launcher", "com.cvte.tv.launcher")


def find_adb():
    """adb from PATH, ANDROID_HOME, or the usual SDK folders on each system."""
    found = shutil.which("adb")
    if found:
        return found
    exe = "adb.exe" if os.name == "nt" else "adb"
    homes = [os.environ.get("ANDROID_HOME"), os.environ.get("ANDROID_SDK_ROOT")]
    if os.name == "nt":
        homes.append(os.path.join(os.environ.get("LOCALAPPDATA", ""), "Android", "Sdk"))
    else:
        homes += [os.path.expanduser("~/Library/Android/sdk"), os.path.expanduser("~/Android/Sdk"),
                  "/opt/homebrew/share/android-commandlinetools", "/usr/local/share/android-commandlinetools"]
    for home in homes:
        if home:
            p = pathlib.Path(home) / "platform-tools" / exe
            if p.exists():
                return str(p)
    sys.exit("adb not found. Install Android platform-tools (Windows: winget install Google.PlatformTools, "
             "Mac: brew install android-platform-tools).")


ADB = None


def adb(*args, ip=None, check=False, timeout=60):
    """Runs adb (against ip:5555 when given) and returns stdout as text."""
    global ADB
    ADB = ADB or find_adb()
    cmd = [ADB] + (["-s", target(ip)] if ip else []) + list(args)
    r = subprocess.run(cmd, capture_output=True, text=True, encoding="utf-8", errors="replace", timeout=timeout)
    if check and r.returncode != 0:
        sys.exit(f"adb {' '.join(args)} failed: {r.stderr.strip() or r.stdout.strip()}")
    return r.stdout


def shell(ip, command, timeout=60):
    return adb("shell", command, ip=ip, timeout=timeout)


def target(ip):
    return ip if ":" in ip else f"{ip}:5555"


def is_protected(pkg):
    if pkg in PROTECTED_EXACT:
        return "Android core package: the TV may not boot without it"
    if pkg.startswith(PROTECTED_PREFIXES) and pkg not in PROVIDERS_ALLOWED:
        return "Android core package: the TV may not boot without it"
    if any(h in pkg.lower() for h in FACTORY_HINTS):
        return "factory/test package: never touch it"
    return None


def answers(ip):
    """True when the TV still answers over adb."""
    try:
        return shell(ip, "echo ok", timeout=10).strip() == "ok"
    except Exception:
        return False
