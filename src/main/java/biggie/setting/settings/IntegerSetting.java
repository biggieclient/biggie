package biggie.setting.settings;

import biggie.setting.Setting;
import biggie.util.java.BooleanFunction;

public class IntegerSetting extends Setting<Integer> {
	public final int min, max, inc;

	public IntegerSetting(
			String name,
			int value,
			int min,
			int max,
			int inc
	) {
		super(name, value);

		this.min = min;
		this.max = max;
		this.inc = inc;
	}

	public IntegerSetting(
			String name,
			int value,
			int min,
			int max,
			int inc,
			BooleanFunction showIf
	) {
		super(name, value, showIf);

		this.min = min;
		this.max = max;
		this.inc = inc;
	}
}
