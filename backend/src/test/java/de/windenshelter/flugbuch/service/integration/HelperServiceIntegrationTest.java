package de.windenshelter.flugbuch.service.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.DayOfWeek;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import de.windenshelter.flugbuch.dto.HelperRegistrationRequest;
import de.windenshelter.flugbuch.model.CompetitionType;
import de.windenshelter.flugbuch.model.Helper;
import de.windenshelter.flugbuch.model.HelperConfirmationToken;
import de.windenshelter.flugbuch.repository.HelperConfirmationTokenRepository;
import de.windenshelter.flugbuch.repository.HelperRepository;
import de.windenshelter.flugbuch.service.HelperService;

/**
 * Exercises {@link HelperService} against a real (H2) database, proving the
 * end-to-end rule from ticket #54: submitting the form alone never creates a
 * row, and confirming the same email twice updates the existing helper
 * instead of creating a second one. {@link JavaMailSender} is mocked out -
 * no real mail server is reachable in tests - so only the send call itself
 * is stubbed away, not the persistence logic around it.
 */
@SpringBootTest
@Transactional
class HelperServiceIntegrationTest {

    @Autowired
    private HelperService helperService;

    @Autowired
    private HelperRepository helperRepository;

    @Autowired
    private HelperConfirmationTokenRepository tokenRepository;

    @MockitoBean
    private JavaMailSender mailSender;

    private HelperRegistrationRequest sampleRequest() {
        HelperRegistrationRequest request = new HelperRegistrationRequest();
        request.setFirstName("Erika");
        request.setLastName("Musterfrau");
        request.setPhone("0123456789");
        request.setEmail("erika.musterfrau@edpu.de");
        request.setCompetition(CompetitionType.PG);
        request.setSkills("radio, retrieval");
        request.setAvailableDays(Set.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY));
        return request;
    }

    @Test
    void submitRegistration_withoutConfirming_createsNoHelperRow() {
        helperService.submitRegistration(sampleRequest());

        assertThat(helperRepository.findAll()).isEmpty();
        assertThat(tokenRepository.findAll()).hasSize(1);
    }

    @Test
    void submitRegistration_thenConfirm_createsExactlyOneHelper() {
        helperService.submitRegistration(sampleRequest());
        HelperConfirmationToken pendingToken = tokenRepository.findAll().get(0);

        helperService.confirm(pendingToken.getToken());

        assertThat(helperRepository.findAll()).hasSize(1);
        Helper savedHelper = helperRepository.findAll().get(0);
        assertThat(savedHelper.getEmail()).isEqualTo("erika.musterfrau@edpu.de");
        assertThat(savedHelper.getAvailableDays()).containsExactlyInAnyOrder(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
    }

    @Test
    void confirmingTheSameEmailTwice_updatesTheSameRowInsteadOfDuplicating() {
        // 1. First sign-up: create.
        helperService.submitRegistration(sampleRequest());
        HelperConfirmationToken firstToken = tokenRepository.findAll().get(0);
        helperService.confirm(firstToken.getToken());
        Long helperId = helperRepository.findAll().get(0).getId();

        // 2. Same email again, different details: update.
        HelperRegistrationRequest updateRequest = sampleRequest();
        updateRequest.setSkills("bar, launch direction");
        updateRequest.setAvailableDays(Set.of(DayOfWeek.MONDAY));
        helperService.submitRegistration(updateRequest);
        HelperConfirmationToken secondToken = tokenRepository.findAll().stream()
                .filter(t -> !t.isUsed())
                .findFirst().orElseThrow();
        helperService.confirm(secondToken.getToken());

        assertThat(helperRepository.findAll()).hasSize(1);
        Helper updatedHelper = helperRepository.findAll().get(0);
        assertThat(updatedHelper.getId()).isEqualTo(helperId);
        assertThat(updatedHelper.getSkills()).isEqualTo("bar, launch direction");
        assertThat(updatedHelper.getAvailableDays()).containsExactly(DayOfWeek.MONDAY);
    }

    @Test
    void confirm_sameTokenTwice_secondAttemptFails() {
        helperService.submitRegistration(sampleRequest());
        HelperConfirmationToken pendingToken = tokenRepository.findAll().get(0);
        helperService.confirm(pendingToken.getToken());

        org.junit.jupiter.api.Assertions.assertThrows(
                org.springframework.web.server.ResponseStatusException.class,
                () -> helperService.confirm(pendingToken.getToken()));
        assertThat(helperRepository.findAll()).hasSize(1);
    }
}
