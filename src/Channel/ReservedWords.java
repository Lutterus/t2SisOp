package Channel;

import java.util.ArrayList;

public class ReservedWords {
	private ArrayList<String> palavrasReservadas;

	public ReservedWords() {
		palavrasReservadas = new ArrayList<String>();
	}

	public void addWord(String word) {
		palavrasReservadas.add(word);
	}

	public boolean isReserved(String word) {
		for (String string : palavrasReservadas) {
			if (word.contains(string)) {
				return true;
			}
		}
		return false;
	}
}
