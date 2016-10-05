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

/**
 * This is your negotiation party.
 */
public class Group16 extends AbstractNegotiationParty {
	private Bid lastReceivedBid = null;
	private Bid lastOfferedBid = null;

	@Override
	public void init(AbstractUtilitySpace utilSpace, Deadline dl, TimeLineInfo tl, long randomSeed, AgentID agentId) {

		super.init(utilSpace, dl, tl, randomSeed, agentId);

		System.out.println("Discount Factor is " + utilSpace.getDiscountFactor());
		System.out.println("Reservation Value is " + utilSpace.getReservationValueUndiscounted());

		// if you need to initialize some variables, please initialize them
		// below here
		utilSpace.setReservationValue(0.8);
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

	public Bid generateRandomWalkerBid() {
		Bid result = null;

		do {
			result = generateRandomBid();
		} while (this.getUtility(result) <= this.utilitySpace.getReservationValue());

		return result;
	}

	public boolean isAlmostFinished() {
		boolean result = false;
		TimeLineInfo timeLineInfo = this.getTimeLine();

		if (timeLineInfo != null && (timeLineInfo.getCurrentTime() + 2 >= timeLineInfo.getTotalTime()))
			return true;

		return result;
	}
}
