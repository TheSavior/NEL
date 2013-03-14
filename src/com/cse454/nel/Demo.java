package com.cse454.nel;

import java.util.List;

import com.cse454.nel.features.AllWordsHistogramFeatureGenerator;
import com.cse454.nel.features.FeatureWeights;
import com.cse454.nel.features.InLinkFeatureGenerator;
import com.cse454.nel.search.CrossWikiSearcher;

import edu.stanford.nlp.util.StringUtils;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;

import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;

public class Demo {
	private  boolean isNeedCursorChange = true;

	private  final JPanel panel = new JPanel();
	private  DocumentProcessor processor;
	private  FeatureWeights weights;

	private  JTextArea input;
	private  JEditorPane output;

	public static String HTML = "<html>\n" + "<body>\n"
			+ "Click on the link in the editale JEditorPane <br>\n"
			+
			// "<a href=\"http://java.sun.com\">\nlink</a>" +
			"<a href=\"file:///c:/temp/test.html\">\nlink</a>" + "</body>\n"
			+ "</html>";

	public Demo() throws Exception {

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});

		DocPreProcessor preProcessor = new DocPreProcessor();
		processor = new DocumentProcessor(preProcessor);

		// Here's necessary feature weight input (values will come from gui)
		int crossWikiWeight = 1;
		int inLinkWeight = 1;
		int histogramWeight = 1;

		weights = new FeatureWeights();
		weights.setFeature(InLinkFeatureGenerator.FEATURE_STRING, inLinkWeight);
		weights.setFeature(CrossWikiSearcher.FEATURE_STRING, crossWikiWeight);
		weights.setFeature(AllWordsHistogramFeatureGenerator.FEATURE_NAME,
				histogramWeight);

		// Process the document

		// now you could compare each { token | entity } in a scroll bar on the
		// gui or something

	}
	public static void main(String[] args) throws Exception {
		new Demo();
	}

	public void Link() {
		// Here's the text to be linked (value to come from gui)
		String text = input.getText();
		List<Sentence> results;
		try {
			results = processor.ProcessDocument(weights, text);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// JOptionPane.showMessageDialog(null, e.getMessage());
			return;
		}

		System.out.println(results.size() + " sentences");
		for (Sentence s : results) {
			if (s.getEntities() == null) {
				System.out.println("Entities is null");
				continue;
			}
			System.out.println(StringUtils.join(s.getEntities(), " "));
		}
		System.out.println();
	}

	private void createAndShowGUI() {
		// Create and set up the window.
		JFrame frmNamedEntityLinker = new JFrame("HelloWorldSwing");
		frmNamedEntityLinker.getContentPane().setName("Named Entity Linker");
		frmNamedEntityLinker.setTitle("Named Entity Linker");
		frmNamedEntityLinker.getContentPane().setPreferredSize(
				new Dimension(500, 350));
		frmNamedEntityLinker.getContentPane().setMinimumSize(
				new Dimension(500, 350));
		frmNamedEntityLinker.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Display the window.
		frmNamedEntityLinker.pack();
		frmNamedEntityLinker.getContentPane().add(panel, BorderLayout.NORTH);
		panel.setLayout(new BorderLayout(0, 0));

		JSplitPane splitPane = new JSplitPane();
		splitPane.setPreferredSize(new Dimension(500, 300));
		splitPane.setMinimumSize(new Dimension(300, 150));
		splitPane.setResizeWeight(0.5);
		panel.add(splitPane, BorderLayout.CENTER);

		input = new JTextArea();
		input.setLineWrap(true);
		input.setMinimumSize(new Dimension(100, 22));
		input.setPreferredSize(new Dimension(250, 22));
		input.setText("input");
		splitPane.setLeftComponent(input);

		MyHTMLEditorKit kit = new MyHTMLEditorKit();
		output = new JTextPane() {
			public void setCursor(Cursor cursor) {
				if (isNeedCursorChange) {
					super.setCursor(cursor);
				}
			}
		};
		output.setEditorKit(kit);
		output.setEditable(false);
		output.setMinimumSize(new Dimension(100, 22));
		output.setPreferredSize(new Dimension(250, 22));
		output.setText(HTML);
		output.addHyperlinkListener(new HTMLListener());
		splitPane.setRightComponent(output);

		JButton btnNewButton = new JButton("Link!");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Linking");
				Link();
			}
		});
		panel.add(btnNewButton, BorderLayout.SOUTH);
		frmNamedEntityLinker.setVisible(true);
	}

	private class HTMLListener implements HyperlinkListener {
		public void hyperlinkUpdate(HyperlinkEvent e) {
			if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
				try {
					output.setPage(e.getURL());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	public class MyHTMLEditorKit extends HTMLEditorKit {

		MyLinkController handler = new MyLinkController();

		public void install(JEditorPane c) {
			MouseListener[] oldMouseListeners = c.getMouseListeners();
			MouseMotionListener[] oldMouseMotionListeners = c
					.getMouseMotionListeners();
			super.install(c);
			// the following code removes link handler added by original
			// HTMLEditorKit

			for (MouseListener l : c.getMouseListeners()) {
				c.removeMouseListener(l);
			}
			for (MouseListener l : oldMouseListeners) {
				c.addMouseListener(l);
			}

			for (MouseMotionListener l : c.getMouseMotionListeners()) {
				c.removeMouseMotionListener(l);
			}
			for (MouseMotionListener l : oldMouseMotionListeners) {
				c.addMouseMotionListener(l);
			}

			// add out link handler instead of removed one
			c.addMouseListener(handler);
			c.addMouseMotionListener(handler);
		}

		public class MyLinkController extends LinkController {

			public void mouseClicked(MouseEvent e) {
				JEditorPane editor = (JEditorPane) e.getSource();

				if (editor.isEditable() && SwingUtilities.isLeftMouseButton(e)) {
					if (e.getClickCount() == 2) {
						editor.setEditable(false);
						super.mouseClicked(e);
						editor.setEditable(true);
					}
				}

			}

			public void mouseMoved(MouseEvent e) {
				JEditorPane editor = (JEditorPane) e.getSource();

				if (editor.isEditable()) {
					isNeedCursorChange = false;
					editor.setEditable(false);
					isNeedCursorChange = true;
					super.mouseMoved(e);
					isNeedCursorChange = false;
					editor.setEditable(true);
					isNeedCursorChange = true;
				}
			}

		}
	}
}
