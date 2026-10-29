"""Render original vector branding and fictional project illustrations for isolated previews."""

import shutil
import subprocess
from pathlib import Path

import cairo
import gi

gi.require_version("Rsvg", "2.0")
from gi.repository import Rsvg

ROOT = Path(__file__).resolve().parents[2]


def render(source, target, size):
    width, height = map(int, size.split("x"))
    surface = cairo.ImageSurface(cairo.FORMAT_ARGB32, width, height)
    viewport = Rsvg.Rectangle()
    viewport.x, viewport.y, viewport.width, viewport.height = 0, 0, width, height
    Rsvg.Handle.new_from_file(str(source)).render_document(
        cairo.Context(surface), viewport
    )
    surface.write_to_png(str(target))


def main():
    branding = ROOT / "assets/branding"
    branding.mkdir(parents=True, exist_ok=True)
    svg = """<svg xmlns="http://www.w3.org/2000/svg" width="1024" height="1024" viewBox="0 0 256 256">
<rect width="256" height="256" rx="48" fill="#65425f"/>
<path d="M42 77Q85 57 128 87Q171 57 214 77L201 131Q168 177 128 133Q88 177 55 131Z" fill="#ead0af"/>
<path d="M65 96Q87 82 108 103Q88 126 65 106Z M148 103Q170 82 191 96L191 106Q170 126 148 103Z" fill="#74536f"/>
<path d="M163 150H210V214H163Z" fill="#f8f5f1"/><path d="M174 169H199M174 182H199M174 195H191" stroke="#74536f" stroke-width="5"/>
<path d="M72 170L85 190L108 158" fill="none" stroke="#d7b87b" stroke-width="8" stroke-linecap="round"/>
</svg>"""
    (branding / "icon.svg").write_text(svg, encoding="utf-8")
    for size in (44, 50, 150, 256, 300, 1024):
        name = {
            44: "Square44x44Logo.png",
            50: "StoreLogo.png",
            150: "Square150x150Logo.png",
            300: "store-icon-300.png",
        }.get(size, f"icon-{size}.png")
        render(branding / "icon.svg", branding / name, f"{size}x{size}")
    subprocess.run(
        [
            "convert",
            str(branding / "icon-256.png"),
            "-define",
            "icon:auto-resize=256,128,64,48,32,16",
            str(branding / "icon.ico"),
        ],
        check=True,
    )
    shutil.copy2(
        branding / "icon-256.png",
        ROOT / "src/main/resources/de/appfabrik/kostuemplan/icon-256.png",
    )
    photos = ROOT / "assets/store_demo/images"
    photos.mkdir(parents=True, exist_ok=True)
    shapes = {
        "lilac": '<path d="M300 130Q400 80 500 130L620 485Q400 545 180 485Z" fill="#a88ab7"/><path d="M300 130Q400 240 500 130" fill="none" stroke="#e7cfaa" stroke-width="18"/><path d="M330 205L290 475M470 205L510 475M400 215V500" stroke="#876397" stroke-width="10"/><circle cx="400" cy="190" r="19" fill="#d7b87b"/>',
        "rose": '<ellipse cx="400" cy="465" rx="250" ry="48" fill="#876397"/><path d="M230 450L435 90L550 450Z" fill="#b494bb"/><path d="M250 415Q400 440 535 415" stroke="#e7bf84" stroke-width="25"/><path d="M400 220l13 28 30 4-22 20 5 30-26-15-26 15 5-30-22-20 30-4Z" fill="#ead0af"/>',
        "sage": '<path d="M150 220Q270 160 400 240Q530 160 650 220L610 360Q510 485 400 360Q290 485 190 360Z" fill="#92aa8c"/><path d="M215 260Q275 220 345 280Q280 335 220 295Z M455 280Q525 220 585 260L580 295Q520 335 455 280Z" fill="#f4ede4"/><path d="M155 240L75 185M645 240L725 185" stroke="#61785d" stroke-width="16"/>',
    }
    for name, shape in shapes.items():
        source = photos / f"{name}.svg"
        source.write_text(
            '<svg xmlns="http://www.w3.org/2000/svg" width="800" height="600" viewBox="0 0 800 600"><rect width="800" height="600" fill="#f4ede4"/><ellipse cx="400" cy="516" rx="245" ry="25" fill="#e3d9ce"/>'
            + shape
            + "</svg>\n",
            encoding="utf-8",
        )
        render(source, photos / f"{name}.png", "800x600")
    subprocess.run(
        [
            "convert",
            str(photos / "lilac.png"),
            "-quality",
            "92",
            str(photos / "lilac.jpg"),
        ],
        check=True,
    )


if __name__ == "__main__":
    main()
