package byteware.setting.settings;

import byteware.setting.Setting;
import byteware.util.java.BooleanFunction;

import java.util.Arrays;
import java.util.List;

public class ListSetting extends Setting<String> {
	private final List<String> values;
	private int index;

	public ListSetting(String name, String value, String... values) {
		super(name, value);

		this.values = Arrays.asList(values);

		int tempIndex = this.values.indexOf(value);

		if (tempIndex < 0) {
			this.value = this.values.get(0);
			this.index = 0;
		} else {
			this.index = tempIndex;
			this.value = this.values.get(tempIndex);
		}
	}

	public ListSetting(
			String name,
			String value,
			BooleanFunction showIf,
			String... values
	) {
		super(name, value, showIf);

		this.values = Arrays.asList(values);
	}

	public void nextMode() {
		index++;

		if (index > values.size() - 1) {
			index = 0;
		}

		this.value = values.get(index);
	}

	public void previousMode() {
		index--;

		if (index < 0) {
			index = values.size() - 1;
		}

		this.value = values.get(index);
	}
}
