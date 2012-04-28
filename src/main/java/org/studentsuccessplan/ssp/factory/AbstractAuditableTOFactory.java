package org.studentsuccessplan.ssp.factory;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.studentsuccessplan.ssp.dao.AuditableCrudDao;
import org.studentsuccessplan.ssp.model.Auditable;
import org.studentsuccessplan.ssp.service.ObjectNotFoundException;
import org.studentsuccessplan.ssp.transferobject.AuditableTO;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public abstract class AbstractAuditableTOFactory<TObject extends AuditableTO<M>, M extends Auditable>
		implements TOFactory<TObject, M> {

	private final Class<TObject> tObjectClass;
	private final Class<M> mClass;

	private static final Logger LOGGER = LoggerFactory
			.getLogger(AbstractAuditableTOFactory.class);

	public AbstractAuditableTOFactory(final Class<TObject> tObjectClass,
			final Class<M> mClass) {
		this.tObjectClass = tObjectClass;
		this.mClass = mClass;
	}

	@Override
	public M from(final TObject tObject) throws ObjectNotFoundException {
		M model;

		if (tObject.getId() == null) {
			model = newModel();
		} else {
			model = getDao().get(tObject.getId());
			if (model == null) {
				throw new ObjectNotFoundException(
						"id provided, but not valid: "
								+ tObject.getId().toString());
			}
		}

		model.setObjectStatus(tObject.getObjectStatus());

		return model;
	}

	@Override
	public TObject from(final M model) {
		final TObject tObject = newTObject();
		tObject.from(model);
		return tObject;
	}

	@Override
	public M from(final UUID id) {
		return getDao().get(id);
	}

	@Override
	public List<TObject> asTOList(
			final Collection<M> models) {
		final List<TObject> tos = Lists.newArrayList();

		if ((models != null) && !models.isEmpty()) {
			for (M model : models) {
				tos.add(from(model));
			}
		}

		return tos;
	}

	@Override
	public Set<TObject> asTOSet(final Collection<M> models) {
		final Set<TObject> tos = Sets.newHashSet();
		for (M model : models) {
			tos.add(from(model));
		}
		return tos;
	}

	@Override
	public Set<M> asSet(final Collection<TObject> tObjects)
			throws ObjectNotFoundException {
		final Set<M> models = Sets.newHashSet();
		for (TObject tObject : tObjects) {
			models.add(from(tObject));
		}
		return models;
	}

	private TObject newTObject() {
		try {
			return tObjectClass.newInstance();
		} catch (InstantiationException e) {
			LOGGER.error("unable to instantiate Transfer object in factory.");
		} catch (IllegalAccessException e) {
			LOGGER.error("unable to instantiate Transfer object in factory.");
		}
		return null;
	}

	private M newModel() {
		try {
			return mClass.newInstance();
		} catch (InstantiationException e) {
			LOGGER.error("unable to instantiate Model object in factory.");
		} catch (IllegalAccessException e) {
			LOGGER.error("unable to instantiate Model object in factory.");
		}
		return null;
	}

	protected abstract AuditableCrudDao<M> getDao();
}
