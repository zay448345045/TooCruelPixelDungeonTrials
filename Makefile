@PHONY: build
build: __always
	rm -rf ./build
	make build-and-sign-apk
	make build-desktop

@PHONY: run
ide:
	android-studio . >/dev/null 2>&1 & disown

@PHONY: backup
backup:
	rsync -a . ../tcpd.bk

@PHONY: change
change:
	./tools/tinychange

@PHONY: build-and-sign-apk
build-desktop: __always
	rm desktop/build/libs/desktop-*.jar
	./gradlew desktop:release
	cp desktop/build/libs/desktop-*.jar ./build/desktop.jar

@PHONY: build-and-sign-apk
build-and-sign-apk: __always
	./gradlew android:assembleRelease
	make sign-release-apk
	mv ./build/aligned.apk ./build/android.apk

@PHONY: sign-release-apk
sign-release-apk: __always
	./tools/sign.sh

@PHONY: __always
__always: