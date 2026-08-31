package de.windenshelter.flugbuch.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.DayOfWeek;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import de.windenshelter.flugbuch.configuration.mail.HelperConfirmationProperties;
import de.windenshelter.flugbuch.dto.HelperRegistrationRequest;
import de.windenshelter.flugbuch.mapper.HelperMapper;
import de.windenshelter.flugbuch.model.CompetitionType;
import de.windenshelter.flugbuch.model.Helper;
import de.windenshelter.flugbuch.model.HelperConfirmationToken;
import de.windenshelter.flugbuch.repository.HelperConfirmationTokenRepository;
import de.windenshelter.flugbuch.repository.HelperRepository;

/**
 * Plain Mockito unit tests for {@link HelperService} - no Spring context or
 * database required. Covers the core rule the ticket cares about: nothing
 * lands in {@link Helper} until a confirmation token is redeemed, and the
 * create-vs-update decision (and matching email template) is based purely on
 * whether the submitted email already belongs to a confirmed helper.
 */
class HelperServiceTest {

    private final HelperRepository helperRepository = mock(HelperRepository.class);
    private final HelperConfirmationTokenRepository tokenRepository = mock(HelperConfirmationTokenRepository.class);
    private final HelperConfirmationMailService mailService = mock(HelperConfirmationMailService.class);
    private final HelperMapper helperMapper = mock(HelperMapper.class);

    private final HelperConfirmationProperties properties = new HelperConfirmationProperties();
    private final HelperService service;

    HelperServiceTest() {
        properties.setTokenExpirationMinutes(60);
        properties.setFrontendConfirmUrl("http://localhost:4200/helpers/confirm");
        properties.setMailFrom("no-reply@flugbuch.local");
        service = new HelperService(helperRepository, tokenRepository, mailService, properties, helperMapper);
    }

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

    // -------------------------------------------------------------------
    // submitRegistration
    // -------------------------------------------------------------------

    @Test
    void submitRegistration_newEmail_savesPendingTokenAndSendsCreateMail() {
        HelperRegistrationRequest request = sampleRequest();
        when(helperRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(tokenRepository.save(any(HelperConfirmationToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.submitRegistration(request);

        verify(tokenRepository).save(any(HelperConfirmationToken.class));
        verify(mailService).sendCreateConfirmation(any(HelperConfirmationToken.class));
        verify(mailService, never()).sendUpdateConfirmation(any());
        verify(helperRepository, never()).save(any());
    }

    @Test
    void submitRegistration_knownEmail_savesPendingTokenAndSendsUpdateMail() {
        HelperRegistrationRequest request = sampleRequest();
        Helper existing = Helper.builder().id(1L).email(request.getEmail()).build();
        when(helperRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(existing));
        when(tokenRepository.save(any(HelperConfirmationToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.submitRegistration(request);

        verify(tokenRepository).save(any(HelperConfirmationToken.class));
        verify(mailService).sendUpdateConfirmation(any(HelperConfirmationToken.class));
        verify(mailService, never()).sendCreateConfirmation(any());
        verify(helperRepository, never()).save(any());
    }

    @Test
    void submitRegistration_capturesSubmittedValuesOnThePendingToken() {
        HelperRegistrationRequest request = sampleRequest();
        when(helperRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(tokenRepository.save(any(HelperConfirmationToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.submitRegistration(request);

        var captor = org.mockito.ArgumentCaptor.forClass(HelperConfirmationToken.class);
        verify(tokenRepository).save(captor.capture());
        HelperConfirmationToken savedToken = captor.getValue();
        assertThat(savedToken.getFirstName()).isEqualTo("Erika");
        assertThat(savedToken.getEmail()).isEqualTo("erika.musterfrau@edpu.de");
        assertThat(savedToken.getCompetition()).isEqualTo(CompetitionType.PG);
        assertThat(savedToken.getAvailableDays()).containsExactlyInAnyOrder(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
        assertThat(savedToken.isUsed()).isFalse();
        assertThat(savedToken.getExpiresAt()).isAfter(Instant.now());
    }

    // -------------------------------------------------------------------
    // confirm
    // -------------------------------------------------------------------

    private HelperConfirmationToken pendingToken(String email, Instant expiresAt, boolean used) {
        return HelperConfirmationToken.builder()
                .id(1L)
                .token("valid-token")
                .expiresAt(expiresAt)
                .used(used)
                .firstName("Erika")
                .lastName("Musterfrau")
                .phone("0123456789")
                .email(email)
                .competition(CompetitionType.PG)
                .skills("radio")
                .availableDays(Set.of(DayOfWeek.SATURDAY))
                .build();
    }

    @Test
    void confirm_newHelper_createsHelperFromPendingTokenAndMarksTokenUsed() {
        HelperConfirmationToken pendingToken = pendingToken("erika.musterfrau@edpu.de", Instant.now().plusSeconds(60), false);
        when(tokenRepository.findByToken("valid-token")).thenReturn(Optional.of(pendingToken));
        when(helperRepository.findByEmail("erika.musterfrau@edpu.de")).thenReturn(Optional.empty());

        service.confirm("valid-token");

        var captor = org.mockito.ArgumentCaptor.forClass(Helper.class);
        verify(helperRepository).save(captor.capture());
        Helper savedHelper = captor.getValue();
        assertThat(savedHelper.getFirstName()).isEqualTo("Erika");
        assertThat(savedHelper.getEmail()).isEqualTo("erika.musterfrau@edpu.de");
        assertThat(pendingToken.isUsed()).isTrue();
        verify(tokenRepository).save(pendingToken);
    }

    @Test
    void confirm_existingHelper_updatesThatSameRowInsteadOfCreatingASecondOne() {
        HelperConfirmationToken pendingToken = pendingToken("erika.musterfrau@edpu.de", Instant.now().plusSeconds(60), false);
        Helper existing = Helper.builder().id(42L).email("erika.musterfrau@edpu.de").firstName("Old Name").build();
        when(tokenRepository.findByToken("valid-token")).thenReturn(Optional.of(pendingToken));
        when(helperRepository.findByEmail("erika.musterfrau@edpu.de")).thenReturn(Optional.of(existing));

        service.confirm("valid-token");

        var captor = org.mockito.ArgumentCaptor.forClass(Helper.class);
        verify(helperRepository).save(captor.capture());
        Helper savedHelper = captor.getValue();
        assertThat(savedHelper.getId()).isEqualTo(42L);
        assertThat(savedHelper.getFirstName()).isEqualTo("Erika");
    }

    @Test
    void confirm_unknownToken_throwsBadRequestAndSavesNothing() {
        when(tokenRepository.findByToken("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.confirm("unknown")).isInstanceOf(ResponseStatusException.class);
        verify(helperRepository, never()).save(any());
    }

    @Test
    void confirm_expiredToken_throwsBadRequestAndSavesNothing() {
        HelperConfirmationToken expired = pendingToken("erika.musterfrau@edpu.de", Instant.now().minusSeconds(60), false);
        when(tokenRepository.findByToken("valid-token")).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> service.confirm("valid-token")).isInstanceOf(ResponseStatusException.class);
        verify(helperRepository, never()).save(any());
    }

    @Test
    void confirm_alreadyUsedToken_throwsBadRequestAndSavesNothing() {
        HelperConfirmationToken used = pendingToken("erika.musterfrau@edpu.de", Instant.now().plusSeconds(60), true);
        when(tokenRepository.findByToken("valid-token")).thenReturn(Optional.of(used));

        assertThatThrownBy(() -> service.confirm("valid-token")).isInstanceOf(ResponseStatusException.class);
        verify(helperRepository, never()).save(any());
    }

    // -------------------------------------------------------------------
    // getPublicList / getAdminList
    // -------------------------------------------------------------------

    @Test
    void getPublicList_mapsEveryConfirmedHelperToThePublicDto() {
        Helper helper = Helper.builder().id(1L).email("erika.musterfrau@edpu.de").build();
        when(helperRepository.findAll()).thenReturn(List.of(helper));

        service.getPublicList();

        verify(helperMapper).toPublicDto(helper);
    }

    @Test
    void getAdminList_mapsEveryConfirmedHelperToTheAdminDto() {
        Helper helper = Helper.builder().id(1L).email("erika.musterfrau@edpu.de").build();
        when(helperRepository.findAll()).thenReturn(List.of(helper));

        service.getAdminList();

        verify(helperMapper).toAdminDto(helper);
    }
}
