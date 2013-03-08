package com.cse454.nel.kbp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;



/**
 * An iterator of sentences and their annotations from the corpus. Do not forget
 * to call close() after use.
 *
 * @author xiaoling
 *
 */
public class ProcessedCorpus implements Iterator<Map<String, String>> {

	private final String processedDocPath;

	public ProcessedCorpus() throws Exception {
		this(KbpConstants.processedDocPath, SFConstants.dataTypes);
	}

	public ProcessedCorpus(String docPath) throws Exception {
		this(docPath, SFConstants.dataTypes);
	}

	public ProcessedCorpus(String docPath, String[] dts) throws Exception {
		dataTypes = dts;
		this.processedDocPath = docPath;
		init();
	}

	protected String[] dataTypes = null;

	protected Map<String, BufferedReader> dataReaders = null;

	protected Map<String, String> cur = null;

	// caching the line when hasNext is called.
	protected String cache = null;

	public void init() throws Exception {
		dataReaders = new HashMap<String, BufferedReader>();
		cur = new HashMap<String, String>();
		if (dataTypes.length == 0) {
			throw new Exception("No data type is specified");
		}
		for (String dataType : dataTypes) {
			try {
				String filename = processedDocPath
						+ SFConstants.prefix + "." + dataType;
				if (new File(filename).exists()) {
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(
									new FileInputStream(filename), "UTF-8"));
					dataReaders.put(dataType, reader);
					cur.put(dataType, null);
				} else {
					throw new Exception("The data type [" + dataType
							+ "] does not exist. filename = "+filename);
				}
			} catch (UnsupportedEncodingException | FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	public void close() {
		for (BufferedReader reader : dataReaders.values()) {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * read the next line from dataTypes[0], cache it for later use.
	 */
	@Override
	public boolean hasNext() {
		if (cache != null) {
			return true;
		} else {
			try {
				cache = dataReaders.get(dataTypes[0]).readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (cache == null) {
				cur =null ;
				return false;
			} else {
				return true;
			}
		}
	}

	@Override
	public Map<String, String> next() {
		if (cur==null) {
			return cur;
		}
		if (cache != null) {
			cur.put(dataTypes[0], cache);
			cache = null;
		} else {
			try {
				cur.put(dataTypes[0], dataReaders.get(dataTypes[0]).readLine());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		for (int i = 1; i < dataTypes.length; ++i) {
			try {
				cur.put(dataTypes[i], dataReaders.get(dataTypes[i]).readLine());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return cur;
 	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
