package com.cse454.nel;

import java.util.List;

import com.cse454.nel.features.AllWordsHistogramFeatureGenerator;
import com.cse454.nel.features.FeatureWeights;
import com.cse454.nel.features.InLinkFeatureGenerator;
import com.cse454.nel.search.CrossWikiSearcher;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;    

public class Demo {
	private static final JPanel panel = new JPanel();

	public static void main(String[] args) throws Exception {

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
		/*
		DocPreProcessor preProcessor = new DocPreProcessor();
		DocumentProcessor processor = new DocumentProcessor(preProcessor);

		// Here's the text to be linked (value to come from gui)
		String text = "";

		// Here's necessary feature weight input (values will come from gui)
		int crossWikiWeight = 1;
		int inLinkWeight = 1;
		int histogramWeight = 1;

		FeatureWeights weights = new FeatureWeights();
		weights.setFeature(InLinkFeatureGenerator.FEATURE_STRING, inLinkWeight);
		weights.setFeature(CrossWikiSearcher.FEATURE_STRING, crossWikiWeight);
		weights.setFeature(AllWordsHistogramFeatureGenerator.FEATURE_NAME, histogramWeight);

		// Process the document
		List<Sentence> results = processor.ProcessDocument(weights, text);

		// now you could compare each  { token | entity } in a scroll bar on the gui or something
		 */
	}
	
	public static void Link() {
		
	}
	
	private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frmNamedEntityLinker = new JFrame("HelloWorldSwing");
        frmNamedEntityLinker.getContentPane().setName("Named Entity Linker");
        frmNamedEntityLinker.setTitle("Named Entity Linker");
        frmNamedEntityLinker.getContentPane().setPreferredSize(new Dimension(500, 350));
        frmNamedEntityLinker.getContentPane().setMinimumSize(new Dimension(500, 350));
        frmNamedEntityLinker.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Display the window.
        frmNamedEntityLinker.pack();
                frmNamedEntityLinker.getContentPane().add(panel, BorderLayout.NORTH);
                panel.setLayout(new BorderLayout(0, 0));
                
                JSplitPane splitPane = new JSplitPane();
                splitPane.setPreferredSize(new Dimension(500, 300));
                splitPane.setMinimumSize(new Dimension(300, 150));
                splitPane.setResizeWeight(0.5);
                panel.add(splitPane, BorderLayout.CENTER);
                
                JTextArea input = new JTextArea();
                input.setMinimumSize(new Dimension(100, 22));
                input.setPreferredSize(new Dimension(250, 22));
                input.setText("input");
                splitPane.setLeftComponent(input);
                
                JTextArea output = new JTextArea();
                output.setEditable(false);
                output.setMinimumSize(new Dimension(100, 22));
                output.setPreferredSize(new Dimension(250, 22));
                output.setText("output");
                splitPane.setRightComponent(output);
                
                JButton btnNewButton = new JButton("Link!");
                btnNewButton.addActionListener(new ActionListener() {
                	public void actionPerformed(ActionEvent e) {
                		Link();
                	}
                });
                panel.add(btnNewButton, BorderLayout.SOUTH);
        frmNamedEntityLinker.setVisible(true);
    }
}