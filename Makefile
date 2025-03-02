@PHONY: run
ide:
	android-studio . >/dev/null 2>&1 & disown

@PHONY: backup
backup:
	rsync -a . ../tcpd.bk

@PHONY: change
change:
	./tools/tinychange