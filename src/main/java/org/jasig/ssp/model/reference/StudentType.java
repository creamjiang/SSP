/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.ssp.model.reference;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import org.jasig.ssp.model.Auditable;

/**
 * StudentType reference object.
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class StudentType
		extends AbstractReference
		implements Auditable {

	private static final long serialVersionUID = -7875126705128856132L;

	// Modified for SPC! They have a non-standard EAL type record in the db.
	// Eventually, so this custom file can be eliminated for future upgrades,
	// a data update to properly align the EAL_ID would need to happen with 
	// SPC prod db.
	public static final UUID EAL_ID = UUID
			.fromString("beb66b23-e591-11e1-869f-406c8f22c3ce");

	private boolean requireInitialAppointment;

	/**
	 * Empty constructor
	 */
	public StudentType() {
		super();
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 *            Identifier; required
	 */

	public StudentType(final UUID id) {
		super(id);
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 *            Identifier; required
	 * @param name
	 *            Name; required; max 100 characters
	 */

	public StudentType(final UUID id, final String name) {
		super(id, name);
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 *            Identifier; required
	 * @param name
	 *            Name; required; max 100 characters
	 * @param description
	 *            Description; max 150 characters
	 */
	public StudentType(final UUID id, final String name,
			final String description) {
		super(id, name, description);
	}

	/**
	 * @return the requireInitialAppointment
	 */
	public boolean isRequireInitialAppointment() {
		return requireInitialAppointment;
	}

	/**
	 * @param requireInitialAppointment
	 *            the requireInitialAppointment to set
	 */
	public void setRequireInitialAppointment(
			final boolean requireInitialAppointment) {
		this.requireInitialAppointment = requireInitialAppointment;
	}

	@Override
	protected int hashPrime() {
		return 313;
	}

	@Override
	public int hashCode() { // NOPMD by jon.adams
		return hashPrime() * super.hashCode()
				* (requireInitialAppointment ? 3 : 5);
	}
}