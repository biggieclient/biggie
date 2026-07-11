package biggie.ui.dataset.impl;

import biggie.setting.settings.IntegerSetting;
import biggie.ui.dataset.Slider;

public class IntSlider extends Slider {
	private final IntegerSetting setting;

	public IntSlider(IntegerSetting setting) {
		this.setting = setting;
	}

	@Override
	public double getInput() {
		return setting.value;
	}

	@Override
	public double getMin() {
		return setting.min;
	}

	@Override
	public double getMax() {
		return setting.max;
	}

	@Override
	public void setValue(double value) {
		setting.value = new Double(value).intValue();
	}

	@Override
	public void setValueString(String value) {
		try {
			setting.value = Integer.parseInt(value);
		} catch (Exception ignored) {
		}
	}

	@Override
	public String getName() {
		return setting.name.replace("-", " ");
	}

	@Override
	public String getValueString() {
		return setting.value.toString();
	}

	@Override
	public double getIncrement() {
		return setting.inc;
	}

	@Override
	public boolean isVisible() {
		return setting.showIf.get();
	}

	@Override
	public void stepping(boolean increment) {
		if (increment) {
			if (setting.value >= setting.max) return;

			setting.value = setting.value + 1;
		} else {
			if (setting.value <= setting.min) return;

			setting.value = setting.value - 1;
		}
	}
}
