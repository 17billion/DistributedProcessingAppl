package com.distributed.processing.consumer;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import com.distributed.processing.producer.vo.Record;


public class ConsumerGroup {

	private List<ConsumerThread> cThreads;

	public ConsumerGroup(String resultDirectory, int partitionCnt, ArrayList<Queue<Record>> recordPartition) {
		cThreads = new ArrayList<>();
		
		for (int i = 0; i < partitionCnt; i++) {
			ConsumerThread ct = new ConsumerThread(resultDirectory, i, recordPartition);
			cThreads.add(ct);
		}
	}

	public void execute() {
		for (ConsumerThread cThread : cThreads) {
		Thread t = new Thread(cThread);
		t.start();
		}
		return;
	}
	

}