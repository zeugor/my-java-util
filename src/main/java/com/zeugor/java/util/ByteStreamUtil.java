package com.zeugor.java.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.apache.commons.io.input.TeeInputStream;
import org.apache.commons.io.output.TeeOutputStream;
import org.apache.log4j.Logger;

public class ByteStreamUtil {

	private static final Logger log = Logger.getLogger(ByteStreamUtil.class);

	public static final Set<InputStream> splitInputStream(InputStream input) throws IOException {
		
		PipedOutputStream pipedOut01 = new PipedOutputStream();
		PipedOutputStream pipedOut02 = new PipedOutputStream();
		
		TeeOutputStream tout = new TeeOutputStream(pipedOut01, pipedOut02);
		
		TeeInputStream tin = new TeeInputStream(input, tout, true);

		Executors.newSingleThreadExecutor().submit(tin::readAllBytes);	
		
		Set<InputStream> inputStreamSet = new HashSet<>();
		inputStreamSet.add(new PipedInputStream(pipedOut01));
		inputStreamSet.add(new PipedInputStream(pipedOut02));		
		
		return Collections.unmodifiableSet(inputStreamSet);
	}

	public static final Set<InputStream> splitInputStream(InputStream input, int numberOfSplits) {
		checkArgs(input, numberOfSplits);

		List<PipedOutputStream> pipedOutList = new ArrayList<>();

		TeeInputStream tin = createSplits(input, pipedOutList, numberOfSplits);

		startTransmission(tin);
		
		return toPipedInputStreamSet(pipedOutList);
	}

	private static final void checkArgs(InputStream input, int numberOfSplits) {
		if (input == null) { 
			throw new IllegalArgumentException("input can't be null.");
		}

		if (numberOfSplits < 2) {
			throw new IllegalArgumentException("number-of-splits must be bigger or equal than 2.");
		}
	}

	private static final TeeInputStream createSplits(InputStream input,
			List<PipedOutputStream> pipedOutList, int num) {
		
		if (pipedOutList.isEmpty()) {
			pipedOutList.add(new PipedOutputStream());
		}

		TeeInputStream tin = null;
		InputStream newInput = input;
		for (int i = 0; i < num - 1; i++) {
			tin = (TeeInputStream) createAnSplit(newInput, pipedOutList);
			newInput = tin;
		}		

		if (num != pipedOutList.size()) {
			throw new IllegalStateException("invalid number of splits. Should be: " + num + ", but it is: " + pipedOutList.size());
		}

		return  tin;
	}

	private static final TeeInputStream createAnSplit(InputStream input, List<PipedOutputStream> pipedOutList) {
		
		PipedOutputStream lastListedPipedOut = pipedOutList.get(pipedOutList.size() - 1);

		PipedOutputStream pipedOut = new PipedOutputStream();

		OutputStream teeOutputStream = new TeeOutputStream(lastListedPipedOut, pipedOut);
		
		pipedOutList.add(pipedOut);

		TeeInputStream tin = new TeeInputStream(input, teeOutputStream, true); 

		return tin;
	}
	
	private static final void startTransmission(TeeInputStream tin) {
		Executors
			.newSingleThreadExecutor()
			.submit(tin::readAllBytes);	
	}

	private static final Set<InputStream> toPipedInputStreamSet(List<PipedOutputStream> pipedOutList) {
		return pipedOutList
				.stream()
				.map(ByteStreamUtil::toPipedInputStream)
				.filter(Objects::nonNull)
				.collect(Collectors.toUnmodifiableSet()); 
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
