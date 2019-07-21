package com.distributed.processing.producer;

import java.util.ArrayList;
import java.util.Queue;

import com.distributed.processing.producer.vo.Record;

public class ProducerGroup {

	ProducerThread pThread;
	
	public ProducerGroup(String fileName, int partitionCnt, ArrayList<Queue<Record>>  recordPartition, Long startSeek) {
		this.pThread = new ProducerThread(fileName, partitionCnt, recordPartition, startSeek);
	}

	public void execute() {
		Thread t = new Thread(this.pThread);
		t.start();
		return;
	}
}