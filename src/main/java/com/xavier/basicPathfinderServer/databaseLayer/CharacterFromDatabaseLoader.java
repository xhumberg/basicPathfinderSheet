package com.xavier.basicPathfinderServer.databaseLayer;

import java.util.List;
import java.util.Map;

import com.xavier.basicPathfinderServer.Adjustment;
import com.xavier.basicPathfinderServer.CharacterClass;
import com.xavier.basicPathfinderServer.ClassFeature;
import com.xavier.basicPathfinderServer.Feat;
import com.xavier.basicPathfinderServer.Item;
import com.xavier.basicPathfinderServer.PathfinderCharacter;
import com.xavier.basicPathfinderServer.Spell;
import com.xavier.basicPathfinderServer.TrackedResource;
import com.xavier.basicPathfinderServer.ResultSetMappers.AllowedAdjustmentsMapper;
import com.xavier.basicPathfinderServer.ResultSetMappers.CharacterHealthInterimMapper;
import com.xavier.basicPathfinderServer.ResultSetMappers.CharacterWealthInterimMapper;
import com.xavier.basicPathfinderServer.ResultSetMappers.ClassFeatureMapper;
import com.xavier.basicPathfinderServer.ResultSetMappers.ClassMapper;
import com.xavier.basicPathfinderServer.ResultSetMappers.ClassSkillMapper;
import com.xavier.basicPathfinderServer.ResultSetMappers.EnabledAdjustmentsMapper;
import com.xavier.basicPathfinderServer.ResultSetMappers.FeatMapper;
import com.xavier.basicPathfinderServer.ResultSetMappers.ItemMapper;
import com.xavier.basicPathfinderServer.ResultSetMappers.KnownSpellMapper;
import com.xavier.basicPathfinderServer.ResultSetMappers.MiscResourceMapper;
import com.xavier.basicPathfinderServer.ResultSetMappers.PathfinderCharacterMapper;
import com.xavier.basicPathfinderServer.ResultSetMappers.SkillRanksMapper;
import com.xavier.basicPathfinderServer.ResultSetMappers.SpellInterimMapper;
import com.xavier.basicPathfinderServer.ResultSetMappers.interimObjects.CharacterHealthInterim;
import com.xavier.basicPathfinderServer.ResultSetMappers.interimObjects.CharacterWealthInterim;
import com.xavier.basicPathfinderServer.ResultSetMappers.interimObjects.SpellNameLevelAndClassInterim;

public class CharacterFromDatabaseLoader {

	private final static String GET_CHARACTER_QUERY = "select * from PathfinderCharacter where CharacterID = ?";
	private final static String GET_ALLOWED_ADJUSTMENTS_QUERY = "select AdjustmentName, AdjustmentEffect from AllowedAdjustments inner join StandardAdjustments on AllowedAdjustments.AdjustmentID = StandardAdjustments.AdjustmentID where CharacterID = ?";
	private final static String GET_ENABLED_ADJUSTMENTS_QUERY = "select AdjustmentName from EnabledAdjustments inner join StandardAdjustments on EnabledAdjustments.AdjustmentID = StandardAdjustments.AdjustmentID where CharacterID = ?";
	private final static String GET_CLASSES_FOR_CHARACTER = "select * from Classes inner join CharacterClasses on Classes.ClassID = CharacterClasses.ClassID where CharacterClasses.CharacterID = ?";
	private final static String GET_SKILL_RANKS = "select * from SkillRanks where CharacterID = ?";
	private final static String GET_CLASS_SKILLS = "select * from ClassSkills where CharacterID = ?";
	private final static String GET_KNOWN_SPELLS = "select * from SpellsKnown inner join Spells on SpellsKnown.SpellID = Spells.SpellID where CharacterID = ?";
	private final static String GET_PREPPED_SPELLS_QUERY = "select ClassID, SpellsPrepped.SpellLevel, SpellName from SpellsPrepped inner join Spells on SpellsPrepped.SpellID = Spells.SpellID where CharacterID = ?";
	private final static String GET_SPELLS_CAST = "select SpellName, SpellsCast.SpellLevel, ClassID from SpellsCast inner join Spells on Spells.SpellID = SpellsCast.SpellID where SpellsCast.CharacterID = ?";
	private final static String GET_EQUIPMENT = "select * from Equipment inner join Items on Items.ItemID = Equipment.ItemID left join TrackedResources on TrackedResources.ResourceID = Equipment.TrackedResourceID where CharacterID = ?";
	private final static String GET_FEATS = "select * from Feats inner join TakenFeats on Feats.FeatID = TakenFeats.FeatID where CharacterID = ?";
	private final static String GET_CLASS_FEATURES = "select * from CharacterClassFeatures inner join ClassFeatures on CharacterClassFeatures.FeatureID = ClassFeatures.FeatureID left join TrackedResources on CharacterClassFeatures.TrackedResourceID = TrackedResources.ResourceID where CharacterID = ?";
	private final static String GET_MISC_TRACKED_RESOURCES = "select * from CharactersTrackedResources inner join TrackedResources on CharactersTrackedResources.TrackedResourceId = TrackedResources.ResourceID where CharacterID = ?";
	private final static String GET_CHARACTER_WEALTH = "select * from CharacterWealth where CharacterID = ?";
	private final static String GET_CHARACTER_HEALTH = "select * from CharacterHP where CharacterID = ?";
	
	
	@SuppressWarnings("unchecked")
	public static PathfinderCharacter loadCharacter(String idString) {
		int id = Integer.parseInt(idString);
		DatabaseAccess<Object> db = new DatabaseAccess<>();
		PathfinderCharacter character = (PathfinderCharacter)db.executeSelectQuery(new PathfinderCharacterMapper(), GET_CHARACTER_QUERY, id);
		
		if (character != null) {
			List<Adjustment> allowedAdjustments = (List<Adjustment>)db.executeSelectQuery(new AllowedAdjustmentsMapper(), GET_ALLOWED_ADJUSTMENTS_QUERY, id);
			character.setAllowedAdjustments(allowedAdjustments);
			
			List<String> enabledAdjustments = (List<String>)db.executeSelectQuery(new EnabledAdjustmentsMapper(), GET_ENABLED_ADJUSTMENTS_QUERY, id);
			character.toggleAdjustments(enabledAdjustments);
			
			List<CharacterClass> classes = (List<CharacterClass>)db.executeSelectQuery(new ClassMapper(), GET_CLASSES_FOR_CHARACTER, id);
			character.addClasses(classes);
			
			List<String> classSkills = (List<String>)db.executeSelectQuery(new ClassSkillMapper(), GET_CLASS_SKILLS, id);
			for (String classSkill : classSkills) {
				character.setClassSkill(classSkill);
			}
			
			Map<String, Integer> skillRanks = (Map<String, Integer>)db.executeSelectQuery(new SkillRanksMapper(), GET_SKILL_RANKS, id);
			for (String skill : skillRanks.keySet()) {
				character.setSkillRanks(skillRanks.get(skill), skill);
			}
			
			List<Spell> knownSpells = (List<Spell>)db.executeSelectQuery(new KnownSpellMapper(), GET_KNOWN_SPELLS, id);
			for (Spell knownSpell : knownSpells) {
				character.giveSpellKnown(knownSpell.getClassId(), knownSpell);
			}
			
			List<SpellNameLevelAndClassInterim> preppedSpells = (List<SpellNameLevelAndClassInterim>)db.executeSelectQuery(new SpellInterimMapper(), GET_PREPPED_SPELLS_QUERY, id);
			for (SpellNameLevelAndClassInterim preppedSpell : preppedSpells) {
				character.prepSpell(preppedSpell.getClassId(), preppedSpell.getSpellName(), preppedSpell.getLevel());
			}
			
			List<SpellNameLevelAndClassInterim> castSpells = (List<SpellNameLevelAndClassInterim>)db.executeSelectQuery(new SpellInterimMapper(), GET_SPELLS_CAST, id);
			for (SpellNameLevelAndClassInterim castSpell : castSpells) {
				character.castSpell(castSpell.getClassId(), castSpell.getSpellName(), castSpell.getLevel());
			}
			
			List<Item> items = (List<Item>)db.executeSelectQuery(new ItemMapper(), GET_EQUIPMENT, id);
			for (Item item : items) {
				if (item.isEquipped()) {
					character.equip(item);
				} else {
					character.giveItem(item);
				}
			}
			
			List<Feat> feats = (List<Feat>)db.executeSelectQuery(new FeatMapper(), GET_FEATS, id);
			for (Feat feat : feats) {
				character.giveFeat(feat);
			}
			
			List<ClassFeature> features = (List<ClassFeature>)db.executeSelectQuery(new ClassFeatureMapper(), GET_CLASS_FEATURES, id);
			for (ClassFeature feature : features) {
				character.giveClassFeature(feature);
			}
			
			List<TrackedResource> miscTrackedResources = (List<TrackedResource>)db.executeSelectQuery(new MiscResourceMapper(), GET_MISC_TRACKED_RESOURCES, id);	
			for (TrackedResource resource : miscTrackedResources) {
				character.giveMiscTrackedResource(resource);
			}
			
			CharacterWealthInterim characterWealth = (CharacterWealthInterim)db.executeSelectQuery(new CharacterWealthInterimMapper(), GET_CHARACTER_WEALTH, id);
			character.setTotalEarnedGold(characterWealth.getEarnedGold());
			character.setSpentGold(characterWealth.getSpentGold());
			
			CharacterHealthInterim characterHealth = (CharacterHealthInterim)db.executeSelectQuery(new CharacterHealthInterimMapper(), GET_CHARACTER_HEALTH, id);
			character.setFavoredClassBonusHP(characterHealth.getFavoredClassBonusHp());
			character.takeDamage(characterHealth.getDamageTaken());
			
			db.close();
			return character;
		}
		db.close();
		return new PathfinderCharacter("Error: Couldn't load character", "https://www.aautomate.com/images/easyblog_shared/November_2018/11-12-18/human_error_stop_400.png");
	}
}