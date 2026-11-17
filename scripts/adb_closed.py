#!/usr/bin/env python3
"""Proves network ADB is closed at the end: tries to connect to the TV's port (5555 by default, or Wireless
debugging's own port). Only "closed" counts.

    python scripts/adb_closed.py <tv-ip> [port]
"""
import socket
import sys


def main():
    if len(sys.argv) not in (2, 3):
        sys.exit(__doc__)
    ip, port = sys.argv[1], int(sys.argv[2]) if len(sys.argv) == 3 else 5555
    try:
        with socket.create_connection((ip, port), timeout=3):
            print(f"OPEN: {ip}:{port} still answers. Switch off Network/USB debugging (and Wireless debugging), "
                  "reboot the TV, and check again.")
            sys.exit(1)
    except OSError:
        print(f"closed: {ip}:{port}")


if __name__ == "__main__":
    main()
