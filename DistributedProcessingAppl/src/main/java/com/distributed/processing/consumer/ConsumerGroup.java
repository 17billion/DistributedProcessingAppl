package com.distributed.processing.consumer;

import java.util.ArrayList;
import java.util.Queue;

import com.distributed.processing.producer.vo.Record;

public class ConsumerGroup {

	private ConsumerThread cThread;

	public ConsumerGroup(String resultDirectory, int partitionCnt, ArrayList<Queue<Record>> recordPartition) {
		cThread = new ConsumerThread(resultDirectory, partitionCnt, recordPartition);
	}

	public void execute() {

		Thread t = new Thread(cThread);
		t.start();
		return;
	}
}