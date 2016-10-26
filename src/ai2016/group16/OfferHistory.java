package ai2016.group16;

import java.util.*;

import negotiator.Bid;
import negotiator.actions.Offer;

// Contains list of offer from agent(s)
// An offer contains the agent's ID and its Bid
// This class can be used to calculate utility of each agent for a Bid
public class OfferHistory {
	private List<Offer> listOffer;
	private List<String> listAgent;
	
	public OfferHistory(){
		listOffer = new ArrayList<Offer>();
		listAgent = new ArrayList<String>();
	}
	
	public OfferHistory(Offer offer){
		if (listOffer == null){
			listOffer = new ArrayList<Offer>();
			listAgent = new ArrayList<String>();
		}
		
		listOffer.add(offer);
		addAgent(offer.getAgent().toString());
	}
	
	public OfferHistory(List<Offer> listOffer){
		this.listOffer = listOffer;
		listAgent = new ArrayList<String>();
		
		for (Offer offer : listOffer)
			addAgent(offer.getAgent().toString());
	}
	
	public void addAgent(String agentId){
		if (!listAgent.contains(agentId))
			listAgent.add(agentId);
	}
	
	public List<String> getAgent() {
		return listAgent;
	}

	public void setAgent(List<String> listAgent) {
		this.listAgent = listAgent;
	}

	public void addOffer(Offer offer){
		if (listOffer != null)
			listOffer = new ArrayList<Offer>();
		
		listOffer.add(offer);
		addAgent(offer.getAgent().toString());
	}
	
	public List<Offer> getOfferHistory(){
		return listOffer;
	}
	
	public void setOfferHistory(List<Offer> listOffer){
		this.listOffer = listOffer;
	}
	
	public List<Offer> getOfferHistory(String agentId){
		List<Offer> listOffer = new ArrayList<Offer>();
		
		for (Offer offer : this.listOffer) {
			if (offer.getAgent().toString().equals(agentId))
				listOffer.add(offer);
		}
		
		return listOffer;
	}
	
	public double getUtility(String agentId, Bid bid){
		List<Offer> listOffer = getOfferHistory(agentId);
		
		// dummy
		return listOffer.size();
	}
}
