package com.distributed.processing.main;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.DOMConfiguration;

import com.distributed.processing.common.Constants;
import com.distributed.processing.consumer.ConsumerGroup;
import com.distributed.processing.producer.ProducerGroup;
import com.distributed.processing.producer.vo.Record;
import com.distributed.processing.utils.ObjectFileRWriter;

public class Main {
	private static final Logger LOGGER = LogManager.getLogger(Main.class);
	public static ArrayList<Queue<Record>> recordPartition = new ArrayList<Queue<Record>>();

	public static void main(String[] args) {
		ObjectFileRWriter ofr = new ObjectFileRWriter();
		if (args.length < 3) {
			LOGGER.error("[{}] {} ", "INIT", "Please correct parameter. ex) words.txt result/ 7 10(Not Required)");
			for (int i = 0; i < args.length; i++) {
				LOGGER.error("[{}] {} ", "INIT", "Inputed parameters > (" + i + ") : " + args[i]);
			}
			System.exit(0);
		} else if (args.length > 4) {
			LOGGER.error("[{}] {} ", "INIT", "Please correct parameter.");
			System.exit(0);
		}

		String fileName = args[0];
		String resultDirectory = args[1];
		int partitionCnt = Integer.parseInt(args[2]);

		Long startSeek=0L;
		if (args.length >= 4){
			if(args[3] != ""){
				startSeek = Long.valueOf(args[3]);
			} 
		}

		/*String fileName = "words.txt";
		String resultDirectory = "result/";
		int partitionCnt = 28;
		Long startSeek = 0L; */


		if(partitionCnt == 0) partitionCnt = 1;
		partitionCnt = Math.min(partitionCnt, 28);
		
		for (int i = 0; i < partitionCnt; i++) {
			Queue<Record> q = new LinkedList<>();
			recordPartition.add(i, q);
		}
		
		LOGGER.info("[{}] {} {} {}", "INIT", "FILENAME : "+ fileName, " | RESULT DIRECTORY : " + resultDirectory, " | PARTITION CNT : " + partitionCnt);

		ofr.mkDir(resultDirectory);

		ProducerGroup producerGroup = new ProducerGroup(fileName, partitionCnt, recordPartition, startSeek);
		producerGroup.execute();
		LOGGER.info("[{}] {} ", "INIT", "PRODUCER");

		ConsumerGroup consumerGroup = new ConsumerGroup(resultDirectory, partitionCnt, recordPartition);
		consumerGroup.execute();
		LOGGER.info("[{}] {} ", "INIT", "CONSUMER");

		ofr.writeFile(Constants._THREAD_STATUS_FILE, Constants._THREAD_RUNNING_STATUS, Constants._STR_NORMAL);
	}
}
