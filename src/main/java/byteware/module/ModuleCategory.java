package byteware.module;

public enum ModuleCategory {
	COMBAT("Combat"),
	MISC("Misc"),
	MOVEMENT("Movement"),
	RENDER("Render");

	public final String categoryName;

	ModuleCategory(String categoryName) {
		this.categoryName = categoryName;
	}
}
