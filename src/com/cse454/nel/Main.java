package com.cse454.nel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.List;

import com.cse454.nel.MultiDocumentProcessor.ProcessedDocumentCallback;
import com.cse454.nel.dataobjects.Sentence;
import com.cse454.nel.document.AbstractDocument;
import com.cse454.nel.document.DocumentFactory;

/**
 * Here's a sample class showing how simple it is to use our NEL system.
 *
 */
public class Main {

	private static final int NUM_THREADS = 1;
	private static final String FOLDER = "./docs/";

	public static void main(String[] args) throws Exception {
		// Stanford NER commandeers System.err with annoying messages
		Util.PreventStanfordNERErrors();

		// Create a new doc factory for our document files
		DocPreProcessor preProcessor = new DocPreProcessor();
		FileDocumentFactory docFactory = new FileDocumentFactory(FOLDER, preProcessor);

		// Multi-doc processor processes many documents at once using our factory
		MultiDocumentProcessor multiDocProcessor = new MultiDocumentProcessor(4);
		// Add our callback to retrieve the results for each document
		multiDocProcessor.addProcessDocumentListener(new ProcessedDocumentCallback() {

			@Override
			public void onDocumentFinished(AbstractDocument document) {
				// write the entities to a file?
			}
			@Override
			public void onProcessError(Exception e) {
				// error processing file
			}
		});

		// now process the docs!
		multiDocProcessor.ProcessDocuments(docFactory);
	}

	private static class FileDocumentFactory implements DocumentFactory {

		private final File[] files;
		private final DocPreProcessor preprocessor;

		private int curDoc = 0;

		public FileDocumentFactory(String folder, DocPreProcessor preProcessor) {
			this.files = new File(folder).listFiles();
			this.preprocessor = preProcessor;
		}

		@Override
		public AbstractDocument NextDocument() {
			if (curDoc == files.length) {
				return null;
			}
			FileDocument doc = new FileDocument(files[curDoc], preprocessor);
			curDoc++;
			return doc;
		}
	}

	private static class FileDocument extends AbstractDocument {

		private final File file;
		private final DocPreProcessor preprocessor;

		protected FileDocument(File file, DocPreProcessor preProcessor) {
			super(file.getAbsolutePath());
			this.file = file;
			this.preprocessor = preProcessor;
		}

		@Override
		protected List<Sentence> GenerateSentences() throws Exception {
			return preprocessor.ProccessArticle(readFile(file));
		}

		private static String readFile(File file) throws IOException {
		  FileInputStream stream = new FileInputStream(file);
		  try {
		    FileChannel fc = stream.getChannel();
		    MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
		    // Instead of using default, pass in a decoder.
		    return Charset.defaultCharset().decode(bb).toString();
		  }
		  finally {
		    stream.close();
		  }
		}
	}
}
