package ai2016.group16;

import java.util.*;
import negotiator.actions.Offer;

// Contains list of offer from agent(s)
// An offer contains the agent's ID and its Bid
// This class can be used to calculate utility of each agent for a Bid
public class OfferHistory {
	private List<Offer> listOffer;

	public OfferHistory() {
		listOffer = new ArrayList<Offer>();
	}

	public OfferHistory(Offer offer) {
			listOffer = new ArrayList<Offer>();

		listOffer.add(offer);
	}

	public OfferHistory(List<Offer> listOffer) {
		this.listOffer = listOffer;
	}

	public void addOffer(Offer offer) {
		if (listOffer != null)
			listOffer = new ArrayList<Offer>();

		listOffer.add(offer);
	}

	public List<Offer> getOfferHistory() {
		return listOffer;
	}

	public void setOfferHistory(List<Offer> listOffer) {
		this.listOffer = listOffer;
	}

	public List<Offer> getOfferHistory(String agentId) {
		List<Offer> listOffer = new ArrayList<Offer>();

		for (Offer offer : this.listOffer) {
			if (offer.getAgent().toString().equals(agentId))
				listOffer.add(offer);
		}

		return listOffer;
	}
}
