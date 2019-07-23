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
	int pNum;

	public ConsumerThread(String resultDirectory, int pNum, ArrayList<Queue<Record>> recordPartition) {
		this.resultDirectory = resultDirectory;
		this.recordPartition = recordPartition;
		this.pNum = pNum;
	}

	public void run() {
		int cnt = 0;
		int duplCnt = 0;
		String word = "";
		Boolean complFlag = false;
		Long seek = 0L;
		LOGGER.info("[{}] THREAD_ID={} ", "CONSUMER THREAD START", Thread.currentThread().getId());
		while (!Thread.currentThread().isInterrupted()) {
			String threadStatus = ofrw.readFile(Constants._THREAD_STATUS_FILE);
			if (threadStatus.contains(Constants._THREAD_STOP_STATUS)) {
				LOGGER.info("[{}] {} ", "CONSUMER", "LAST WORD : " + word + " (SEEK : " + seek + ")");
				LOGGER.info("[{}] {} ", "CONSUMER", "Duplicate Count : " + duplCnt);
				LOGGER.info("[{}] {} ", "CONSUMER", "Apply Count : " + Constants.wordWareHouse.size());
				LOGGER.info("[{}] {} ", "CONSUMER", "THREAD TERMINATED");
				Thread.currentThread().interrupt();
				return;
			}

			if (recordPartition.get(pNum).size() < 1) {
				try {

					if (complFlag) {
						LOGGER.info("[{}] {} ", "CONSUMER", "WORD : " + word + " (SEEK : " + seek + ")");
						LOGGER.info("[{}] {} ", "CONSUMER", "Consumer processing is complete (SEEK : " + seek + ")");
						complFlag = false;
					}
					Thread.sleep(1000);
					continue;
				} catch (InterruptedException e) {

				}
			} else if(recordPartition.get(pNum)!=null){
				cnt++;
				Record r = recordPartition.get(pNum).poll();
				try{
				word = r.getWord();
				} catch (NullPointerException e) {
					System.out.println("recordPartition.get(pNum) : "  +recordPartition.get(pNum).toString());
					System.out.println("r.toString() : "  +r.toString());
					System.out.println("계속 발생한다고?");
					continue;
				}
				seek = r.getSeekNum();
				if (cnt % 10000 == 0) {
					LOGGER.info("[{}] {} ", "CONSUMER", "WORD : " + word + " (SEEK : " + seek + ")");
				}
				String wordLower = word.toLowerCase();
				String FirstStr = wordLower.substring(0, 1);
				if (Constants.getEqualWordWareHouse(wordLower)) {					
					duplCnt++;
					continue;
				}
				if (Pattern.matches("^[0-9]*$", FirstStr)) {
					ofrw.writeFile(resultDirectory + "/" + Constants._NUMBER_TXT, word, Constants._STR_APPEND);
				} else {
					ofrw.writeFile(resultDirectory + "/" + FirstStr + Constants._STR_TXT, word, Constants._STR_APPEND);
				}
				Constants.addWordWareHouse(wordLower, word);
				complFlag = true;
			}
		}
	}
}
