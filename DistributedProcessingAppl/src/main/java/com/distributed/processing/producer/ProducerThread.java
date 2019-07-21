package com.distributed.processing.producer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
		Boolean complFlag = false;
		//HashMap<String, Integer> wordWareHouse = new HashMap<String, Integer>();
		String word = "";
		while (true) {
			String threadStatus = ofrw.readFile(Constants._THREAD_STATUS_FILE);			
			if (threadStatus.contains(Constants._THREAD_STOP_STATUS)) {
				LOGGER.info("[{}] {} ", "PRODUCER", "THREAD TERMINATED");
				LOGGER.info("[{}] {} ", "PRODUCER", "LAST WORD : " + word+ " (SEEK : " + startSeek+")" + " line : " + cnt);
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

			
			
			for (Record record : wordList) {
				if (record.getWord().length() == 0) {
					continue;
				}
				
				word = record.getWord();
				//if(wordWareHouse.containsKey(word)){
				//	System.out.println(word);
				//}
				startSeek = record.getSeekNum();
				String firstStr = word.substring(0, 1);
				//wordWareHouse.put(word, pNum);
				cnt++;
				if (cnt % 10000 == 0) {
					LOGGER.info("[{}] {} ", "PRODUCER", "line : " + cnt + " | WORD : " + word+ " (SEEK : " + startSeek+")");		
				}
				if (Pattern.matches("^[a-zA-Z0-9]*$", firstStr)) {
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