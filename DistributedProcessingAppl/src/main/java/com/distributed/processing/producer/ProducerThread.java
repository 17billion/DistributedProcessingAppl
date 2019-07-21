package com.distributed.processing.producer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Queue;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.distributed.processing.common.Constants;
import com.distributed.processing.producer.vo.Record;
import com.distributed.processing.utils.ObjectFileRWriter;

public class ProducerThread implements Runnable {

	private static final Logger LOGGER = LogManager.getLogger(ProducerThread.class);
	String fileName;
	int partitionCnt;
	ArrayList<Queue<Record>> recordPartition;
	Long startSeek;

	public ProducerThread(String fileName, int partitionCnt, ArrayList<Queue<Record>> recordPartition, Long startSeek) {
		this.fileName = fileName;
		this.partitionCnt = partitionCnt;
		this.recordPartition = recordPartition;
		this.startSeek = startSeek;
	}

	public void run() {
		ObjectFileRWriter ofrw = new ObjectFileRWriter();
		int pNum = 0;
		int cnt = 0;
		int pocsCnt = 0;
		Boolean complFlag = false;
		while (true) {
			String threadStatus = ofrw.readFile(Constants._THREAD_STATUS_FILE);
			if (threadStatus.contains(Constants._THREAD_STOP_STATUS)) {
				LOGGER.info("[{}] {} ", "PRODUCER", "LAST SEEK : " + startSeek+" line : " + cnt);				
				LOGGER.debug("[{}] {} ", "PRODUCER", "Processed count : " + pocsCnt);
				LOGGER.info("[{}] {} ", "PRODUCER", "THREAD TERMINATED");
				Thread.currentThread().interrupt();
				return;
			}

			ArrayList<Record> wordList = null;
			try {
				wordList = ofrw.readFileTail(fileName, startSeek);
			} catch (IOException e1) {
				e1.printStackTrace();
			} 
			
			if (wordList.size() == 0) {
				try {
					if (complFlag) {
						LOGGER.info("[{}] {} ", "PRODUCER", cnt + " lines processing is complete.");
						complFlag=false;
					}
					Thread.sleep(10000);
					continue;
				} catch (InterruptedException e) {

				}
			}

			
			String word = "";
			for (Record record : wordList) {
				if (record.getWord().length() == 0) {
					continue;
				}
				word = record.getWord();
				startSeek = record.getSeekNum();
				String firstStr = word.substring(0, 1);
				cnt++;
				if (cnt % 10000 == 0) {
					LOGGER.info("[{}] {} ", "PRODUCER", "line : " + cnt + " | WORD : " + word+ " (SEEK : " + startSeek+")");		
				}
				if (Pattern.matches("^[a-zA-Z0-9]*$", firstStr)) {
					pocsCnt++;
					recordPartition.get(pNum).add(record);
					pNum++;
					if (pNum >= partitionCnt) {
						pNum = 0;
					}
				}
			}
			complFlag=true;
			LOGGER.info("[{}] {} ", "PRODUCER", "line : " + cnt + " | WORD : " + word+ " (SEEK : " + startSeek+")");
			
			
		}
	}
}