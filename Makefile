ide:
	android-studio . >/dev/null 2>&1 & disown

backup:
	rsync -a . ../tcpd.bk
