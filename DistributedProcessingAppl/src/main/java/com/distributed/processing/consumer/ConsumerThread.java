package com.distributed.processing.consumer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.distributed.processing.common.Constants;
import com.distributed.processing.producer.vo.Record;
import com.distributed.processing.utils.ObjectFileRWriter;

public class ConsumerThread implements Runnable {

	private static final Logger LOGGER = LogManager.getLogger(ConsumerThread.class);
	String resultDirectory;
	int partitionCnt;
	ArrayList<Queue<Record>> recordPartition;
	ObjectFileRWriter ofrw = new ObjectFileRWriter();
	HashMap<String, String> wordWareHouse = new HashMap<String, String>();

	public ConsumerThread(String resultDirectory, int partitionCnt, ArrayList<Queue<Record>> recordPartition) {
		this.resultDirectory = resultDirectory;
		this.recordPartition = recordPartition;
	}

	public void run() {
		int cnt = 0;
		String word = "";
		Boolean complFlag = false;
		Long seek = 0L;
		while (!Thread.currentThread().isInterrupted()) {
			for (int pNum = 0; pNum < recordPartition.size(); pNum++) {
				String threadStatus = ofrw.readFile(Constants._THREAD_STATUS_FILE);
				if (threadStatus.contains(Constants._THREAD_STOP_STATUS)) {
					LOGGER.info("[{}] {} ", "CONSUMER", "THREAD TERMINATED");
					LOGGER.info("[{}] {} ", "CONSUMER", "LAST WORD : " + word + " (SEEK : " + seek+")");	
					Thread.currentThread().interrupt();
					return;
				}
				
				if (recordPartition.get(pNum).size() == 0) {
					try {
						if(cnt==0 && pNum != 0){
							pNum=-1;
						} else{
							pNum--;
						}
						
						if (complFlag) {
							LOGGER.info("[{}] {} ", "CONSUMER", "WORD : " + word + " (SEEK : " + seek+")");		
							LOGGER.info("[{}] {} ", "CONSUMER", "Consumer processing is complete (SEEK : " + seek+")");
							complFlag=false;							
						}						
						Thread.sleep(1000);						
						continue;
					} catch (InterruptedException e) {

					}
				}
				
				cnt++;
				Record  r = recordPartition.get(pNum).poll();
				word = r.getWord();
				seek = r.getSeekNum();
				if(cnt % 10000  == 0){
					LOGGER.info("[{}] {} ", "CONSUMER", "WORD : " + word + " (SEEK : " + seek+")");		
				}
				String wordLower = word.toLowerCase();
				String FirstStr = wordLower.substring(0, 1);
				if (wordWareHouse.containsKey(wordLower)) {
					continue;
				}
				if (Pattern.matches("^[0-9]*$", FirstStr)) {
					ofrw.writeFile(resultDirectory + "/" + Constants._NUMBER_TXT, word, Constants._STR_APPEND);
				} else {
					ofrw.writeFile(resultDirectory + "/" + FirstStr + Constants._STR_TXT, word, Constants._STR_APPEND);
				}
				wordWareHouse.put(wordLower, word);
				complFlag=true;
			}
		}
	}
}
