package ai2016.group16;

import java.util.*;

import negotiator.Bid;
import negotiator.actions.Offer;
import negotiator.xml.SimpleElement;

public class Agent {
	private String id;
	private List<Offer> offers;
	private List<Issue> issues;

	public Agent(String id) {
		this.id = id;
		offers = new ArrayList<Offer>();
		issues = new ArrayList<Issue>();
	}

	public Agent(String id, Offer offer) {
		this.id = id;
		offers = new ArrayList<Offer>();
		issues = new ArrayList<Issue>();
		List<negotiator.issue.Issue> _issues = offer.getBid().getIssues();
		double initWeight = ((double)1/_issues.size());

		for (negotiator.issue.Issue issue : _issues) {
			Issue newIssue = new Issue(issue.getName(), issue.getNumber(), initWeight);

			for (SimpleElement simpleElement : issue.toXML().getChildElementsAsList())
				newIssue.addValue(new Value(simpleElement.getAttribute("value")));

			issues.add(newIssue);
		}

		addOffer(offer);
	}

	public Agent(String id, List<Offer> offers) {
		this.id = id;
		issues = new ArrayList<Issue>();

		for (negotiator.issue.Issue issue : offers.get(0).getBid().getIssues()) {
			Issue newIssue = new Issue(issue.getName(), issue.getNumber());

			for (SimpleElement simpleElement : issue.toXML().getChildElementsAsList())
				newIssue.addValue(new Value(simpleElement.getAttribute("value")));

			issues.add(newIssue);
		}

		for (Offer offer : offers)
			addOffer(offer);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<Offer> getOffers() {
		return offers;
	}

	public void setOffers(List<Offer> offers) {
		this.offers = offers;
	}

	public void addOffer(Offer offer) {
		offers.add(offer);
		updateIssue(offer.getBid().getValues());
	}

	public List<Issue> getIssues() {
		return issues;
	}

	public void setIssues(List<Issue> issues) {
		this.issues = issues;
	}

	public Issue getIssue(int number) {
		Issue issue = null;

		for (Issue _issue : issues)
			if (_issue.getNumber() == number)
				return _issue;

		return issue;
	}

	public void updateIssue(Map<Integer, negotiator.issue.Value> issuesValue) {
		for (Map.Entry<Integer, negotiator.issue.Value> issue : issuesValue.entrySet()) {
			for (Issue _issue : issues)
				if (_issue.getNumber() == issue.getKey()) {
					for (Value value : _issue.getValues()) {
						if (value.getName().equals(issue.getValue().toString())) {
							value.addOccurance();
							break;
						}
					}

					if (_issue.getLastValue().equals(issue.getValue().toString()))
						_issue.setWeight(_issue.getWeight() + 0.1);

					_issue.setLastValue(issue.getValue().toString());
					_issue.UpdateEvaluationValue();

					break;
				}
		}

		normalizeWeight();
	}

	public void normalizeWeight() {
		double total = 0;

		for (Issue issue : issues)
			total += issue.getWeight();

		for (Issue issue : issues)
			issue.setWeight((issue.getWeight() / total));
	}

	public double getUtility(Bid bid) {
		double result = 0;

		for (Map.Entry<Integer, negotiator.issue.Value> issue : bid.getValues().entrySet()) {
			Issue _issue = getIssue(issue.getKey());

			if (_issue != null) {
				Value value = _issue.getValue(issue.getValue().toString());

				if (value != null)
					result = result + (_issue.getWeight() * value.getEvaluation());
			}
		}

		return result;
	}
}
