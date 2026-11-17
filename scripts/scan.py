#!/usr/bin/env python3
"""Finds Android TVs on this Wi-Fi: every address on the computer's local networks with ADB's port 5555 open,
plus any Wireless debugging services adb can see.

    python scripts/scan.py
"""
import concurrent.futures
import ipaddress
import socket
import sys

sys.path.insert(0, __import__("os").path.dirname(__file__))
import tvlib  # noqa: E402


def local_networks():
    """The /24 around each IPv4 address this computer has on a private network."""
    addrs = set()
    try:
        for info in socket.getaddrinfo(socket.gethostname(), None, socket.AF_INET):
            addrs.add(info[4][0])
    except socket.gaierror:
        pass
    try:  # the address used to reach the internet, which getaddrinfo can miss
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.connect(("8.8.8.8", 80))
        addrs.add(s.getsockname()[0])
        s.close()
    except OSError:
        pass
    nets = set()
    for a in addrs:
        ip = ipaddress.ip_address(a)
        if ip.is_private and not ip.is_loopback:
            nets.add(ipaddress.ip_network(f"{a}/24", strict=False))
    return sorted(nets)


def open_5555(host):
    try:
        with socket.create_connection((str(host), 5555), timeout=0.6):
            return str(host)
    except OSError:
        return None


def main():
    nets = local_networks()
    if not nets:
        sys.exit("No local network found. Is this computer on the Wi-Fi?")
    hosts = [h for n in nets for h in n.hosts()]
    with concurrent.futures.ThreadPoolExecutor(128) as pool:
        found = [h for h in pool.map(open_5555, hosts) if h]
    print("Networks:", ", ".join(map(str, nets)))
    for h in found:
        print(f"ADB open: {h}:5555")
    mdns = tvlib.adb("mdns", "services")
    for line in mdns.splitlines():
        if "_adb" in line:
            print("Wireless debugging:", line.strip())
    if not found and "_adb" not in mdns:
        print("No TV found. Check that Developer options > USB debugging (or Network debugging) is on, "
              "and that the TV is on the same Wi-Fi.")


if __name__ == "__main__":
    main()
