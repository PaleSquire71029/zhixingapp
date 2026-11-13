#!/usr/bin/env python3
"""Validate repository-local Markdown links without third-party dependencies."""

from __future__ import annotations

import html
import re
import sys
from pathlib import Path
from urllib.parse import unquote, urlsplit

ROOT = Path(__file__).resolve().parents[1]
LINK_RE = re.compile(r"!?(?:\[[^\]]*\])\((<[^>]+>|[^)]*)\)")
REFERENCE_RE = re.compile(r"^\s{0,3}\[[^\]]+\]:\s*(<[^>]+>|\S+)", re.IGNORECASE)
FENCE_RE = re.compile(r"^\s{0,3}(`{3,}|~{3,})")


def destination_from_match(value: str) -> str:
    value = value.strip()
    if value.startswith("<") and value.endswith(">"):
        value = value[1:-1]
    return value.split(None, 1)[0] if value else ""


def resolve_local_target(markdown_file: Path, destination: str) -> Path | None:
    destination = html.unescape(destination.strip())
    if not destination or destination.startswith("#"):
        return None

    parsed = urlsplit(destination)
    if parsed.scheme or parsed.netloc:
        return None

    raw_path = unquote(parsed.path)
    if not raw_path:
        return None

    if raw_path.startswith("/"):
        candidate = ROOT / raw_path.lstrip("/")
    else:
        candidate = markdown_file.parent / raw_path

    try:
        return candidate.resolve(strict=False)
    except OSError:
        return candidate


def markdown_files() -> list[Path]:
    return sorted(
        path for path in ROOT.rglob("*.md")
        if ".git" not in path.parts
    )


def check_file(markdown_file: Path) -> list[tuple[int, str, Path]]:
    failures: list[tuple[int, str, Path]] = []
    fenced_by: str | None = None

    for line_number, line in enumerate(
        markdown_file.read_text(encoding="utf-8").splitlines(), start=1
    ):
        fence_match = FENCE_RE.match(line)
        if fence_match:
            marker = fence_match.group(1)
            marker_char = marker[0]
            if fenced_by is None:
                fenced_by = marker_char
            elif marker_char == fenced_by:
                fenced_by = None
            continue
        if fenced_by is not None:
            continue

        searchable = re.sub(r"`+.*?`+", "", line)
        destinations = [
            destination_from_match(match.group(1))
            for match in LINK_RE.finditer(searchable)
        ]
        reference = REFERENCE_RE.match(searchable)
        if reference:
            destinations.append(destination_from_match(reference.group(1)))

        for destination in destinations:
            target = resolve_local_target(markdown_file, destination)
            if target is None:
                continue
            try:
                target.relative_to(ROOT)
            except ValueError:
                failures.append((line_number, destination, target))
                continue
            if not target.exists():
                failures.append((line_number, destination, target))

    return failures


def main() -> int:
    files = markdown_files()
    failures: list[tuple[Path, int, str, Path]] = []

    for markdown_file in files:
        for line_number, destination, target in check_file(markdown_file):
            failures.append((markdown_file, line_number, destination, target))

    if failures:
        print(f"Markdown link check failed: {len(failures)} broken local link(s).")
        for source, line_number, destination, target in failures:
            print(
                f"MISSING {source.relative_to(ROOT)}:{line_number} "
                f"-> {destination!r} (resolved: {target})"
            )
        return 1

    print(f"Markdown link check passed: {len(files)} Markdown file(s), no broken local links.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
