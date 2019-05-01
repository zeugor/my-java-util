package com.zeugor.java.util;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class TeeListOutputStream extends OutputStream {
	private final List<? extends OutputStream> branchList;

	public TeeListOutputStream(final List<? extends OutputStream> branchList) {
		this.branchList = branchList;
	}

	@Override
	public synchronized void write(final int b) throws IOException {
		for (OutputStream branch : branchList) {
			branch.write(b);
		}
	}

	@Override
	public void flush() throws IOException {
		for (OutputStream branch : branchList) {
			branch.flush();
		}
	}

	@Override
	public void close() throws IOException {
		for (OutputStream branch : branchList) {
			branch.close();
		}
	}
}
