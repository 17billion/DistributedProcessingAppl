package com.distributed.processing.producer.vo;

public class Record {
	String word;
	Long seekNum;
	
	public String getWord() {
		return word;
	}
	public void setWord(String word) {
		this.word = word;
	}
	public Long getSeekNum() {
		return seekNum;
	}
	public void setSeekNum(Long seekNum) {
		this.seekNum = seekNum;
	}
	@Override
	public String toString() {
		return "Record [word=" + word + ", seekNum=" + seekNum + "]";
	}	
}
