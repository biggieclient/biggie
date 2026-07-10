package byteware.ui.dataset.impl;

import byteware.setting.settings.DoubleSetting;
import byteware.ui.dataset.Slider;

public class DoubleSlider extends Slider {
	private final DoubleSetting setting;

	public DoubleSlider(DoubleSetting setting) {
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
		setting.value = value;
	}

	@Override
	public void setValueString(String value) {
		try {
			setting.value = Double.parseDouble(value);
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
			setting.value = Math.round(setting.value * 10 + 1) / 10.0;
		} else {
			if (setting.value <= setting.min) return;
			setting.value = Math.round(setting.value * 10 - 1) / 10.0;
		}
	}
}
