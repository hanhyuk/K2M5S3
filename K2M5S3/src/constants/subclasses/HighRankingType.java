package constants.subclasses;

public enum HighRankingType {

	FirstAdvance, SecondAdvance, ThirdAdvance, ForthAdvance;

	public int getType() {
		return ordinal();
	}
}
