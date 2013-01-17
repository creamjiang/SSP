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
package org.jasig.mygps.web;

import java.util.List;
import java.util.UUID;

import org.jasig.ssp.factory.reference.ChallengeReferralTOFactory;
import org.jasig.ssp.model.reference.Challenge;
import org.jasig.ssp.security.permissions.Permission;
import org.jasig.ssp.service.reference.ChallengeReferralService;
import org.jasig.ssp.service.reference.ChallengeService;
import org.jasig.ssp.transferobject.reference.ChallengeReferralTO;
import org.jasig.ssp.web.api.AbstractBaseController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Lists;

@Controller
@RequestMapping("/1/mygps/challengereferral")
public class MyGpsChallengeReferralController extends AbstractBaseController {

	@Autowired
	private transient ChallengeReferralService challengeReferralService;

	@Autowired
	private transient ChallengeReferralTOFactory challengeReferralTOFactory;

	@Autowired
	private transient ChallengeService challengeService;

	private static final Logger LOGGER = LoggerFactory
			.getLogger(MyGpsChallengeReferralController.class);

	public MyGpsChallengeReferralController() {
		super();
	}

	public MyGpsChallengeReferralController(
			final ChallengeReferralService challengeReferralService,
			final ChallengeService challengeService,
			final ChallengeReferralTOFactory challangeReferralTOFactory) {
		super();
		this.challengeReferralService = challengeReferralService;
		this.challengeService = challengeService;
		challengeReferralTOFactory = challangeReferralTOFactory;
	}

	@RequestMapping(value = "/getByChallengeId", method = RequestMethod.GET)
	@PreAuthorize(Permission.DENY_ALL) // TODO set a more relevant permission. just locking down by default for now
	public @ResponseBody
	List<ChallengeReferralTO> getByChallengeId(
			@RequestParam("challengeId") final UUID challengeId)
			throws Exception {

		try {
			final Challenge challenge = challengeService.get(challengeId);
			return challengeReferralTOFactory.asTOList(challengeReferralService
					.getChallengeReferralsByChallengeId(challenge));

		} catch (Exception e) {
			LOGGER.error("ERROR : getByChallengeId() : {}", e.getMessage(), e);
			throw e;
		}
	}

	@RequestMapping(value = "/search", method = RequestMethod.GET)
	@PreAuthorize(Permission.DENY_ALL) // TODO set a more relevant permission. just locking down by default for now
	public @ResponseBody
	List<ChallengeReferralTO> search(@RequestParam("query") final String query,
			@RequestParam("challengeId") final UUID challengeId)
			throws Exception {

		// Not using query param as we are no longer searching the
		// referrals, simply returning based on challengeId and whether the
		// referral is public.

		try {
			final Challenge challenge = challengeService.get(challengeId);
			if (challenge == null) {
				return Lists.newArrayList();
			}

			return challengeReferralTOFactory
					.asTOList(challengeReferralService
							.challengeReferralSearch(challenge));
		} catch (Exception e) {
			LOGGER.error("ERROR : search() : {}", e.getMessage(), e);
			throw e;
		}
	}

	@Override
	protected Logger getLogger() {
		return LOGGER;
	}
}
