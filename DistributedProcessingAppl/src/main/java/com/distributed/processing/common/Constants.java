package com.distributed.processing.common;

import java.util.HashMap;

public class Constants {
	public static final String PROC_START_TIME = "_"+System.currentTimeMillis();
	public static final String _THREAD_STATUS_FILE = "THREAD-STATUS";
	public static final String _THREAD_RUNNING_STATUS = "running";
	public static final String _THREAD_STOP_STATUS = "stop";
	
	public static final String _STR_APPEND = "append";
	public static final String _STR_NORMAL = "normal";
	public static final String _NUMBER_TXT = "number.txt";
	public static final String _STR_TXT = ".txt";
	
	public static HashMap<String, String> wordWareHouse = new HashMap<String, String>();
	public synchronized static void addWordWareHouse(String wordLower, String word) {
		Constants.wordWareHouse.put(wordLower, word);
    }
	public synchronized static Boolean getEqualWordWareHouse(String wordLower) {
		if(Constants.wordWareHouse.containsKey(wordLower)) return true;
		return false;
    }

	
}
