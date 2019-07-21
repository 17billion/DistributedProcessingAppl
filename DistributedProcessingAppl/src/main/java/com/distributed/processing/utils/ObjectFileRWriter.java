package com.distributed.processing.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.distributed.processing.common.Constants;
import com.distributed.processing.producer.vo.Record;

public class ObjectFileRWriter {
	private static final Logger LOGGER = LogManager.getLogger(ObjectFileRWriter.class);

	public void writeFile(String fileName, String str, String type) {
		try {
			File file = new File(fileName);
			FileWriter fileWriter;
			BufferedWriter br;
			if (type == Constants._STR_APPEND) {
				fileWriter = new FileWriter(file, true);
			} else {
				fileWriter = new FileWriter(file);
			}

			br = new BufferedWriter(fileWriter);
			br.write(str);
			br.newLine();
			br.close();
			LOGGER.debug("[{}] {} ", "WRITE", "filename : " + fileName + " " + " status : " + str);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String readFile(String fileName) {
		StringBuilder contentBuilder = new StringBuilder();

		try (Stream<String> stream = Files.lines(Paths.get(fileName), StandardCharsets.UTF_8)) {
			stream.forEach(s -> contentBuilder.append(s).append("\n"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		return contentBuilder.toString();
	}

	public ArrayList<Record> readFileTail(String fileName, Long seek) throws IOException {
		File f = new File(fileName);
		ArrayList<Record> wordList = new ArrayList<Record>();
		RandomAccessFile raf = null;
		String word;
		try {
			raf = new RandomAccessFile(f, "rw");
			raf.seek(seek);
			while ((word = raf.readLine()) != null) {
				Record r = new Record();
				r.setWord(word);
				r.setSeekNum(raf.getFilePointer());
				wordList.add(r);
			}
		} catch (FileNotFoundException e) {
			return wordList;
		} finally {
			if (raf != null) {
				raf.close();
			}
		}

		return wordList;
	}

	public void mkDir(String dirName) {
		File file = new File(dirName);
		if (!file.exists()) {
			LOGGER.debug("[{}] {} ", "MKDIR", "file.mkdir() : " + file.mkdirs());
		}
	}
}