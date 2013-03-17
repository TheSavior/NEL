package com.cse454.nel.scripts;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import com.cse454.nel.DocPreProcessor;
import com.cse454.nel.DocumentProcessor;
import com.cse454.nel.dataobjects.Sentence;
import com.cse454.nel.features.AllWordsHistogramFeatureGenerator;
import com.cse454.nel.features.FeatureWeights;
import com.cse454.nel.features.InLinkFeatureGenerator;
import com.cse454.nel.search.CrossWikiSearcher;

import edu.stanford.nlp.util.StringUtils;

public class DemoScript {
	private boolean isNeedCursorChange = true;

	private final JPanel panel = new JPanel();
	private DocumentProcessor processor;
	private FeatureWeights weights;

	private JTextArea input;
	private JTextPane output;

	private final String wikipedia = "http://en.wikipedia.org/wiki/";
	private JTextField weight1;
	private JTextField weight2;
	private JTextField weight3;

	// Default weights to use
	private int inLinkWeight = 2;
	private int crossWikiWeight = 13;
	private int histogramWeight = 85;

	private static final String INPUT_DEFAULT =
			 "TOKYO , Aug. 19 -LRB- Xinhua -RRB-                                                                                                                                                                                                                                   " +
			 " Japanese Prime Minister Shinzo Abe left Tokyo Sunday morning for a weeklong visits to Indonesia , India and Malaysia .                                                                                                                                               " +
			 " In Jakarta , his first stop , Abe is scheduled to meet with Indonesian President Susilo Bambang Yudhoyono and deliver a speech on Japan 's future policy toward the Association of Southeast Asian Nations -LRB- ASEAN -RRB- on Monday , according to Kyodo News .   " +
			 " Besides the signing of a free trade agreement , the two leaders will issue a joint statement calling for cooperation on environment and energy , Japanese officials were quoted as saying .                                                                          " +
			 " Abe will arrive in New Delhi on Wednesday .                                                                                                                                                                                                                          " +
			 " He is scheduled to make a policy speech at the Indian parliament shortly after arrival and meet with Indian Prime Minister Manmohan Singh later in the day .                                                                                                         " +
			 " To seek India 's cooperation on Japan 's environment-protection initiative to halve global greenhouse emissions from current levels by 2050 is on the premier 's agenda , Japanese officials said .                                                                  " +
			 " In Kuala Lumpur , his last leg , Abe and Malaysian Prime Minister Abdullah Ahmad Badawi is slated to meet on Friday and sign a joint statement calling for cooperation in various areas ranging from politics and security to economy and environment , Kyodo said . " +
			 " When meeting with the press before departure , Abe described the relationship between Japan and India as the most promising bilateral tie and expressed his willingness to send a message during the tour that Japan and ASEAN share the future .                    " +
			 " A business mission of 250-member scale , led by the Japan Business Federation Chairman Fujio Mitarai , will accompany Abe throughout the tour ending Saturday ." +
			 "";

	public DemoScript() throws Exception {
		DocPreProcessor preProcessor = new DocPreProcessor();
		processor = new DocumentProcessor(preProcessor);

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});

		// Here's necessary feature weight input (values will come from gui)

		// Process the document

		// now you could compare each { token | entity } in a scroll bar on the
		// gui or something

	}

	public static void main(String[] args) throws Exception {
		new DemoScript();
	}

	public void Link() {
		weights = new FeatureWeights();
		int in;
		int cross;
		int hist;

		try {
			in = Integer.parseInt(weight1.getText());
		} catch (Exception e) {
			in = inLinkWeight;
		}

		try {
			cross = Integer.parseInt(weight2.getText());
		} catch (Exception e) {
			cross = crossWikiWeight;
		}

		try {
			hist = Integer.parseInt(weight3.getText());
		} catch (Exception e) {
			hist = histogramWeight;
		}

		/*
		 * System.out.println("In: " + Integer.toString(in));
		 * System.out.println("Cross: " + Integer.toString(cross));
		 * System.out.println("Hist: " + Integer.toString(hist));
		 */
		weights.setFeature(InLinkFeatureGenerator.FEATURE_STRING, in);
		weights.setFeature(CrossWikiSearcher.FEATURE_STRING, cross);
		weights.setFeature(AllWordsHistogramFeatureGenerator.FEATURE_STRING,
				hist);

		// Here's the text to be linked (value to come from gui)
		String text = input.getText();
		List<Sentence> results;
		try {
			processor.setFeatureWeights(weights);
			results = processor.processDocument(text);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// JOptionPane.showMessageDialog(null, e.getMessage());
			return;
		}

		StringBuilder builder = new StringBuilder();

		System.out.println(results.size() + " sentences");
		for (Sentence s : results) {
			String[] ents = s.getEntities();
			String[] tokens = s.getTokens();
			if (ents == null) {
				System.out.println("Entities is null");
				continue;
			}
			System.out.println(StringUtils.join(ents, " "));

			boolean isTagOpen = false;
			for (int i = 0; i < tokens.length; i++) {
				// Start a tag if we are an entity and the one before us wasn't
				// the same entity
				if (ents[i] != "0" && !isTagOpen) { // && ((i - 1 > 0) &&
													// (ents[i] != ents[i -
													// 1]))) {
					builder.append("<a href=\"" + wikipedia + ents[i]
							+ "\">");
					isTagOpen = true;
				}
				builder.append(tokens[i]);

				// If the current token is entity AND we have a next token, and
				// that next token isn't the same as this one
				// OR there are no more tokens
				if (ents[i] != "0"
						&& (((i + 1 < ents.length) && (ents[i] != ents[i + 1])) || (i + 1 >= ents.length))) {
					builder.append("</a>");
					isTagOpen = false;
				}
				builder.append(" ");
			}
			builder.append("<br />");

		}
		System.out.println(builder.toString());
		output.setText(builder.toString());
		System.out.println();
	}

	private void createAndShowGUI() {
		// Create and set up the window.
		JFrame frmNamedEntityLinker = new JFrame("HelloWorldSwing");
		frmNamedEntityLinker.setMinimumSize(new Dimension(650, 400));
		frmNamedEntityLinker.getContentPane().setName("Named Entity Linker");
		frmNamedEntityLinker.setTitle("Named Entity Linker");
		frmNamedEntityLinker.getContentPane().setPreferredSize(
				new Dimension(500, 350));
		frmNamedEntityLinker.getContentPane().setMinimumSize(
				new Dimension(500, 350));
		frmNamedEntityLinker.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Display the window.
		frmNamedEntityLinker.pack();
		frmNamedEntityLinker.getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(new BorderLayout(0, 0));

		JPanel panel_1 = new JPanel();
		panel.add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(new BorderLayout(0, 0));

		JSplitPane splitPane = new JSplitPane();
		panel_1.add(splitPane, BorderLayout.CENTER);
		splitPane.setPreferredSize(new Dimension(500, 300));
		splitPane.setMinimumSize(new Dimension(300, 150));
		splitPane.setResizeWeight(0.5);

		output = new JTextPane();
		output.setContentType("text/html");
		output.setEditable(false);
		output.setMinimumSize(new Dimension(100, 22));
		output.setPreferredSize(new Dimension(250, 22));
		output.setText("output");
		output.addHyperlinkListener(new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent event) {
				if (event.getEventType() == HyperlinkEvent.EventType.ENTERED) {
					String tooltip = event.getURL().toExternalForm();
					output.setToolTipText(tooltip.substring(wikipedia.length()));
				} else if (event.getEventType() == HyperlinkEvent.EventType.EXITED) {
					// Reset tooltip
					output.setToolTipText(null);
				} else if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					try {
						browseTo(event.getURL().toURI());
					} catch (Exception ioe) {
						// Some warning to user
					}
				}
			}
		});
		JScrollPane scrollPane = new JScrollPane(output);
		splitPane.setRightComponent(scrollPane);

		input = new JTextArea();
		input.setLineWrap(true);
		input.setMinimumSize(new Dimension(100, 22));
		input.setPreferredSize(new Dimension(250, 22));
		input.setText(INPUT_DEFAULT);

		JScrollPane scrollPane2 = new JScrollPane(input);
		splitPane.setLeftComponent(scrollPane2);

		JButton btnNewButton = new JButton("Link!");
		panel_1.add(btnNewButton, BorderLayout.SOUTH);

		JPanel panel_2 = new JPanel();
		panel.add(panel_2, BorderLayout.SOUTH);
		panel_2.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		JLabel lblWeight = new JLabel("InLink:");
		panel_2.add(lblWeight);

		weight1 = new JTextField();
		weight1.setText(Integer.toString(inLinkWeight));
		panel_2.add(weight1);
		weight1.setColumns(2);

		Component horizontalStrut_1 = Box.createHorizontalStrut(20);
		panel_2.add(horizontalStrut_1);

		JLabel lblWeightW = new JLabel("CrossWiki:");
		panel_2.add(lblWeightW);

		weight2 = new JTextField();
		weight2.setText(Integer.toString(crossWikiWeight));
		weight2.setColumns(2);
		panel_2.add(weight2);

		Component horizontalStrut = Box.createHorizontalStrut(20);
		panel_2.add(horizontalStrut);

		JLabel lblWeight_1 = new JLabel("Histogram:");
		panel_2.add(lblWeight_1);

		weight3 = new JTextField();
		panel_2.add(weight3);
		weight3.setText(Integer.toString(histogramWeight));
		weight3.setColumns(2);
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Linking");
				Link();
			}
		});
		frmNamedEntityLinker.setVisible(true);
	}

	private void browseTo(URI url) {
		if (!java.awt.Desktop.isDesktopSupported()) {
			System.err.println("Desktop is not supported (fatal)");
			System.exit(1);
		}
		java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
		if (!desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
			System.err
					.println("Desktop doesn't support the browse action (fatal)");
			System.exit(1);
		}

		try {
			desktop.browse(url);
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

}
