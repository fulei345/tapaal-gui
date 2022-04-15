package dk.aau.cs.verification;

import dk.aau.cs.TCTL.visitors.HasDeadlockVisitor;
import dk.aau.cs.model.tapn.TAPNQuery;

public class QueryResult {
	private final boolean satisfied;
	private boolean approximationInconclusive = false;
	private final boolean discreteInclusion;
	private final TAPNQuery query;
	private final BoundednessAnalysisResult boundednessAnalysis;
	private double percent;

	public boolean isCTL = false;
	public QueryResult(boolean satisfied, BoundednessAnalysisResult boundednessAnalysis, TAPNQuery query, boolean discreteInclusion){
		this.satisfied = satisfied;
		this.boundednessAnalysis = boundednessAnalysis;
		this.query = query;
		this.discreteInclusion = discreteInclusion;
	}

	// SMC
	public QueryResult(boolean satisfied, BoundednessAnalysisResult boundednessAnalysis, TAPNQuery query, boolean discreteInclusion, double percent){
		this.satisfied = satisfied;
		this.boundednessAnalysis = boundednessAnalysis;
		this.query = query;
		this.discreteInclusion = discreteInclusion;
		this.percent = percent;
	}

	public double getPercent() {
		return percent;
	}
	
	public boolean isQuerySatisfied() {
		return satisfied;
	}
	
	public boolean isApproximationInconclusive() {
		return approximationInconclusive;
	}
	
	public void setApproximationInconclusive(boolean result) {
		approximationInconclusive = result;
	}
	
	public boolean isDiscreteIncludion() {
		return discreteInclusion;
	}
	
	public boolean hasDeadlock(){
		return new HasDeadlockVisitor().hasDeadLock(query.getProperty());
	}
	
	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		if(approximationInconclusive)
			buffer.append(getInconclusiveString());
		else {
			buffer.append("Property is ");
			buffer.append(satisfied ? "satisfied" : "not satisfied.");
			if(percent > 0.0){
				buffer.append("with " + percent + " probability");
			}
		}
		if(shouldAddExplanation())
			buffer.append(getExplanationString());
		return buffer.toString();
	}
	
	public QueryType queryType(){
		return query.queryType();
	}
	
	private boolean shouldAddExplanation() {
		return (queryType().equals(QueryType.EF) && !isQuerySatisfied()) 
		|| (queryType().equals(QueryType.EG)) // && !isQuerySatisfied()) 
		|| (queryType().equals(QueryType.AF)) // && isQuerySatisfied())
		|| (queryType().equals(QueryType.AG) && isQuerySatisfied())
		|| (hasDeadlock() && 
				(!isQuerySatisfied() && queryType().equals(QueryType.EF)) || 
				(isQuerySatisfied() && queryType().equals(QueryType.AG))
                || (hasDeadlock() && boundednessAnalysis.isUPPAAL()) );
	}
	
	protected String getExplanationString(){
		return boundednessAnalysis.toString();
	}
	
	protected String getInconclusiveString(){
		StringBuilder buffer = new StringBuilder();
		buffer.append("The result of the approximation was inconclusive.");
		return buffer.toString();
	}
	
	public TAPNQuery getQuery() {
		return query;
	}

	public BoundednessAnalysisResult boundednessAnalysis() {
		return boundednessAnalysis;
	}

}
