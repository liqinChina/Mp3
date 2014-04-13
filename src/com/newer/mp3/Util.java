package com.newer.mp3;

public class Util {

	/**
	 * 格式化毫秒为分钟
	 * 
	 * @param duration
	 *            毫秒
	 * @return 毫秒的字符串格式
	 */
	public static String formatTime(String duration) {
		int n = Integer.parseInt(duration);
		n /= 1000;
		int minute = n / 60;
		int second = n % 60;
		return String.format("%d:%02d", minute, second);
	}
}
