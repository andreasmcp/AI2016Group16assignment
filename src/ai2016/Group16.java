package ai2016;

import java.util.ArrayList;
import java.util.List;
import ai2016.group16.Agent;
import ai2016.group16.GeneralHelper;
import ai2016.group16.OfferHistory;
import negotiator.AgentID;

import negotiator.Bid;
import negotiator.Deadline;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.Offer;
import negotiator.parties.AbstractNegotiationParty;
import negotiator.session.TimeLineInfo;
import negotiator.utility.AbstractUtilitySpace;

public class Group16 extends AbstractNegotiationParty {
	private OfferHistory offerHistory;
	private List<Agent> agents;
	private Bid lastReceivedBid = null;
	private Bid BestReceivedBid = null;
	private Bid lastOfferedBid = null;
	private Bid ourBestBid = null;
	private Bid acceptedBid = null;
	private double OpUtility = 0;
	private List<BidTemp> bestBids;

	@Override
	public void init(AbstractUtilitySpace utilSpace, Deadline dl, TimeLineInfo tl, long randomSeed, AgentID agentId) {

		super.init(utilSpace, dl, tl, randomSeed, agentId);

		System.out.println("Discount Factor is " + utilSpace.getDiscountFactor());
		System.out.println("Reservation Value is " + utilSpace.getReservationValueUndiscounted());

		// initialization of reservation value if it is not assigned in profile
		if (this.utilitySpace.getReservationValue() == 0.0) {
			utilitySpace.setReservationValue(0.8);
		}

		offerHistory = new OfferHistory();
		agents = new ArrayList<Agent>();
		bestBids = new ArrayList<BidTemp>();
	}

	/**
	 * Each round this method gets called and ask you to accept or offer. The
	 * first party in the first round is a bit different, it can only propose an
	 * offer.
	 *
	 * @param validActions
	 *            Either a list containing both accept and offer or only offer.
	 * @return The chosen action.
	 */
	@Override
	public Action chooseAction(List<Class<? extends Action>> validActions) {
		// Check remaining time of negotiation, if almost finished, set RV = 1
		// boolean isAlmostFinished = this.isAlmostFinished();
		// boolean isDeadline = this.isDeadline();
		double LRBUtility = this.getUtility(lastReceivedBid);
		double BRBUtility = this.getUtility(BestReceivedBid);
		Bid UpcomingBid = null;
		boolean isDeadline = isDeadline();
		
		
		if(this.getUtility(lastReceivedBid) >= 0.8){
			this.acceptedBid = lastReceivedBid;
			return new Accept(getPartyId(), lastReceivedBid);
		}
		
		if(isDeadline){
			if(acceptedBid == null){
				if(this.getUtility(lastReceivedBid) - this.getUtility(lastOfferedBid) >= 0.1){
					this.acceptedBid = lastReceivedBid;
					return new Accept(getPartyId(), lastReceivedBid);
				}
			}else{
				if(this.getUtility(lastReceivedBid) - this.getUtility(acceptedBid) >= 0.05){
					this.acceptedBid = lastReceivedBid;
					return new Accept(getPartyId(), lastReceivedBid);
				}
			}
			
		}
		
		if (LRBUtility > OpUtility) { 
			if (LRBUtility < BRBUtility) {
				UpcomingBid = BestReceivedBid;
				lastOfferedBid = UpcomingBid;
				return new Offer(getPartyId(), UpcomingBid);
			} else {
				return new Accept(getPartyId(), lastReceivedBid);
			}
		} else {
			UpcomingBid = BiddingStrategy();
			
			if (this.getUtility(UpcomingBid) < this.getUtility(lastReceivedBid)) {
				this.acceptedBid = lastReceivedBid;
				return new Accept(getPartyId(), lastReceivedBid);
			} else {
				if (this.getUtility(lastReceivedBid) >= 0.75) {
					this.acceptedBid = lastReceivedBid;
					return new Accept(getPartyId(), lastReceivedBid);
				}
				lastOfferedBid = UpcomingBid;
				return new Offer(getPartyId(), UpcomingBid);
			}
		}

		
	}

	/**
	 * All offers proposed by the other parties will be received as a message.
	 * You can use this information to your advantage, for example to predict
	 * their utility.
	 *
	 * @param sender
	 *            The party that did the action. Can be null.
	 * @param action
	 *            The action that party did.
	 */
	@Override
	public void receiveMessage(AgentID sender, Action action) {

		super.receiveMessage(sender, action);
		if (action instanceof Offer) {
			addAgent(sender.toString(), (Offer) action);
			offerHistory.addOffer((Offer) action);
			lastReceivedBid = ((Offer) action).getBid();
		} else {
			this.utilitySpace.setReservationValue(this.getUtility(lastOfferedBid));
		}

		if (BestReceivedBid == null) {
			BestReceivedBid = lastReceivedBid;
			if (BestReceivedBid != null)
				bestBids.add(new BidTemp(this.getUtility(BestReceivedBid), BestReceivedBid));
		} else {
			if (this.getUtility(BestReceivedBid) < this.getUtility(lastReceivedBid)) {
				BestReceivedBid = lastReceivedBid;
				if (BestReceivedBid != null)
					bestBids.add(new BidTemp(this.getUtility(BestReceivedBid), BestReceivedBid));
			}
		}

		OpUtility = this.getUtility(sender, lastReceivedBid);
	}

	@Override
	public String getDescription() {
		return "Party Group 16";
	}

	/**
	 * To get bid to offer, use random walker bid with minimum utility =
	 * reservation value If random bid doesn't reach reservation value within
	 * time constraint, then search for best random bid. If on the last round
	 * the bid still not reach reservation value, then compare with last offered
	 * bid then choose bid with the best utility
	 *
	 */
	public Bid generateRandomWalkerBid() {
		Bid result = null;
		Bid prevresult = null;
		boolean isAlmostFinished = this.isAlmostFinished();
		boolean isTimeOut = this.isTimeOut();
		boolean isDeadline = this.isDeadline();

		if (this.utilitySpace.getReservationValue() == 1.00 && !isDeadline) {
			result = ourBestBid;
		} else {
			do {
				prevresult = result;
				result = generateRandomBid();
				if (this.getUtility(prevresult) > this.getUtility(result)) {
					result = prevresult;
				}
				isAlmostFinished = this.isAlmostFinished();

			} while (this.getUtility(result) <= this.utilitySpace.getReservationValue() && !isAlmostFinished
					&& !isTimeOut);

			if (this.getUtility(result) > this.getUtility(ourBestBid)) {
				ourBestBid = result;
			}

			if (result == null) {
				if (lastOfferedBid == null) {
					result = generateRandomBid();
				} else {
					result = lastOfferedBid;
				}
			} else {
				if (lastOfferedBid != null) {
					if (this.getUtility(result) < this.getUtility(this.lastOfferedBid)) {
						result = this.lastOfferedBid;
					}
				}
			}
		}

		return result;
	}

	/**
	 * To get bid to offer for second half of session We practice that bid will
	 * come from 1 to actual reservation value with iteration -0.05 and also put
	 * some logic for timeout 1/12 of the round time First option is to generate
	 * bid with current reservation value within time limit If the bid is not
	 * generated, then generate another random bid using current reservation
	 * value - 0.3 without time limit
	 */

	public Bid generateConcederBid() {
		Bid result = null;
		Bid prevresult = null;
		boolean isTimeOut = this.isTimeOut();
		boolean isDeadline = this.isDeadline();
		double avgUtility = 0.00;
		double lastOfferedBidUtility = this.getUtility(lastOfferedBid);

		if (lastOfferedBidUtility == 1.00) {
			// result = lastOfferedBid;
			this.utilitySpace.setReservationValue(lastOfferedBidUtility - 0.1);
			// return result;
		} else {
			this.utilitySpace.setReservationValue(lastOfferedBidUtility - 0.05);
		}

		if(isDeadline){
			do {
				prevresult = result;
				result = generateRandomBid();
				if (this.getUtility(prevresult) > this.getUtility(result)) {
					result = prevresult;
				}
			} while (this.getUtility(result) <= this.getUtility(BestReceivedBid) && !isTimeOut);
		}
		
		do {
			prevresult = result;
			result = generateRandomBid();
			if (this.getUtility(prevresult) > this.getUtility(result)) {
				result = prevresult;
			}
		} while ((this.getUtility(result) <= this.getUtility(BestReceivedBid)) && !isDeadline);

		// if result is still null because timeout
		// search for bid which in average utility
		if (result == null) {
			avgUtility = (this.getUtility(ourBestBid) + OpUtility) / 2;
			do {
				prevresult = result;
				result = generateRandomBid();
				if (this.getUtility(prevresult) > this.getUtility(result)) {
					result = prevresult;
				}

			} while ((this.getUtility(result) <= avgUtility) && !isDeadline);

		}

		// if result is still null, bid with BestReceivedBid
		if (result == null) {
			result = BestReceivedBid;
		}
		// if random generated bid is not good enough compared to last received bid,
		// then bid lastReceivedBid
		if (this.getUtility(BestReceivedBid) >= this.getUtility(result)) {
			result = this.BestReceivedBid;
			bestBids.add(new BidTemp(this.getUtility(BestReceivedBid), result));
		}

		// if result is still null
		// assign with lastOfferedBid if it is not null and just generate random
		// bid if it is null.
		if (result == null) {
			if (lastOfferedBid != null) {
				result = bestBids.get(GeneralHelper.generateRandom(0, bestBids.size() - 1)).bid;
			} else {
				result = generateRandomBid();
			}

		}

		if (this.getUtility(result) > this.getUtility(ourBestBid)) {
			ourBestBid = result;
		}
		
		return result;
	}

	public Bid generateSecondHalfBid(int opponentType) {
		Bid result = null;

		return result;
	}

	/**
	 * All negotiation has deadline, therefore need to know the deadline to
	 * determine action to bid another offer or accept
	 *
	 */
	public boolean isAlmostFinished() {
		boolean result = false;
		TimeLineInfo timeLineInfo = this.getTimeLine();

		// assign time limit 3/4 of deadline negotiation
		if (timeLineInfo != null && (timeLineInfo.getCurrentTime() >= timeLineInfo.getTotalTime() * 0.75))
			result = true;

		return result;
	}

	public boolean isTimeOut() {
		boolean result = false;
		TimeLineInfo timeLineInfo = this.getTimeLine();
		double startTime = timeLineInfo.getTime();

		// assign time limit 2s of deadline negotiation
		if (timeLineInfo != null && timeLineInfo.getCurrentTime() >= (startTime + 2.00))
			result = true;

		return result;
	}

	public boolean isDeadline() {
		boolean result = false;
		TimeLineInfo timeLineInfo = this.getTimeLine();

		// assign time limit 11/12 of deadline negotiation
		if (timeLineInfo != null && (timeLineInfo.getCurrentTime() >= timeLineInfo.getTotalTime() - 0.50))
			result = true;

		return result;
	}

	public Bid BiddingStrategy() {
		Bid UpcomingBid = null;
		TimeLineInfo timeLineInfo = this.getTimeLine();
		double currentTime = timeLineInfo.getCurrentTime();
		double halfTime = timeLineInfo.getTotalTime() * 0.5;
		
		// assign time limit 1/2 time
		if (currentTime <= halfTime) {
			UpcomingBid = generateRandomWalkerBid();
		} else {
			UpcomingBid = generateConcederBid();
		}

		return UpcomingBid;
	}

	public void addAgent(String id, Offer offer) {
		boolean exist = false;
		Agent _agent = null;

		for (Agent agent : agents)
			if (agent.getId().equals(id)) {
				exist = true;
				_agent = agent;
			}

		if (!exist)
			agents.add(new Agent(id, offer));
		else
			_agent.addOffer(offer);
	}

	public Agent getAgent(String id) {
		for (Agent agent : agents)
			if (agent.getId().equals(id))
				return agent;

		return null;
	}

	public List<Double> getLastNUtilities(String id, int n) {
		List<Double> listUtil = new ArrayList<Double>();

		Agent agent = getAgent(id);

		if (agent != null && !agent.getOffers().isEmpty()) {
			if (n > agent.getOffers().size())
				n = agent.getOffers().size();

			for (int i = 0; i < n; i++)
				listUtil.add(agent.getUtility(agent.getOffers().get(i).getBid()));
		}

		return listUtil;
	}

	public double getUtility(AgentID agentId, Bid bid) {
		for (Agent agent : agents)
			if (agent.getId().equals(agentId.toString()))
				return agent.getUtility(bid);

		return 0;
	}
}

class BidTemp {
	public double utility;
	public Bid bid;

	public BidTemp(double utility, Bid bid) {
		this.utility = utility;
		this.bid = bid;
	}
}
