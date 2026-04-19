package maxboxx.blueprints.graphics.ui.screens;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

public class NumberInput extends EditBox {
	private int minValue = 0;
	private int maxValue = 1000;

	private Consumer<Integer> change = null;

	public NumberInput(Font font, int width, int height, Component narration) {
		super(font, width, height, narration);
	}

	public void setMinValue(int minValue) {
		this.minValue = minValue;
	}

	public void setMaxValue(int maxValue) {
		this.maxValue = maxValue;
	}

	public void onChange(Consumer<Integer> change) {
		this.change = change;
	}

	@Override
	public void setFocused(boolean focused) {
		boolean wasFocused = isFocused();

		super.setFocused(focused);

		if (wasFocused && !focused) {
			setIntValue(getIntValue());

			if (change != null) {
				change.accept(getIntValue());
			}
		}
	}

	@Override
	public void insertText(@NonNull String textToInsert) {
		String filtered = filterText(textToInsert);
		super.insertText(filtered);
	}

	@Override
	public void setValue(@NonNull String value) {
		super.setValue(filterText(value));
	}

	private String filterText(String input) {
		if (input == null || input.isEmpty()) return "";

		StringBuilder result = new StringBuilder();

		for (char c : input.toCharArray()) {
			if (Character.isDigit(c)) {
				result.append(c);
			}
		}
		return result.toString();
	}

	public void setIntValue(Integer value) {
		if (value < minValue) {
			value = minValue;
		}
		else if (value > maxValue) {
			value = maxValue;
		}

		setValue(value.toString());
	}

	public Integer getIntValue() {
		try {
			int n = Integer.parseInt(getValue());

			if (n < minValue) {
				return minValue;
			}
			else if (n > maxValue) {
				return maxValue;
			}
			else {
				return n;
			}
		} catch (NumberFormatException e) {
			return minValue;
		}
	}
}
