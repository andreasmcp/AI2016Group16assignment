package ai2016.group16;

import java.util.*;
import negotiator.Bid;
import negotiator.issue.Issue;

public class GeneralHelper {
	public static List<Issue> getIssues(Bid bid) {
		return bid.getIssues();
	}
}
