package biggie.setting;

import biggie.util.java.BooleanFunction;

public class Setting<T> {
	public final String name;
	public T value;
	public BooleanFunction showIf;

	public Setting(String name, T value) {
		this.name = name;
		this.value = value;
		this.showIf = () -> true;
	}

	public Setting(String name, T value, BooleanFunction showIf) {
		this.name = name;
		this.value = value;
		this.showIf = showIf;
	}
}
