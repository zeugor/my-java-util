package com.zeugor.java.util;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Test;

import junit.framework.Assert;

public class ByteStreamUtilTest {
	
	private static final Random r = new Random(Instant.now().toEpochMilli());

	private static final int BUFFER_LENGTH = 128;

	private static int getRandomSeconds() {
		int min = 0;
		int max = 10;

		return r.ints(min, (max + 1)).findFirst().getAsInt();
	}

	@Test
	public static void test2() throws IOException, InterruptedException, ExecutionException {
		int streamSize = 10;
		InputStream inputStream = writeEvery1SecInputStream(streamSize);

		final int NUMBER_OF_SPLITS = 3;

		List<InputStream> list = //ByteStreamUtil.splitInputStream(inputStream, NUMBER_OF_SPLITS);
				ByteStreamUtil.splitInputStream(inputStream, NUMBER_OF_SPLITS);
//
//		List<Future<Integer>> futuresList = new ArrayList<>();
//
//		for (int i = 0; i < set.size(); i++) {
//
//			Future<Integer> size = readEveryNSec(i, set., getRandomSeconds());
//			
//			futuresList.add(size);
//		}
		
		for (int i = 0; i < list.size(); i++) {

			readEveryNSec(i, list.get(i), getRandomSeconds());
			
		}
		



		
	}
	
//	@Test
	public void test() {
		InputStream inputStream =
//				writeEvery1Sec();	
				System.in;

		final int NUMBER_OF_SPLITS = 3;

		List<InputStream> list = ByteStreamUtil.splitInputStream(inputStream, NUMBER_OF_SPLITS);


		for (int i = 0; i < list.size(); i++) {

			readEveryNSec(i, list.get(i), getRandomSeconds());
		}	}

	static Future<Integer> readEveryNSec(int idx, InputStream in, int seconds) {

		new Thread() {
			public void run() {
				read(idx, in, seconds);
			}
		}.start();
		
//		return Executors.newSingleThreadExecutor().submit(() -> read(idx, in, seconds));
		return null;
	}
	
	static Integer read(int idx, InputStream in, int seconds) {

		byte[] array = new byte[BUFFER_LENGTH];
		int l;

		int counter = 0;
		try {

			while ((l = in.read(array)) != -1) {

				Thread.sleep(seconds * 1000);

				String s = new String(array);
				System.out.println("[" + idx + "--" + seconds + "s]" + s);

				counter++;

				array = new byte[BUFFER_LENGTH];
			}

			
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

//					System.out.println("readEvery3Sec out");

		return counter;
	}	
	
	private static final InputStream writeEvery1SecInputStream(int streamSize) throws IOException {
		PipedOutputStream baos = new PipedOutputStream();

		new Thread() {
			@Override
			public void run() {

				int c = 0;

				while (true) {
					try {

						Thread.sleep(1000);

						String s = c++ + ")source\n";
//						System.out.println("sout: " + s);
						
						baos.write(s.getBytes());

						if (c > streamSize) {
							baos.close();
							return;
						}

					} catch (InterruptedException | IOException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();


		return new PipedInputStream(baos);
	}
	
	public static void main(String...strings) throws IOException, InterruptedException, ExecutionException {
		test2();
	}
}
