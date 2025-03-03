.PHONY: ide
ide:
	android-studio . >/dev/null 2>&1 & disown

.PHONY: backup
backup:
	rsync -a . ../tcpd.bk

.PHONY: change
change:
	./tools/tinychange

.PHONY: build
build: __always
	rm -rf ./build
	rm -f desktop/build/libs/desktop-*.jar
	mkdir -p ./build
	./gradlew android:assembleRelease
	./gradlew desktop:release

.PHONY: sign-release-apk
sign-release-apk: __always
	./tools/sign.sh

.PHONY: move-binaries
move-binaries:
	mv ./build/aligned.apk ./build/android.apk
	mv desktop/build/libs/desktop-*.jar ./build/desktop.jar

.PHONY: __always
__always: