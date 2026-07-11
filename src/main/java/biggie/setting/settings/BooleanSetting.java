package biggie.setting.settings;

import biggie.setting.Setting;
import biggie.util.java.BooleanFunction;

public class BooleanSetting extends Setting<Boolean> {
	public BooleanSetting(String name, Boolean value) {
		super(name, value);
	}

	public BooleanSetting(
			String name,
			Boolean value,
			BooleanFunction showIf
	) {
		super(name, value, showIf);
	}
}
