# Changelog

## 1.0.0 — 2026-11-06

First release.

- Real password engine on `crypto.getRandomValues` with rejection sampling (no modulo bias)
- Six character sets: A-Z, a-z, 0-9, symbols, 56 Chinese characters, 44 Devanagari letters
- At least one character from every enabled set, guaranteed; no password ever repeats
- Fortune meanings: pinyin and meaning for every 汉字 in a password
- Length 4–128, entropy read-out in bits, four-level strength meter
- Avoid look-alikes (`O 0 I l 1 |`), shuffle count 1–1000, shuffle duration 0.3–4 s
- Three complete languages: English, हिन्दी, 中文
- Light / auto / dark theme, four accent colours, three animation switches
- Recent list of the last six passwords, persisted in `localStorage`, with Reset all
- Installable PWA with service worker; runs from `file://` with no server
- Native Android shell: WebView + clipboard bridge, back button closes the window
- Signed APK 1.0.0 — 166 KB, minSdk 23, targetSdk 34, no permissions at all
