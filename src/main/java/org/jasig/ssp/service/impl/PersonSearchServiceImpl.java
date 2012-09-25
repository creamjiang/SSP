package org.jasig.ssp.service.impl;

import java.util.Collection;

import org.jasig.ssp.dao.PersonSearchDao;
import org.jasig.ssp.model.Person;
import org.jasig.ssp.model.PersonSearchResult;
import org.jasig.ssp.model.reference.ProgramStatus;
import org.jasig.ssp.service.ObjectNotFoundException;
import org.jasig.ssp.service.PersonProgramStatusService;
import org.jasig.ssp.service.PersonSearchService;
import org.jasig.ssp.util.sort.PagingWrapper;
import org.jasig.ssp.util.sort.SortingAndPaging;
import org.jasig.ssp.web.api.validation.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

/**
 * PersonSearch service implementation
 */
@Service
@Transactional
public class PersonSearchServiceImpl implements PersonSearchService {

	@Autowired
	private transient PersonSearchDao dao;

	@Autowired
	private transient PersonProgramStatusService personProgramStatus;

	@Override
	public PagingWrapper<PersonSearchResult> searchBy(
			final ProgramStatus programStatus, final Boolean outsideCaseload,
			final String searchTerm, final Person advisor,
			final SortingAndPaging sAndP)
			throws ObjectNotFoundException, ValidationException {

		final PagingWrapper<Person> people = dao.searchBy(programStatus,
				(outsideCaseload == null ? Boolean.FALSE : outsideCaseload),
				searchTerm, advisor, sAndP);

		final Collection<PersonSearchResult> personSearchResults = Lists
				.newArrayList();
		for (final Person person : people) {
			personSearchResults.add(new PersonSearchResult(person, // NOPMD
					personProgramStatus));
		}

		return new PagingWrapper<PersonSearchResult>(people.getResults(),
				personSearchResults);
	}
}