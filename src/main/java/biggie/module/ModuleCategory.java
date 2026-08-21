package biggie.module;

public enum ModuleCategory {
	COMBAT("Combat"),
	MISC("Misc"),
	MOVEMENT("Movement"),
	PLAYER("Player"),
	RENDER("Render");

	public final String categoryName;

	ModuleCategory(String categoryName) {
		this.categoryName = categoryName;
	}
}
