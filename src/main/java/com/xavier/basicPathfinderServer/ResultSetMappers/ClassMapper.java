package com.xavier.basicPathfinderServer.ResultSetMappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.xavier.basicPathfinderServer.CastingType;
import com.xavier.basicPathfinderServer.CharacterClass;

public class ClassMapper implements ResultSetMapper<Object> {

	@Override
	public List<CharacterClass> map(ResultSet resultSet) {
		List<CharacterClass> classes = new ArrayList<>();
		try {
			while (resultSet.next()) {
				int id = resultSet.getInt("ClassID");
				int level = resultSet.getInt("ClassLevel");
				int bab = resultSet.getInt("ClassBAB");
				int fort = resultSet.getInt("ClassFort");
				int ref = resultSet.getInt("ClassRef");
				int will = resultSet.getInt("ClassWill");
				int skillsPerLevel = resultSet.getInt("SkillsPerLevel");
				int hitDice = resultSet.getInt("HitDice");
				String name = resultSet.getString("ClassName");
				boolean spellcasting = resultSet.getString("Spellcasting").equals("t");
				CharacterClass characterClass = null;
				if (spellcasting) {
					CastingType type = CastingType.valueOf(resultSet.getString("SpellcastingType"));
					String ability = resultSet.getString("SpellcastingAbility");
					String spellsPerDayString = resultSet.getString("SpellsPerDay");
					Map<Integer, Integer> baseSpellsPerDay = SpellsPerDayParser.parse(spellsPerDayString);
					
					characterClass = new CharacterClass(id, level, bab, fort, ref, will, skillsPerLevel, hitDice, name, spellcasting, type, ability, baseSpellsPerDay);
				} else {
					characterClass = new CharacterClass(id, level, bab, fort, ref, will, skillsPerLevel, hitDice, name, false, CastingType.NONE, null, null);
				}
				classes.add(characterClass);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return classes;
	}

}