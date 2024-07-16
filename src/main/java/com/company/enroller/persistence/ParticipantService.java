package com.company.enroller.persistence;

import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.company.enroller.model.Participant;

import java.util.ArrayList;
import java.util.Collection;

@Service("participantService")
public class ParticipantService implements UserDetailsService {

	private final DatabaseConnector connector;

	@Autowired
	private PasswordEncoder passwordEncoder;

	public ParticipantService() {
		this.connector = DatabaseConnector.getInstance();
	}

	public Collection<Participant> getAll() {
		String hql = "FROM Participant";
		Query<Participant> query = connector.getSession().createQuery(hql, Participant.class);
		return query.list();
	}

	public Collection<Participant> getAllSorted(String sortBy, String sortOrder) {
		String hql = "FROM Participant ORDER BY " + sortBy + " " + sortOrder;
		Query<Participant> query = connector.getSession().createQuery(hql, Participant.class);
		return query.list();
	}

	public Collection<Participant> getAllFilteredAndSorted(String sortBy, String sortOrder, String key) {
		String hql = "FROM Participant";
		if (key != null && !key.isEmpty()) {
			hql += " WHERE login LIKE :key";
		}
		hql += " ORDER BY " + sortBy + " " + sortOrder;
		Query<Participant> query = connector.getSession().createQuery(hql, Participant.class);
		if (key != null && !key.isEmpty()) {
			query.setParameter("key", "%" + key + "%");
		}
		return query.list();
	}

	public Participant findByLogin(String login) {
		return connector.getSession().get(Participant.class, login);
	}

	public void add(Participant participant) {
		String hashedPassword = passwordEncoder.encode(participant.getPassword());
		participant.setPassword(hashedPassword);

		Transaction transaction = connector.getSession().beginTransaction();
		try {
			connector.getSession().save(participant);
			transaction.commit();
		} catch (Exception e) {
			transaction.rollback();
			throw e; // rethrow exception after rollback
		}
	}

	public void delete(Participant participant) {
		Transaction transaction = connector.getSession().beginTransaction();
		try {
			connector.getSession().delete(participant);
			transaction.commit();
		} catch (Exception e) {
			transaction.rollback();
			throw e; // rethrow exception after rollback
		}
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Participant participant = findByLogin(username);
		if (participant == null) {
			throw new UsernameNotFoundException("User not found: " + username);
		}
		return new org.springframework.security.core.userdetails.User(participant.getLogin(), participant.getPassword(), new ArrayList<>());
	}
}
