package byteware.setting.settings;

import byteware.setting.Setting;
import byteware.util.java.BooleanFunction;

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
