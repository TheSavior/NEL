package com.cse454.nel.scoring;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.cse454.nel.dataobjects.EntityMention;
import com.cse454.nel.dataobjects.Sentence;
import com.cse454.nel.document.AbstractDocument;
import com.cse454.nel.features.FeatureWeights;

public class EvaluationScorer implements AbstractScorer {

	private final Map<AbstractDocument, ScoreCard> documentScorecards;

	public EvaluationScorer() {
		documentScorecards = new HashMap<AbstractDocument, ScoreCard>();
	}

	@Override
	public void ScoreMentions(AbstractDocument doc, List<EntityMention> mentions) {

	}

	@Override
	public void Score(AbstractDocument document, FeatureWeights weights, Sentence sentence, String[] entities) {
		if (!documentScorecards.containsKey(document)) {
			ScoreCard scoreCard = new ScoreCard();
			documentScorecards.put(document, scoreCard);
		}
		ScoreCard scoreCard = documentScorecards.get(document);

		String[] gold = sentence.getGold();
		Set<String> goldEnts = new HashSet<String>();
		Set<String> correctEnts = new HashSet<String>();
		Set<String> incorrectEnts = new HashSet<String>();
		Set<String> badEntities = new HashSet<String>();
		Integer missedEntities = new Integer(0);
		if (gold.length == entities.length) {
			for (int i = 0; i < gold.length; i++) {
				if (i == 14) {
					System.out.println("adsf");
				}
				if (gold[i].equals("0")) {
					// check for one's we're incorrectly entity linking
					if (!entities[i].equals("0")) {
						badEntities.add(entities[i]);
					}
				} else {
					goldEnts.add(gold[i]);
					if (gold[i].equals(entities[i])) {
						correctEnts.add(entities[i]);
					} else if (entities[i].equals("0")) {
						missedEntities++;
					} else {
						incorrectEnts.add(entities[i]);
					}
				}
			}
		} else {
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
					if (goldEnts.contains(ent)) {
						correctEnts.add(ent);
					} else {
						incorrectEnts.add(ent);
					}
				}
			}
		}
		scoreCard.addGoldEnts(sentence, goldEnts);
		scoreCard.addCorrectEnt(sentence, correctEnts);
		scoreCard.addIncorrectEnt(sentence, incorrectEnts);
		scoreCard.addBadEnts(sentence, badEntities);
		scoreCard.addMissedEnts(sentence, missedEntities);
	}

	public double getTotalCorrect() {
		int num = 0;
		for (ScoreCard card : documentScorecards.values()) {
			num+=card.getTotalCorrect();
		}
		return num;
	}

	public double getTotalGold() {
		int num = 0;
		for (ScoreCard card : documentScorecards.values()) {
			num+=card.getTotalGold();
		}
		return num;
	}

	public double getTotalMissed() {
		int num = 0;
		for (ScoreCard card : documentScorecards.values()) {
			num += card.getTotalMissed();
		}
		return num;
	}

	public double getPrecisionScore() {
		return getTotalCorrect() / (getTotalGold() - getTotalMissed());
	}

	public double getOverallRation() {
		return getTotalCorrect() / getTotalGold();
	}

	public double getRecallScore() {
		return (getTotalGold() - getTotalMissed()) / getTotalGold();
	}

	// Scorecard per document evaluating how well it does on that document
	static class ScoreCard {

		private Map<Sentence, Set<String>> goldEnts;
		private Map<Sentence, Set<String>> correctEnts;
		private Map<Sentence, Set<String>> incorrectEnts;
		private Map<Sentence, Set<String>> allEnts;
		private Map<Sentence, Set<String>> badEnts;
		private Map<Sentence, Integer> missedEntities;

		public ScoreCard() {
			this.goldEnts = new HashMap<Sentence, Set<String>>();
			this.correctEnts = new HashMap<Sentence, Set<String>>();
			this.incorrectEnts = new HashMap<Sentence, Set<String>>();
			this.allEnts = new HashMap<Sentence, Set<String>>();
			this.badEnts = new HashMap<Sentence, Set<String>>();
			this.missedEntities = new HashMap<Sentence, Integer>();
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

		public void addBadEnts(Sentence sentence, Set<String> bad) {
			badEnts.put(sentence, bad);
		}

		public void addAllEnts(Sentence sentence, Set<String> allEnts) {
			this.allEnts.put(sentence, allEnts);
		}

		public void addMissedEnts(Sentence sentence, Integer i) {
			this.missedEntities.put(sentence, i);
		}

		public int getTotalMissed() {
			int num = 0;
			for (Entry<Sentence, Integer> entry : missedEntities.entrySet()) {
				num+= entry.getValue();
			}
			return num;
		}

		public int getTotalCorrect() {
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
