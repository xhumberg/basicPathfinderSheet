package com.xavier.basicPathfinderServer.controllers;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.xavier.basicPathfinderServer.Adjustment;
import com.xavier.basicPathfinderServer.GoogleAuthenticationResponseJson;
import com.xavier.basicPathfinderServer.PathfinderCharacter;
import com.xavier.basicPathfinderServer.Prosopa;
import com.xavier.basicPathfinderServer.ResultSetMappers.AccessibleCharactersMapper;
import com.xavier.basicPathfinderServer.databaseLayer.AdjustmentDatabaseModifier;
import com.xavier.basicPathfinderServer.databaseLayer.CharacterFromDatabaseLoader;
import com.xavier.basicPathfinderServer.databaseLayer.DatabaseAccess;

@RestController
public class CharacterController {

	PathfinderCharacter defaultProsopa;
	Map<String, PathfinderCharacter> loadedCharacters; //TODO: Make this a cache
	Gson gson;
	
	private final String GET_CHARACTERS_FOR_USER_QUERY = "SELECT CharacterName, PathfinderCharacter.CharacterID FROM UserIDToEmail INNER JOIN UserAccess ON UserIDToEmail.UserID = UserAccess.UserID INNER JOIN PathfinderCharacter ON UserAccess.CharacterID = PathfinderCharacter.CharacterID WHERE UserIDToEmail.UserEmail = (?) OR UserAccess.UserID = -1;";
	
	@Autowired
	public CharacterController() {
		defaultProsopa = Prosopa.get();
		gson = new Gson();
		loadedCharacters = new HashMap<>();
	}
	
	@GetMapping("/character/{id}")
	public String getProsopa(@PathVariable String id, @RequestParam(required = false) String token) {
		if (id.equals("prosopa")) {
			System.out.println("Fetching default Prosopa");
			Gson gson = new Gson();
			return gson.toJson(defaultProsopa.convertToJson());
		} else if (token == null){
			PathfinderCharacter character = new PathfinderCharacter(-1, "Error: cannot access character without logging in", "");
			return gson.toJson(character.convertToJson());
		} else {
			System.out.println("Request to get character id " + id);
			PathfinderCharacter character = loadCharacterID(id, token);
			return gson.toJson(character.convertToJson());
		}
	}
	
	private PathfinderCharacter loadCharacterID(String id, String token) {
		GoogleAuthenticationResponseJson authenticatedGoogleToken = authenticateToken(token);
		System.out.println(authenticatedGoogleToken.getEmail() + " wants to load character " + id);
		
		if(!loadedCharacters.containsKey(id)) {
			loadedCharacters.put(id, CharacterFromDatabaseLoader.loadCharacter(id));
		} 
		return loadedCharacters.get(id);
	}

	@GetMapping("character/load")
	public String loadFromDatabase(@RequestParam String token) throws URISyntaxException, ClassNotFoundException {
		//TODO: query to make sure the user has access permission instead of just assuming they do
		GoogleAuthenticationResponseJson authenticatedGoogleToken = authenticateToken(token);
		System.out.println("Load characters for " + authenticatedGoogleToken.getEmail());
		
        DatabaseAccess<List<String>> db = new DatabaseAccess<>();
        List<String> response = db.executeSelectQuery(new AccessibleCharactersMapper(), GET_CHARACTERS_FOR_USER_QUERY, authenticatedGoogleToken.getEmail());
        db.close();
        if (response != null) {
        	return gson.toJson(response);
        }
        //If error
		return "[]";
	}

	private GoogleAuthenticationResponseJson authenticateToken(String tokenString) {
		//Authenticate Token
		RestTemplate restTemplate = new RestTemplate();
		String urlToHit = "https://oauth2.googleapis.com/tokeninfo?id_token=" + tokenString.replace("\"", "");
		ResponseEntity<String> response = restTemplate.getForEntity(urlToHit, String.class);
		GoogleAuthenticationResponseJson responseJson = gson.fromJson(response.getBody(), GoogleAuthenticationResponseJson.class);
		return responseJson;
	}
	
	@PutMapping("/character/{id}/toggle/{adjustmentName}") 
	public void toggleAdjustment(@PathVariable String id, @PathVariable String adjustmentName, @RequestParam(required = false) String token) {
		System.out.println("Time to toggle " + adjustmentName);
		if (id.equals("prosopa")) {
			defaultProsopa.toggleAdjustment(adjustmentName);
		} else {
			PathfinderCharacter character = loadCharacterID(id, token);
			if (character.isAdjustmentEnabled(adjustmentName)) {
				System.out.println("Disabling " + adjustmentName + " for character id " + id);
				Adjustment adjustment = character.toggleAdjustment(adjustmentName);
				AdjustmentDatabaseModifier.disableAdjustment(adjustment.getId(), Integer.parseInt(id));
				
			} else {
				System.out.println("Enabling " + adjustmentName + " for character id " + id);
				Adjustment adjustment = character.toggleAdjustment(adjustmentName);
				AdjustmentDatabaseModifier.enableAdjustment(adjustment.getId(), Integer.parseInt(id));
			}
		}
	}
	
	@PutMapping("/character/{id}/forceReload")
	public void forceCharacterReload(@PathVariable String id, @RequestParam String token) {
		authenticateToken(token);
		System.out.println("Forcing reload from database for character " + id);
		loadedCharacters.remove(id);
		loadCharacterID(id, token);
	}
	
}