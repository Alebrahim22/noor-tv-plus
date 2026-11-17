#!/usr/bin/env python3
"""Prints the Jafari times the TV will use (Fajr 16°, Maghrib 4°, Isha 14°), worked out the same way as the
app's PrayerCalc.java, so they can be checked against the local mosque's timetable before installing.

    python scripts/times.py                       # Kuwait City, today and the next 6 days
    python scripts/times.py --lat 29.3 --lon 48.0 --days 3 --tz 3
"""
import argparse
import datetime
import math


def compute(lat, lon, d, tz):
    def rad(x): return math.radians(x)
    def fix(a, b): return a - b * math.floor(a / b)

    y, m, day = d.year, d.month, d.day
    if m <= 2:
        y, m = y - 1, m + 12
    a = math.floor(y / 100)
    jd = math.floor(365.25 * (y + 4716)) + math.floor(30.6001 * (m + 1)) + day + 2 - a + math.floor(a / 4) - 1524.5
    jd -= lon / (15 * 24.0)

    def sun(j):
        dd = j - 2451545.0
        g = fix(357.529 + 0.98560028 * dd, 360)
        q = fix(280.459 + 0.98564736 * dd, 360)
        L = fix(q + 1.915 * math.sin(rad(g)) + 0.020 * math.sin(rad(2 * g)), 360)
        e = 23.439 - 0.00000036 * dd
        ra = math.degrees(math.atan2(math.cos(rad(e)) * math.sin(rad(L)), math.cos(rad(L)))) / 15
        return math.degrees(math.asin(math.sin(rad(e)) * math.sin(rad(L)))), q / 15 - fix(ra, 24)

    def mid(t): return fix(12 - sun(jd + t)[1], 24)

    def angle(ang, t, before):
        decl = sun(jd + t)[0]
        c = (-math.sin(rad(ang)) - math.sin(rad(decl)) * math.sin(rad(lat))) / (math.cos(rad(decl)) * math.cos(rad(lat)))
        h = math.degrees(math.acos(max(-1, min(1, c)))) / 15
        return mid(t) + (-h if before else h)

    t = [5, 6, 12, 18, 18, 18]
    for _ in range(2):
        t = [angle(16, t[0] / 24, True), angle(0.833, t[1] / 24, True), mid(t[2] / 24),
             angle(0.833, t[3] / 24, False), angle(4, t[4] / 24, False), angle(14, t[5] / 24, False)]
    return [int(math.floor(fix(x + tz - lon / 15, 24) * 60 + 0.5)) % 1440 for x in t]


def hm(m):
    h = m // 60
    return f"{(h % 12) or 12}:{m % 60:02d} {'ص' if h < 12 else 'م'}"


def main():
    try:
        __import__("sys").stdout.reconfigure(encoding="utf-8")  # Arabic in a Windows console
    except (AttributeError, ValueError):
        pass
    p = argparse.ArgumentParser()
    p.add_argument("--lat", type=float, default=29.3759)
    p.add_argument("--lon", type=float, default=47.9774)
    p.add_argument("--tz", type=float, default=None, help="hours from UTC; default: this computer's")
    p.add_argument("--days", type=int, default=7)
    a = p.parse_args()
    today = datetime.date.today()
    print("اليوم        الفجر     الشروق    الظهرين   الغروب    العشاءين  العشاء")
    for i in range(a.days):
        d = today + datetime.timedelta(days=i)
        tz = a.tz if a.tz is not None else datetime.datetime.combine(d, datetime.time(12)).astimezone().utcoffset().total_seconds() / 3600
        print(d.isoformat(), "  ".join(hm(x).rjust(8) for x in compute(a.lat, a.lon, d, tz)))


if __name__ == "__main__":
    main()
