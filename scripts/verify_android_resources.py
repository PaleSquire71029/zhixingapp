#!/usr/bin/env python3
"""Validate Android drawable files before AAPT packages them into an APK.

Gradle can package a truncated image without decoding it. This check catches
broken RIFF/WebP lengths, malformed PNG chunks and incomplete JPEG files.
"""
from __future__ import annotations

import struct
import sys
import zlib
from pathlib import Path
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / "app" / "src" / "main" / "res"
ANDROID_NS = "{http://schemas.android.com/apk/res/android}"


def fail(path: Path, message: str) -> None:
    raise SystemExit(f"INVALID ANDROID RESOURCE: {path.relative_to(ROOT)}: {message}")


def validate_webp(path: Path, data: bytes) -> None:
    if len(data) < 20 or data[:4] != b"RIFF" or data[8:12] != b"WEBP":
        fail(path, "missing RIFF/WEBP header")
    declared_total = int.from_bytes(data[4:8], "little") + 8
    if declared_total != len(data):
        fail(path, f"RIFF declares {declared_total} bytes, actual file has {len(data)}")
    pos = 12
    while pos < len(data):
        if pos + 8 > len(data):
            fail(path, "truncated WebP chunk header")
        chunk_size = int.from_bytes(data[pos + 4:pos + 8], "little")
        end = pos + 8 + chunk_size
        padded_end = end + (chunk_size & 1)
        if padded_end > len(data):
            fail(path, "truncated WebP chunk payload")
        pos = padded_end
    if pos != len(data):
        fail(path, "invalid trailing bytes")


def validate_png(path: Path, data: bytes) -> None:
    if len(data) < 33 or data[:8] != b"\x89PNG\r\n\x1a\n":
        fail(path, "missing PNG signature or IHDR")
    pos = 8
    saw_ihdr = False
    saw_iend = False
    while pos < len(data):
        if pos + 12 > len(data):
            fail(path, "truncated PNG chunk")
        size = struct.unpack(">I", data[pos:pos + 4])[0]
        kind = data[pos + 4:pos + 8]
        end = pos + 8 + size
        crc_end = end + 4
        if crc_end > len(data):
            fail(path, f"truncated {kind.decode('ascii', 'replace')} chunk")
        expected_crc = struct.unpack(">I", data[end:crc_end])[0]
        actual_crc = zlib.crc32(data[pos + 4:end]) & 0xFFFFFFFF
        if expected_crc != actual_crc:
            fail(path, f"CRC mismatch in {kind.decode('ascii', 'replace')} chunk")
        if pos == 8 and kind != b"IHDR":
            fail(path, "IHDR must be the first chunk")
        if kind == b"IHDR":
            saw_ihdr = True
        if kind == b"IEND":
            if size != 0:
                fail(path, "IEND chunk must be empty")
            saw_iend = True
            if crc_end != len(data):
                fail(path, "unexpected bytes after IEND")
            break
        pos = crc_end
    if not saw_ihdr or not saw_iend:
        fail(path, "missing IHDR or IEND")


def validate_jpeg(path: Path, data: bytes) -> None:
    if len(data) < 4 or data[:2] != b"\xff\xd8" or data[-2:] != b"\xff\xd9":
        fail(path, "incomplete JPEG start/end markers")


def resolve_drawable(name: str, kind: str) -> bool:
    for folder in ("drawable", "drawable-nodpi", "drawable-anydpi", "mipmap", "mipmap-anydpi-v26"):
        base = RES / folder / name
        if any(Path(str(base) + ext).is_file() for ext in (".png", ".webp", ".jpg", ".jpeg", ".xml")):
            return True
    return False


def main() -> None:
    if not RES.is_dir():
        raise SystemExit(f"Android res directory missing: {RES}")
    count = 0
    for ext, validator in (
        (".webp", validate_webp),
        (".png", validate_png),
        (".jpg", validate_jpeg),
        (".jpeg", validate_jpeg),
    ):
        for path in RES.rglob(f"*{ext}"):
            validator(path, path.read_bytes())
            count += 1

    manifest_path = ROOT / "app/src/main/AndroidManifest.xml"
    manifest = ET.parse(manifest_path).getroot()
    application = manifest.find("application")
    if application is None:
        raise SystemExit("AndroidManifest.xml has no <application>")
    for attr in ("icon", "roundIcon"):
        ref = application.get(ANDROID_NS + attr, "")
        if not ref.startswith("@mipmap/"):
            raise SystemExit(f"AndroidManifest {attr} must reference @mipmap/...")
        name = ref.split("/", 1)[1]
        if not any((RES / folder / f"{name}.xml").is_file()
                   for folder in ("mipmap-anydpi-v26", "mipmap-anydpi")):
            raise SystemExit(f"launcher resource XML missing for {ref}")

    for icon_xml in (RES / "mipmap-anydpi-v26").glob("ic_launcher*.xml"):
        root = ET.parse(icon_xml).getroot()
        for element in root.iter():
            for key, value in element.attrib.items():
                if key == ANDROID_NS + "drawable" and value.startswith("@drawable/"):
                    drawable = value.split("/", 1)[1]
                    if not resolve_drawable(drawable, "drawable"):
                        raise SystemExit(f"{icon_xml.relative_to(ROOT)} references missing {value}")

    print(f"Android resource validation passed: {count} raster image(s), launcher references valid.")


if __name__ == "__main__":
    main()
