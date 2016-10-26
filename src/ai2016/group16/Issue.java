package ai2016.group16;

import java.util.ArrayList;
import java.util.List;

public class Issue {
	private String name;
	private int number;
	private double weight;
	private List<Value> values;
	private String lastValue;

	public Issue(String name, int number) {
		this.name = name;
		this.number = number;
		weight = 0.0;
		values = new ArrayList<Value>();
		lastValue = "";
	}

	public Issue(String name, int number, double weight) {
		this.name = name;
		this.number = number;
		this.weight = weight;
		values = new ArrayList<Value>();
		lastValue = "";
	}

	public Issue(String name, int number, List<Value> values) {
		this.name = name;
		this.number = number;
		weight = 0.0;
		this.values = values;
		lastValue = "";
	}

	public Issue(String name, int number, double weight, List<Value> values, String lastValue) {
		this.name = name;
		this.number = number;
		this.weight = weight;
		this.values = values;
		this.lastValue = lastValue;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public List<Value> getValues() {
		return values;
	}

	public void setValues(List<Value> values) {
		this.values = values;
	}

	public void addValue(Value value) {
		values.add(value);
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public String getLastValue() {
		return lastValue;
	}

	public void setLastValue(String lastValue) {
		this.lastValue = lastValue;
	}

	public void UpdateEvaluationValue() {
		int max = 0;

		for (Value value : values)
			if (value.getOccurance() > max)
				max = value.getOccurance();

		for (Value value : values)
			value.setEvaluation((double) (value.getOccurance() / max));
	}

	public Value getValue(String name) {
		Value value = null;

		for (Value _value : values)
			if (_value.getName().equals(name))
				return value;

		return value;
	}
}
