package com.company.enroller.controllers;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.company.enroller.model.Participant;
import com.company.enroller.persistence.ParticipantService;

@RestController
@RequestMapping("/participants")
public class ParticipantRestController {

	@Autowired
	ParticipantService participantService;

	@RequestMapping(value = "", method = RequestMethod.GET)
	public ResponseEntity<?> getParticipants(
			@RequestParam(value = "sortBy", defaultValue = "login") String sortBy,
			@RequestParam(value = "sortOrder", defaultValue = "ASC") String sortOrder,
			@RequestParam(value = "key", required = false) String key) {
		Collection<Participant> participants = participantService.getAllFilteredAndSorted(sortBy, sortOrder, key);
		return new ResponseEntity<Collection<Participant>>(participants, HttpStatus.OK);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public ResponseEntity<?> getParticipant(@PathVariable("id") String login) {
		Participant participant = participantService.findByLogin(login);
		if (participant == null) {
			return new ResponseEntity(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<Participant>(participant, HttpStatus.OK);
	}

	@RequestMapping(value = "", method = RequestMethod.POST)
	public ResponseEntity<?> registerParticipant(@RequestBody Participant participant) {
		Participant foundParticipant = participantService.findByLogin(participant.getLogin());
		if (foundParticipant != null) {
			return new ResponseEntity("Unable to create. A participant with login " + participant.getLogin() + " already exist.", HttpStatus.CONFLICT);
		}
		participantService.add(participant);
		return new ResponseEntity<Participant>(participant, HttpStatus.CREATED);
	}

	@RequestMapping(value = "{id}", method = RequestMethod.DELETE)
	public ResponseEntity<?> deleteParticipant(@PathVariable("id") String login) {
		Participant participant = participantService.findByLogin(login);
		participantService.delete(participant);
		return new ResponseEntity<Participant>(participant, HttpStatus.OK);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.PUT)
	public ResponseEntity<?> updateParticipant(@PathVariable("id") String login, @RequestBody Participant updatedParticipant) {
		Participant existingParticipant = participantService.findByLogin(login);

		if (existingParticipant == null) {
			return new ResponseEntity("Participant with login " + login + " not found.", HttpStatus.NOT_FOUND);
		}

		existingParticipant.setLogin(updatedParticipant.getLogin());
		existingParticipant.setPassword(updatedParticipant.getPassword());

		return new ResponseEntity<Participant>(existingParticipant, HttpStatus.OK);
	}
}