package com.xavier.basicPathfinderServer;

import java.util.HashMap;
import java.util.LinkedList;

public class Spellcasting {

	private CastingType type;
	private int classId;
	private String castingStat;
	private PathfinderCharacter character;
	private HashMap<Integer, Integer> spellsPerDay;
	private LinkedList<Spell> spellsKnown;
	private HashMap<Integer, LinkedList<Spell>> spellsPrepped;
	private HashMap<Integer, LinkedList<Spell>> spellsCast;
	
	public Spellcasting(int classId, CastingType type, String castingStat,
			PathfinderCharacter character) {
		this.classId = classId;
		this.type = type;
		this.castingStat = castingStat;
		this.character = character;
		spellsPerDay = new HashMap<>();
		spellsKnown = new LinkedList<>();
		spellsPrepped = new HashMap<>();
		spellsCast = new HashMap<>();
	}

	public void setSpellsPerDay(int level, int basePerDay) {
		spellsPerDay.put(level, basePerDay);
	}

	public int getSpellsPerDay(int level) {
		int baseSpellsForLevel = spellsPerDay.get(level);
		int castingMod = character.getAbilityMod(castingStat);
		int bonusSpellsPerDay = 0;
		while (level != 0 && castingMod >= level) {
			bonusSpellsPerDay++;
			castingMod -= 4;
		}
		return baseSpellsForLevel + bonusSpellsPerDay;
	}

	public void addSpellKnown(Spell spell) {
		spellsKnown.add(spell);
	}

	public void prepSpell(String spellName, int level) {
		Spell spell = getSpell(spellName);
		if (spell != null) {
			addSpellDCToSpell(level, spell);
			LinkedList<Spell> spellsOfLevel = spellsPrepped.get(level);
			if (spellsOfLevel == null) {
				spellsPrepped.put(level, new LinkedList<Spell>());
				spellsOfLevel = spellsPrepped.get(level);
			}
			spellsOfLevel.add(spell);
		}
	}
	
	//Note: The spell MUST be prepped
	public void castSpell(String spellName, int level) {
		Spell targetSpell = null;
		LinkedList<Spell> spellsOfLevel = spellsPrepped.get(level);
		for (Spell spell : spellsOfLevel) {
			if (spell.name.equals(spellName)) {
				targetSpell = spell;
				break;
			}
		}
		spellsOfLevel.remove(targetSpell);
		
		LinkedList<Spell> castSpellsOfLevel = spellsCast.get(level);
		if (castSpellsOfLevel == null) {
			spellsCast.put(level, new LinkedList<Spell>());
			castSpellsOfLevel = spellsCast.get(level);
		}
		castSpellsOfLevel.add(targetSpell);
	}

	private void addSpellDCToSpell(int level, Spell spell) {
		ensureSchoolSpecificCharacterStat(spell.school);
		Stat spellDC = new Stat("DC");
		if (spell.savingThrow.equals("none")) {
			spellDC.setBaseValue(-1);
		} else {
			spellDC.setBaseValue(10);
			Adjustment spellDCAdjustments = new Adjustment("DC adjustment", true);
			spellDCAdjustments.addEffect("DC", "Casting Stat", character.getAbility(castingStat));
			spellDCAdjustments.addEffect("DC", "Spell Level", level);
			spellDCAdjustments.addEffect("DC", spell.school, character.getStat(spell.school));
			spellDC.addAdjustment(spellDCAdjustments);
		}
		spell.addSpellDC(spellDC);
	}
	
	private void ensureSchoolSpecificCharacterStat(String spellSchool) {
		Stat schoolSpecificDCChange = character.getStat(spellSchool);
		if (schoolSpecificDCChange == null) {
			character.addStat(spellSchool);
		}
	}
	
	public Spell getSpell(String spellName) {
		for (Spell spell : spellsKnown) {
			if (spell.name.equals(spellName)) {
				return spell;
			}
		}
		return null;
	}

	public Integer getSpellDC(String spellName, int level) {
		Spell spell = getSpell(spellName);
		if(spell != null) {
			return spell.getSpellDCValue();
		}
		return -1;
	}

}
