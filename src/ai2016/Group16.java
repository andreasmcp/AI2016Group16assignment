package ai2016;

import java.util.List;

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
	private Bid lastReceivedBid = null;
	private Bid BestReceivedBid = null;
	private Bid lastOfferedBid = null;

	@Override
	public void init(AbstractUtilitySpace utilSpace, Deadline dl, TimeLineInfo tl, long randomSeed, AgentID agentId) {
		
		super.init(utilSpace, dl, tl, randomSeed, agentId);

		System.out.println("Discount Factor is " + utilSpace.getDiscountFactor());
		System.out.println("Reservation Value is " + utilSpace.getReservationValueUndiscounted());

		// initialization of reservation value if it is not assigned in profile
		if (this.utilitySpace.getReservationValue() == 0.0){
			utilitySpace.setReservationValue(0.8);
		}
		
		offerHistory = new OfferHistory();
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
		boolean isAlmostFinished = this.isAlmostFinished();
		
		
//		if (isAlmostFinished)
//			utilitySpace.setReservationValue(1.0);

		// Accept if Utility of lastReceivedBid is equal or greater than RV
		if (!isAlmostFinished && lastReceivedBid != null
				&& this.getUtility(lastReceivedBid) >= this.utilitySpace.getReservationValue()) {
			this.utilitySpace.setReservationValue(this.getUtility(lastReceivedBid));
			return new Accept(getPartyId(), lastReceivedBid);
		} else {
			//lastOfferedBid = generateRandomWalkerBid();
			lastOfferedBid = BiddingStrategy(5);
			return new Offer(getPartyId(), lastOfferedBid);
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
			offerHistory.addOffer((Offer) action);
			lastReceivedBid = ((Offer) action).getBid();
			
			if (BestReceivedBid == null) {
				BestReceivedBid = lastReceivedBid;
			} else {
				if (this.getUtility(BestReceivedBid) < this.getUtility(lastReceivedBid)) {
					BestReceivedBid = lastReceivedBid;
				}
			}
		} else {
			this.utilitySpace.setReservationValue(this.getUtility(lastOfferedBid));
		}
	}

	@Override
	public String getDescription() {
		return "Party Group 16";
	}

	/**
	 * To get bid to offer, use random walker bid with minimum utility = reservation value
	 * If random bid doesn't reach reservation value within time constraint, then search for best random bid.
	 * If on the last round the bid still not reach reservation value, then compare with last offered bid
	 * then choose bid with the best utility
	 *
	 */
	public Bid generateRandomWalkerBid() {
		Bid result = null;
		Bid prevresult = null;
		boolean isAlmostFinished = this.isAlmostFinished();
		
		do {
			prevresult = result;
			result = generateRandomBid();
			if (this.getUtility(prevresult) > this.getUtility(result)){
				result = prevresult;
			}
			isAlmostFinished = this.isAlmostFinished();
			
		} while (this.getUtility(result) <= this.utilitySpace.getReservationValue() && !isAlmostFinished);

		//if reservation value is 1 because of deadline, but random bid hasn't found
		//assign random bid with last offered bid
		if (this.utilitySpace.getReservationValue() == 1.0){
			if (this.getUtility(result) < this.getUtility(this.lastOfferedBid) ){
				result = this.lastOfferedBid;}
		}
		return result;
	}

	/**
	 * To get bid to offer for second half of session
	 * We practice that bid will come from 1 to actual reservation value
	 * with iteration -0.05 and also put some logic for timeout 1/12 of the round time
	 * First option is to generate bid with current reservation value within time limit
	 * If the bid is not generated, then generate another random bid using current reservation value - 0.3 without time limit
	 */	
	
	public Bid generateConcederBid() {
		Bid result = null;
		Bid prevresult = null;
		boolean isTimeOut = this.isTimeOut();
		boolean isAlmostFinished = this.isAlmostFinished();
		double reservationValuetemp = 1.00;
		
		do {
			prevresult = result;
			result = generateRandomBid();
			if (this.getUtility(prevresult) > this.getUtility(result)){
				result = prevresult;
			}
			
		} while ((this.getUtility(result) >(reservationValuetemp - 0.05)) && !isTimeOut && !isAlmostFinished);
		
		//if reservationValue - 0.1 is lower than actual reservation value, then fill temp with actual RV
		if ((reservationValuetemp - 0.1) < this.utilitySpace.getReservationValue()){
			reservationValuetemp = this.utilitySpace.getReservationValue();
		}
		else{
			reservationValuetemp = reservationValuetemp - 0.1;
		}
		
		//if result is still null because timeout
		//lower the utility to utility - 0.3, and get any random bid >utility - 0.3
		if (result == null){
			do {
				prevresult = result;
				result = generateRandomBid();
				if (this.getUtility(prevresult) > this.getUtility(result)){
					result = prevresult;
				}
				
			} while ((this.getUtility(result) > (reservationValuetemp)) && !isTimeOut && !isAlmostFinished);

		}
		
		//if random generated bid is not good enough compared to last received bid,
		//then bid lastReceivedBid
		if (this.getUtility(BestReceivedBid) > this.getUtility(result)){
			result = this.BestReceivedBid;
		}
		
		//if result is still null
		// assign with lastOfferedBid if it is not null and just generate random bid if it is null.
		if (result == null){
			if (lastOfferedBid != null){
				result = this.lastOfferedBid;}
			else{
				result = generateRandomBid();
			}
			
		}
		
		utilitySpace.setReservationValue(reservationValuetemp);
		
		return result;
	}
	
	public Bid generateSecondHalfBid(int opponentType) {
		Bid result = null;
		
		switch(opponentType){
			case 1: result = null;
			case 2: result = generateRandomWalkerBid();
			case 3: result = null;
			case 4: result = null;
			case 5: result = generateConcederBid();
		}
		
		return result;
	}
	
	
	
	/**
	 * All negotiation has deadline, therefore need to know the deadline
	 * to determine action to bid another offer or accept
	 *
	 */
	public boolean isAlmostFinished() {
		boolean result = false;
		TimeLineInfo timeLineInfo = this.getTimeLine();

		//assign time limit 3/4 of deadline negotiation
		if (timeLineInfo != null && (timeLineInfo.getCurrentTime() >= timeLineInfo.getTotalTime()*3/4))
			result = true;

		return result;
	}
	
	public boolean isTimeOut() {
		boolean result = false;
		TimeLineInfo timeLineInfo = this.getTimeLine();
		double startTime = timeLineInfo.getTime();
		
		//assign time limit 2s of deadline negotiation
		if (timeLineInfo != null && timeLineInfo.getCurrentTime() >= (startTime + 2))
			result = true;

		return result;
	}
	
//	public Bid BiddingStrategy(int OpponentType, float OpponentBidUtility, BidHistory BidHist) {
	public Bid BiddingStrategy(int OpponentType) {
		Bid UpcomingBid = null;
		TimeLineInfo timeLineInfo = this.getTimeLine();
		
		//assign time limit 1/2 time
		if (timeLineInfo != null && (timeLineInfo.getCurrentTime() <= timeLineInfo.getTotalTime()*1/2)){
			UpcomingBid = generateConcederBid();
		}
		else{
			UpcomingBid = generateSecondHalfBid(5);
		}
		
		
		return UpcomingBid;
	}

}
