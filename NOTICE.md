# Credits

Noor TV Plus is a modified version of **Noor TV** by majedphotos (https://github.com/majedphotos/noor-tv,
MIT License, Copyright (c) 2026 majedphotos): the card apps, the phone page, the reminders, the launcher patch and
the three themes (with their wallpapers) come from it.

It is built on top of **CleanYourTV** by Gathin (https://github.com/Gathin23/cleanyourtv, MIT License): the
launcher, the per-model debloat lists and the safety rules (protected packages, the Home guard, closing ADB) that
`scripts/` follows.

Ideas taken, re-written here (no code copied):
- **Afnon** by egyjs (MIT): prayer times worked out on the TV, a full Fajr adhan.
- **cleantv** by JohnWayneeee (MIT): a skill in phases with one reference file each, cross-platform Python
  scripts, the privacy phase, a restore that also puts Home back.
- **UrTV** (tshi-dev) and **KLightTV** (linkq8), which carry no licence: only ideas (minute offsets per prayer, a
  Hijri day offset, settings from the phone). No code or text from them is used.

Prayer-time astronomy: the public formulas published at praytimes.org (sun position from the US Naval Observatory's
low-precision equations), Jafari angles of the Shia Ithna-Ashari, Leva Institute, Qum method.

Recordings (in `noor-app/app/src/kahf/res/raw/`) belong to their reciters and are included for personal use on
the family's TV:
- Adhan: Mohammed Bu Jbara, Persian style — youtube.com/watch?v=WWICkyQBKzQ
- Du'a al-Nudba: Baqir Al-Maqdisi — youtube.com/watch?v=r415SOQJkbs
- Du'a Kumayl: Ali Bu Hamad — youtube.com/watch?v=k1C3svQF78A

Do not publish this repository with the recordings in it; remove `res/raw/*.mp3` first (they are listed in
`.gitignore`), and let each person fetch their own with `scripts/fetch_audio.py`.
