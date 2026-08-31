package de.windenshelter.flugbuch.service;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import de.windenshelter.flugbuch.configuration.mail.HelperConfirmationProperties;
import de.windenshelter.flugbuch.dto.HelperAdminDto;
import de.windenshelter.flugbuch.dto.HelperPublicDto;
import de.windenshelter.flugbuch.dto.HelperRegistrationRequest;
import de.windenshelter.flugbuch.mapper.HelperMapper;
import de.windenshelter.flugbuch.model.Helper;
import de.windenshelter.flugbuch.model.HelperConfirmationToken;
import de.windenshelter.flugbuch.repository.HelperConfirmationTokenRepository;
import de.windenshelter.flugbuch.repository.HelperRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Business logic behind the public helper sign-up form (ticket #54): a
 * single create-or-update form that never writes to {@link Helper} directly.
 * Submitting it stores the proposed values on a {@link HelperConfirmationToken}
 * and emails a confirmation link; only clicking that link (see {@link #confirm})
 * actually creates or updates the helper row. This is what stops fake/ghost
 * helper entries - nobody can register, or edit someone else's entry, using
 * an email address they don't actually control.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HelperService {

    private final HelperRepository helperRepository;
    private final HelperConfirmationTokenRepository tokenRepository;
    private final HelperConfirmationMailService mailService;
    private final HelperConfirmationProperties helperConfirmationProperties;
    private final HelperMapper helperMapper;

    /**
     * Stores the submitted data as a pending token and emails a confirmation
     * link. Whether this is a "create" or "update" - and therefore which of
     * the two email templates goes out - is decided here, by whether
     * {@code request.getEmail()} already belongs to a confirmed helper.
     */
    @Transactional
    public void submitRegistration(HelperRegistrationRequest request) {
        boolean isUpdate = helperRepository.findByEmail(request.getEmail()).isPresent();

        HelperConfirmationToken pendingToken = HelperConfirmationToken.builder()
                .token(UUID.randomUUID().toString())
                .expiresAt(Instant.now().plus(Duration.ofMinutes(helperConfirmationProperties.getTokenExpirationMinutes())))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .competition(request.getCompetition())
                .skills(request.getSkills())
                .availableDays(request.getAvailableDays())
                .build();
        tokenRepository.save(pendingToken);

        try {
            if (isUpdate) {
                mailService.sendUpdateConfirmation(pendingToken);
            } else {
                mailService.sendCreateConfirmation(pendingToken);
            }
        } catch (MailException e) {
            log.error("Failed to send helper confirmation email to {}: {}", pendingToken.getEmail(), e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Could not send the confirmation email, please try again later");
        }
    }

    /**
     * Redeems a confirmation link: creates a new helper, or updates the
     * existing one matching {@code pendingToken.getEmail()}, with the
     * proposed values - never both at once, since email is unique on
     * {@link Helper}.
     */
    @Transactional
    public void confirm(String token) {
        HelperConfirmationToken pendingToken = tokenRepository.findByToken(token)
                .filter(t -> !t.isUsed())
                .filter(t -> t.getExpiresAt().isAfter(Instant.now()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired confirmation link"));

        Helper helper = helperRepository.findByEmail(pendingToken.getEmail())
                .orElseGet(Helper::new);
        helper.setFirstName(pendingToken.getFirstName());
        helper.setLastName(pendingToken.getLastName());
        helper.setPhone(pendingToken.getPhone());
        helper.setEmail(pendingToken.getEmail());
        helper.setCompetition(pendingToken.getCompetition());
        helper.setSkills(pendingToken.getSkills());
        // Copied into a fresh Set rather than assigned directly: Hibernate
        // already tracks pendingToken.availableDays as that entity's own
        // persistent collection, so handing the very same Set instance to a
        // second entity (helper) makes Hibernate see "two representations of
        // the same collection" and blow up on flush. A copy has no such tie.
        helper.setAvailableDays(new HashSet<>(pendingToken.getAvailableDays()));
        helperRepository.save(helper);

        pendingToken.setUsed(true);
        tokenRepository.save(pendingToken);
    }

    /** Reduced view for the public listing (no login required): name, skills, availability. */
    public List<HelperPublicDto> getPublicList() {
        return helperRepository.findAll().stream()
                .map(helperMapper::toPublicDto)
                .toList();
    }

    /** Full records including contact details - ADMIN-only, enforced by SecurityConfig. */
    public List<HelperAdminDto> getAdminList() {
        return helperRepository.findAll().stream()
                .map(helperMapper::toAdminDto)
                .toList();
    }
}
