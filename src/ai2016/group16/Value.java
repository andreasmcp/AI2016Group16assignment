package ai2016.group16;

public class Value {
	private String name;
	private double evaluation;
	private int occurance;

	public Value(String name) {
		this.name = name;
		evaluation = 0;
		occurance = 0;
	}

	public Value(String name, double evaluation) {
		this.name = name;
		this.evaluation = evaluation;
		occurance = 0;
	}
	
	public Value(String name, double evaluation, int occurance) {
		this.name = name;
		this.evaluation = evaluation;
		this.occurance = occurance;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getEvaluation() {
		return evaluation;
	}

	public void setEvaluation(double evaluation) {
		this.evaluation = evaluation;
	}

	public int getOccurance() {
		return occurance;
	}

	public void setOccurance(int occurance) {
		this.occurance = occurance;
	}
	
	public void addOccurance(){
		occurance++;
	}
}
