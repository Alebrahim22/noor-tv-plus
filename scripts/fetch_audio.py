#!/usr/bin/env python3
"""Downloads a recording (a YouTube link) into the Kahf app as res/raw/<name>.mp3: the adhan, or a dua.
Kept as it is by default: the reciter's full voice matters more than a quiet background. --normalize only evens
out the loudness, with no noise removal.

    python scripts/fetch_audio.py <adhan|nudba|kumayl> <youtube-url> [--normalize]

Needs yt-dlp (pip install yt-dlp) and ffmpeg.
"""
import pathlib
import shutil
import subprocess
import sys
import tempfile

ROOT = pathlib.Path(__file__).resolve().parent.parent
NAMES = ("adhan", "nudba", "kumayl")


def main():
    args = [a for a in sys.argv[1:] if not a.startswith("--")]
    if len(args) != 2 or args[0] not in NAMES:
        sys.exit(__doc__)
    name, url = args
    if not shutil.which("ffmpeg"):
        sys.exit("ffmpeg is missing (Windows: winget install Gyan.FFmpeg, Mac: brew install ffmpeg).")
    raw = ROOT / "noor-app/app/src/kahf/res/raw"
    raw.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory() as tmp:
        subprocess.run([sys.executable, "-m", "yt_dlp", "-q", "--no-playlist", "-f", "bestaudio", "-x",
                        "--audio-format", "mp3", "--audio-quality", "128K", "-o", f"{tmp}/a.%(ext)s", url], check=True)
        src = pathlib.Path(tmp) / "a.mp3"
        dst = raw / f"{name}.mp3"
        if "--normalize" in sys.argv:
            subprocess.run(["ffmpeg", "-v", "error", "-y", "-i", str(src), "-af", "loudnorm=I=-18:TP=-2",
                            "-ar", "44100", "-ac", "1", "-b:a", "128k", str(dst)], check=True)
        else:
            shutil.copy(src, dst)
    dur = subprocess.run(["ffprobe", "-v", "error", "-show_entries", "format=duration", "-of", "csv=p=0", str(dst)],
                         capture_output=True, text=True).stdout.strip()
    print(f"{name}: {float(dur or 0):.0f} s -> {dst}")


if __name__ == "__main__":
    main()
