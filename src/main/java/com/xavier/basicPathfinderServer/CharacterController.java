package com.xavier.basicPathfinderServer;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;

@RestController
public class CharacterController {

	@GetMapping("/character/prosopa")
	public String getProsopa() {
		PathfinderCharacter prosopa = new PathfinderCharacter("Prosopa", "https://media.discordapp.net/attachments/526680690218106891/731649744937418792/107571564_607483240193629_5533577863028070138_n.png?width=684&height=606");
		prosopa.setAbility("Strength", 7);
		prosopa.setAbility("Dexterity", 16);
		prosopa.setAbility("Constitution", 14);
		prosopa.setAbility("Intelligence", 21);
		prosopa.setAbility("Wisdom", 11);
		prosopa.setAbility("Charisma", 5);
		Gson gson = new Gson();
		return gson.toJson(prosopa.convertToJson());
	}
	
}