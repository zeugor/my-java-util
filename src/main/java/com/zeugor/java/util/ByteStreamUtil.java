package com.zeugor.java.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.channels.IllegalSelectorException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.io.input.TeeInputStream;
import org.apache.commons.io.output.TeeOutputStream;
import org.apache.log4j.Logger;

public class ByteStreamUtil {

	private static final Logger log = Logger.getLogger(ByteStreamUtil.class);

	public static final List<InputStream> splitInputStream(InputStream input) throws IOException {
		
		PipedOutputStream pipedOut01 = new PipedOutputStream();
		PipedOutputStream pipedOut02 = new PipedOutputStream();
		
		TeeOutputStream tout = new TeeOutputStream(pipedOut01, pipedOut02);
		
		TeeInputStream tin = new TeeInputStream(input, tout, true);

		Executors.newSingleThreadExecutor().submit(tin::readAllBytes);	
		
		List<InputStream> inputStreamList = new ArrayList<>();
		inputStreamList.add(new PipedInputStream(pipedOut01));
		inputStreamList.add(new PipedInputStream(pipedOut02));		
		
		return Collections.unmodifiableList(inputStreamList);
	}

	public static final List<InputStream> splitInputStream(InputStream input, int numberOfSplits) {
		
		checkArgs(input, numberOfSplits);
		
		List<PipedOutputStream> pipedOutList = createPipedOutputStreamList(numberOfSplits);
		List<InputStream> inputStreamList = connectToPipedInputStreams(pipedOutList);
		
		TeeListOutputStream tout = new TeeListOutputStream(pipedOutList);

		TeeInputStream tin = new TeeInputStream(input, tout, true); 
		
		startTransmission(tin);		
				
		if (numberOfSplits != inputStreamList.size()) {
			throw new IllegalStateException("incorrect number of generated inputstreams");
		}
		
		return Collections.unmodifiableList(inputStreamList);
	}

	private static final void checkArgs(InputStream input, int numberOfSplits) {
		if (input == null) { 
			throw new IllegalArgumentException("input can't be null.");
		}

		if (numberOfSplits < 2) {
			throw new IllegalArgumentException("number-of-splits must be bigger or equal than 2.");
		}
	}
	
	private static List<PipedOutputStream> createPipedOutputStreamList(int listSize) {
		List<PipedOutputStream> pipedOutList = new ArrayList<>();
		for (int i = 0; i < listSize; i++) {
			pipedOutList.add(new PipedOutputStream());
		}
		return pipedOutList;
	}
	
	private static final void startTransmission(TeeInputStream tin) {
		new Thread() {
			public void run() {
				try {
					tin.readAllBytes();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.start();
//		Executors
//			.newSingleThreadExecutor()
//			.submit(tin::readAllBytes);	
	}

	private static final List<InputStream> connectToPipedInputStreams(List<PipedOutputStream> pipedOutList) {
		return ((Collection<PipedOutputStream>) pipedOutList)
				.stream()
				.map(ByteStreamUtil::toPipedInputStream)
				.filter(Objects::nonNull)
				.collect(Collectors.toUnmodifiableList()); 
	}
	
	private static final PipedInputStream toPipedInputStream(PipedOutputStream pipedOut) {
		try {
			return new PipedInputStream(pipedOut);
		} catch (IOException e) {
			log.warn("an error while connecting a PipedInputStream to a PipedOutputStream. A split will be missed.", e);
			cleanedClose(pipedOut);
		}
		return null;
	}
	
	private static final void cleanedClose(Closeable closeable) {
		try {
			closeable.close();
		} catch (IOException ex) {
			log.error("while closing" + closeable, ex);
		}
	}

}
