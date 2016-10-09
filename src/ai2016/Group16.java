package ai2016;

import java.util.List;
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
	private Bid lastReceivedBid = null;
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

		if (isAlmostFinished)
			utilitySpace.setReservationValue(1.0);

		// Accept if Utility of lastReceivedBid is equal or greater than RV
		if (!isAlmostFinished && lastReceivedBid != null
				&& this.getUtility(lastReceivedBid) >= this.utilitySpace.getReservationValue()) {
			this.utilitySpace.setReservationValue(this.getUtility(lastReceivedBid));
			return new Accept(getPartyId(), lastReceivedBid);
		} else {
			lastOfferedBid = generateRandomWalkerBid();
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
			lastReceivedBid = ((Offer) action).getBid();
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
	
}
