#!/usr/bin/env bash
# Build a signed, installable APK without Gradle — aapt2 + javac + d8 + apksigner.
set -euo pipefail

SDK=/scratch/work/sdk
JDK=/scratch/work/jdk
BT=$SDK/build-tools/34.0.0
PLATFORM=$SDK/platforms/android-35/android.jar
export JAVA_HOME=$JDK
export PATH=$JDK/bin:$PATH

cd "$(dirname "$0")"
rm -rf build && mkdir -p build/gen build/classes build/dex

echo "[1/7] aapt2 compile"
"$BT/aapt2" compile --dir res -o build/res.zip

echo "[2/7] aapt2 link"
"$BT/aapt2" link \
  -o build/base.apk \
  -I "$PLATFORM" \
  --manifest AndroidManifest.xml \
  -R build/res.zip \
  -A assets \
  --java build/gen \
  --min-sdk-version 23 \
  --target-sdk-version 34 \
  --version-code 1 --version-name 1.0.0 \
  --auto-add-overlay

echo "[3/7] javac"
find src build/gen -name '*.java' > build/sources.txt
javac -nowarn -source 8 -target 8 -encoding UTF-8 \
  -bootclasspath "$PLATFORM" -classpath "$PLATFORM" \
  -d build/classes @build/sources.txt 2>&1 | grep -v 'bootstrap class path' || true

echo "[4/7] d8"
find build/classes -name '*.class' > build/classes.txt
java -Xmx768m -cp "$BT/lib/d8.jar" com.android.tools.r8.D8 --release --lib "$PLATFORM" --min-api 23 --output build/dex @build/classes.txt

echo "[5/7] package dex"
cd build && cp base.apk unsigned.apk && zip -q -X unsigned.apk dex/classes.dex && cd ..

echo "[6/7] zipalign"
"$BT/zipalign" -f -p 4 build/unsigned.apk build/aligned.apk

echo "[7/7] sign"
if [ ! -f fortune.jks ]; then
  java -Xmx256m -cp "$JDK" sun.security.tools.keytool.Main -genkeypair -v -keystore fortune.jks -alias fortune \
    -keyalg RSA -keysize 2048 -validity 10950 \
    -storepass fortune2026 -keypass fortune2026 \
    -dname "CN=Fortune Password, OU=Apps, O=Fortune, L=Beijing, S=Beijing, C=CN" >/dev/null 2>&1
fi
java -Xmx512m -jar "$BT/lib/apksigner.jar" sign --ks fortune.jks --ks-key-alias fortune \
  --ks-pass pass:fortune2026 --key-pass pass:fortune2026 \
  --v1-signing-enabled true --v2-signing-enabled true --v3-signing-enabled true \
  --out build/fortune-password.apk build/aligned.apk

java -Xmx512m -jar "$BT/lib/apksigner.jar" verify --print-certs build/fortune-password.apk | head -5
ls -lh build/fortune-password.apk
