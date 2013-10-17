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
package org.jasig.ssp.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.jasig.ssp.model.reference.ProgramStatus;
import org.jasig.ssp.service.ObjectNotFoundException;
import org.jasig.ssp.service.PersonProgramStatusService;
import org.jasig.ssp.transferobject.CoachPersonLiteTO;
import org.jasig.ssp.transferobject.PersonSearchResultTO;
import org.jasig.ssp.web.api.PersonSearchController;
import org.jasig.ssp.web.api.validation.ValidationException;

/**
 * PersonSearchResult model for use by {@link PersonSearchResultTO} and then
 * {@link PersonSearchController}.
 */
public class PersonSearchRequest {

	
	public static final String PLAN_STATUS_ACTIVE = "ACTIVE";
	
	public static final String PLAN_STATUS_INACTIVE = "INACTIVE";

	public static final String MAP_STATUS_ON_PLAN = "ON_PLAN";
	
	public static final String MAP_STATUS_OFF_PLAN = "OFF_PLAN";

	
	// id of the student
	private String studentId;

	private ProgramStatus programStatus;

	private Person coach;

	private String declaredMajor;

	private BigDecimal hoursEarnedMin;
	
	private BigDecimal hoursEarnedMax;

	private BigDecimal gpaEarnedMin;
	
	private BigDecimal gpaEarnedMax;
	
	private Boolean currentlyRegistered;
	
	private String planStatus;
	
	private String sapStatus;
	
	private String mapStatus;
	
	private Boolean myCaseload;
	
	private Boolean myPlans;

	private Date birthDate;

	public PersonSearchRequest() {
		super();
	}


	public String getStudentId() {
		return studentId;
	}


	public void setStudentId(String studentId) {
		this.studentId = studentId;
	}


	public ProgramStatus getProgramStatus() {
		return programStatus;
	}


	public void setProgramStatus(ProgramStatus programStatus) {
		this.programStatus = programStatus;
	}


	public Person getCoach() {
		return coach;
	}


	public void setCoach(Person coach) {
		this.coach = coach;
	}


	public String getDeclaredMajor() {
		return declaredMajor;
	}


	public void setDeclaredMajor(String declaredMajor) {
		this.declaredMajor = declaredMajor;
	}


	public BigDecimal getHoursEarnedMin() {
		return hoursEarnedMin;
	}


	public void setHoursEarnedMin(BigDecimal hoursEarnedMin) {
		this.hoursEarnedMin = hoursEarnedMin;
	}


	public BigDecimal getHoursEarnedMax() {
		return hoursEarnedMax;
	}


	public void setHoursEarnedMax(BigDecimal hoursEarnedMax) {
		this.hoursEarnedMax = hoursEarnedMax;
	}


	public BigDecimal getGpaEarnedMin() {
		return gpaEarnedMin;
	}


	public void setGpaEarnedMin(BigDecimal gpaEarnedMin) {
		this.gpaEarnedMin = gpaEarnedMin;
	}


	public BigDecimal getGpaEarnedMax() {
		return gpaEarnedMax;
	}


	public void setGpaEarnedMax(BigDecimal gpaEarnedMax) {
		this.gpaEarnedMax = gpaEarnedMax;
	}


	public Boolean getCurrentlyRegistered() {
		return currentlyRegistered;
	}


	public void setCurrentlyRegistered(Boolean currentlyRegistered) {
		this.currentlyRegistered = currentlyRegistered;
	}


	public String getSapStatus() {
		return sapStatus;
	}


	public void setSapStatus(String sapStatus) {
		this.sapStatus = sapStatus;
	}


	public String getMapStatus() {
		return mapStatus;
	}


	public void setMapStatus(String mapStatus) {
		this.mapStatus = mapStatus;
	}


	public String getPlanStatus() {
		return planStatus;
	}


	public void setPlanStatus(String planStatus) {
		this.planStatus = planStatus;
	}


	public Boolean getMyCaseload() {
		return myCaseload;
	}


	public void setMyCaseload(Boolean myCaseload) {
		this.myCaseload = myCaseload;
	}


	public Boolean getMyPlans() {
		return myPlans;
	}


	public void setMyPlans(Boolean myPlans) {
		this.myPlans = myPlans;
	}


	public Date getBirthDate() {
		return birthDate;
	}


	public void setBirthDate(Date birthDate) {
		this.birthDate = birthDate;
	}


}