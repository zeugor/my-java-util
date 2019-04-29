package com.zeugor.java.util;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.io.output.ProxyOutputStream;

public class TeeListOutputStream extends ProxyOutputStream {
	private List<OutputStream> branchList;

	public TeeListOutputStream(final List<? extends OutputStream> branchList) {
		super(branchList.get(0));
		this.branchList = branchList;
	}

	@Override
	public synchronized void write(final byte[] b) throws IOException {
		for (OutputStream branch : branchList) {
			branch.write(b);
		}
	}

	@Override
	public synchronized void write(final byte[] b, final int off, final int len) throws IOException {
		for (OutputStream branch : branchList) {
			branch.write(b, off, len);
		}
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
