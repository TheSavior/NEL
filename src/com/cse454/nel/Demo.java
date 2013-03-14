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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;

public class Demo {
	private boolean isNeedCursorChange = true;

	private final JPanel panel = new JPanel();
	private DocumentProcessor processor;
	private FeatureWeights weights;

	private JTextArea input;
	private JTextPane output;
	
	private final String wikipedia = "http://en.wikipedia.org/wiki/";


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

		StringBuilder builder = new StringBuilder();
		
		System.out.println(results.size() + " sentences");
		for (Sentence s : results) {
			String[] ents = s.getEntities();
			String[] tokens = s.getTokens();
			if (ents == null) {
				System.out.println("Entities is null");
				continue;
			}
			
			for(int i = 0; i < tokens.length; i++) {
				if (ents[i] != "0") {
					builder.append("<a href=\""+wikipedia+ents[i]+"\">");
				}
				builder.append(tokens[i]);
				if (ents[i] != "0") {
					builder.append("</a>");
				}
				builder.append(" ");
			}
			builder.append("<br />");
			
		}
		output.setText(builder.toString());
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

		output = new JTextPane();
		output.setContentType("text/html");
		output.setEditable(false);
		output.setMinimumSize(new Dimension(100, 22));
		output.setPreferredSize(new Dimension(250, 22));
		output.setText("output");
		output.addHyperlinkListener(new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent event) {
				if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					try {
						browseTo(event.getURL().toURI());
					} catch (Exception ioe) {
						// Some warning to user
					}
				}
			}
		});
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
	
	private void browseTo(URI url) {
		if( !java.awt.Desktop.isDesktopSupported() ) {
            System.err.println( "Desktop is not supported (fatal)" );
            System.exit( 1 );
        }   
        java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
        if( !desktop.isSupported( java.awt.Desktop.Action.BROWSE ) ) {
            System.err.println( "Desktop doesn't support the browse action (fatal)" );
            System.exit( 1 );
        }
        
            try {
                desktop.browse( url );
            }
            catch ( Exception e ) {
                System.err.println( e.getMessage() );
            }
	}

}
