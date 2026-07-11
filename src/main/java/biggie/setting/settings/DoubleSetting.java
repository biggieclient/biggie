package biggie.setting.settings;

import biggie.setting.Setting;
import biggie.util.java.BooleanFunction;

public class DoubleSetting extends Setting<Double> {
	public final double min, max, inc;

	public DoubleSetting(
			String name,
			double value,
			double min,
			double max,
			double inc
	) {
		super(name, value);

		this.min = min;
		this.max = max;
		this.inc = inc;
	}

	public DoubleSetting(
			String name,
			double value,
			double min,
			double max,
			double inc,
			BooleanFunction showIf
	) {
		super(name, value, showIf);

		this.min = min;
		this.max = max;
		this.inc = inc;
	}
}
