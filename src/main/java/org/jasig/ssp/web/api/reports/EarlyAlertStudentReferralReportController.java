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
package org.jasig.ssp.web.api.reports; // NOPMD

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;

import org.apache.commons.lang.StringUtils;
import org.jasig.ssp.factory.PersonTOFactory;
import org.jasig.ssp.model.ObjectStatus;
import org.jasig.ssp.model.Person;
import org.jasig.ssp.model.external.Term;
import org.jasig.ssp.service.EarlyAlertResponseService;
import org.jasig.ssp.service.EarlyAlertService;
import org.jasig.ssp.service.JournalEntryService;
import org.jasig.ssp.service.ObjectNotFoundException;
import org.jasig.ssp.service.PersonService;
import org.jasig.ssp.service.TaskService;
import org.jasig.ssp.service.external.TermService;
import org.jasig.ssp.service.reference.ProgramStatusService;
import org.jasig.ssp.service.reference.ReferralSourceService;
import org.jasig.ssp.service.reference.SpecialServiceGroupService;
import org.jasig.ssp.service.reference.StudentTypeService;
import org.jasig.ssp.transferobject.PersonTO;
import org.jasig.ssp.transferobject.reports.AddressLabelSearchTO;
import org.jasig.ssp.transferobject.reports.CaseLoadActivityReportTO;
import org.jasig.ssp.transferobject.reports.EarlyAlertStudentReportTO;
import org.jasig.ssp.transferobject.reports.EarlyAlertStudentSearchTO;
import org.jasig.ssp.util.sort.PagingWrapper;
import org.jasig.ssp.util.sort.SortingAndPaging;
import org.jasig.ssp.web.api.AbstractBaseController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Maps;

/**
 * Service methods for Reporting on Early Alert Student Referrals
 * <p>
 * Mapped to URI path <code>/1/report/earlyalertstudentreferral</code>
 */

@Controller
@RequestMapping("/1/report/earlyalertstudentreferral")
public class EarlyAlertStudentReferralReportController extends EarlyAlertReportBaseController {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(AddressLabelsReportController.class);

	@Autowired
	private transient PersonService personService;
	@Autowired
	private transient PersonTOFactory personTOFactory;
	@Autowired
	private transient TermService termService;
	@Autowired
	private transient ReferralSourceService referralSourcesService;
	@Autowired
	private transient ProgramStatusService programStatusService;	
	@Autowired
	protected transient EarlyAlertService earlyAlertService;
	@Autowired
	protected transient EarlyAlertResponseService earlyAlertResponseService;

	// @Autowired
	// private transient PersonTOFactory factory;

	@InitBinder
	public void initBinder(final WebDataBinder binder) {
		final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy",
				Locale.US);
		dateFormat.setLenient(false);
		binder.registerCustomEditor(Date.class, new CustomDateEditor(
				dateFormat, true));
	}

	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public void getAddressLabels(
			final HttpServletResponse response,
			final @RequestParam(required = false) ObjectStatus status,
			final @RequestParam(required = false) UUID coachId,			
			final @RequestParam(required = false) UUID programStatus,
			final @RequestParam(required = false) UUID referralSourceId,
			final @RequestParam(required = false) Date createDateFrom,
			final @RequestParam(required = false) Date createDateTo,
			final @RequestParam(required = false) String termCode,
			final @RequestParam(required = false, defaultValue = "pdf") String reportType)
			throws ObjectNotFoundException, JRException, IOException {
		
		Person coach = null;
		PersonTO coachTO = null;
		Date startDate = createDateFrom;
		Date endDate = createDateTo;
		String termName = "";
		
		if(termCode != null && termCode.length()> 0) {
			Term term = termService.getByCode(termCode);
			termName = term.getName();
			startDate = term.getStartDate();
			endDate = term.getEndDate();
		}
		
		if(coachId != null)
		{
			coach = personService.get(coachId);
			coachTO = personTOFactory.from(coach);
		}		

		final AddressLabelSearchTO addressLabelSearchTO = new AddressLabelSearchTO(
				coachTO,
				programStatus, null, (List<UUID>)Arrays.asList(referralSourceId), null, null,
				null, createDateFrom,
				createDateTo);
		
		final EarlyAlertStudentSearchTO searchForm = new EarlyAlertStudentSearchTO(addressLabelSearchTO, createDateFrom, createDateTo);

		// TODO Specifying person name sort fields in the SaP doesn't seem to
		// work... end up with empty results need to dig into actual query
		// building
		final PagingWrapper<EarlyAlertStudentReportTO> peopleInfo = earlyAlertService.getStudentsEarlyAlertCountSetForCritera(
				searchForm, SortingAndPaging.createForSingleSort(status, null,
						null, null, null, null));
		
		// final String programStatusName = ((null!=programStatus &&
		// !programStatus.isEmpty())?programStatus.get(0)():"");
		// Get the actual name of the UUID for the programStatus
		final String programStatusName = (programStatus == null ? ""
				: programStatusService.get(programStatus).getName());

		final Map<String, Object> parameters = Maps.newHashMap();
		
		parameters.put("coachName",  getFullName(coachTO));
		
		setStartDateEndDateToMap(parameters, startDate, endDate);
		parameters.put("programStatus", programStatusName);
		parameters.put("referralSourceName", referralSourcesService.get(referralSourceId).getName());
		parameters.put("reportDate", new Date());
		parameters.put("termName", termName);
		

		generateReport( response,  parameters, peopleInfo == null ? null : peopleInfo.getRows(),  "/reports/earlyAlertStudentReferralReport.jasper", 
				 reportType, "Early_Alert_Student_Referral_Report");
	}

	@Override
	protected Logger getLogger() {
		return LOGGER;
	}
}