package com.cse454.nel.scoring;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.text.AbstractDocument;

import com.cse454.nel.Sentence;
import com.cse454.nel.features.FeatureWeights;

public class EvaluationScorer implements AbstractScorer {

	private final Map<AbstractDocument, ScoreCard> documentScorecards;

	public EvaluationScorer() {
		documentScorecards = new HashMap<AbstractDocument, ScoreCard>();
	}

	@Override
	public void Score(AbstractDocument document, FeatureWeights weights, Sentence sentence, String[] entities) {
		if (!documentScorecards.containsKey(document)) {
			ScoreCard scoreCard = new ScoreCard(weights);
			documentScorecards.put(document, scoreCard);
		}
		ScoreCard scoreCard = documentScorecards.get(document);

		String[] gold = sentence.getGold();
		Set<String> goldEnts = new HashSet<String>();
		Set<String> correctEnts = new HashSet<String>();
		Set<String> incorrectEnts = new HashSet<String>();
		Set<String> allEnts = new HashSet<String>();
		for (String g : gold) {
			if (!g.equals("0")) {
				goldEnts.add(g);
			}
		}
		if (goldEnts.size() == 0) { // no entities to score just return
			return;
		}
		scoreCard.addGoldEnts(sentence, goldEnts);
		for (String ent : entities) {
			if (!ent.equals("0")) {
				allEnts.add(ent);
				if (goldEnts.contains(ent)) {
					correctEnts.add(ent);
				} else {
					incorrectEnts.add(ent);
				}
			}
		}
		scoreCard.addAllEnts(sentence, allEnts);
		scoreCard.addCorrectEnt(sentence, correctEnts);
		scoreCard.addIncorrectEnt(sentence, incorrectEnts);

	}

	// Scorecard per document evaluating how well it does on that document
	static class ScoreCard {

		private FeatureWeights weights;
		private Map<Sentence, Set<String>> goldEnts;
		private Map<Sentence, Set<String>> correctEnts;
		private Map<Sentence, Set<String>> incorrectEnts;
		private Map<Sentence, Set<String>> allEnts;

		public ScoreCard(FeatureWeights weights) {
			this.weights = weights;
			this.goldEnts = new HashMap<Sentence, Set<String>>();
			this.correctEnts =  new HashMap<Sentence, Set<String>>();
			this.incorrectEnts =  new HashMap<Sentence, Set<String>>();
		}

		public void addGoldEnts(Sentence sentence, Set<String> gold) {
			goldEnts.put(sentence, gold);
		}

		public void addCorrectEnt(Sentence sentence, Set<String> correct) {
			correctEnts.put(sentence, correct);
		}

		public void addIncorrectEnt(Sentence sentence, Set<String> incorrect) {
			incorrectEnts.put(sentence, incorrect);
		}

		public void addAllEnts(Sentence sentence, Set<String> allEnts) {
			this.allEnts.put(sentence, allEnts);
		}

		public int getAmountCorrect() {
			int num = 0;
			for(Set<String> set : correctEnts.values()) {
				num += set.size();
			}
			return num;
		}

		public int getTotalGold() {
			int num = 0;
			for(Set<String> set : goldEnts.values()) {
				num += set.size();
			}
			return num;
		}

		public double getTotalCorrectRatio() {
			Set<Sentence> sentences = goldEnts.keySet();
			double numerator = 0;
			double denominator = 0;
			for (Sentence sentence : sentences) {
				denominator += goldEnts.get(sentence).size();
				numerator += goldEnts.get(sentence).size();
			}
			return numerator / denominator;
		}

	}
}
