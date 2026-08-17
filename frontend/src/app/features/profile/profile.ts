import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { TranslatePipe } from '@ngx-translate/core';
import { PilotService } from '../../core/services/pilot';
import { AuthService } from '../../core/services/auth';
import { PilotProfile } from '../../core/models/pilot-profile.model';

@Component({
    selector: 'app-profile',
    standalone: true,
    imports: [CommonModule, FormsModule, TranslatePipe],
    templateUrl: './profile.html',
    styleUrls: ['./profile.scss'],
})
export class ProfileComponent implements OnInit {
    private readonly pilotService = inject(PilotService);
    private readonly authService = inject(AuthService);
    private readonly router = inject(Router);

    // Populated from GET /pilots/me in ngOnInit - empty until then.
    public username = '';
    public firstName = ''; public lastName = '';
    public email = ''; public phone = '';
    public licenseType = ''; public licenseNumber = '';
    public homeAirfield = '';

    /**
     * What's actually saved in the database, straight from PilotService's
     * shared signal (see there) - never from the plain form fields above, so
     * the avatar card doesn't flicker/change as you type and only reflects
     * reality once a server response has actually come back (initial load,
     * a successful "Save", or a picture upload). This is also what keeps the
     * sidebar avatar in sync: both this card and the sidebar read the same
     * underlying signal.
     */
    public readonly savedProfile = computed(() => ({
        firstName: this.pilotService.currentProfile()?.firstName ?? '',
        lastName: this.pilotService.currentProfile()?.lastName ?? '',
        licenseType: this.pilotService.currentProfile()?.licenseType ?? '',
        homeAirfield: this.pilotService.currentProfile()?.homeAirfield ?? '',
        profilePicture: this.pilotService.currentProfile()?.profilePicture ?? null,
    }));

    public readonly isLoading = signal(true);
    public readonly loadError = signal(false);
    public readonly isSaving = signal(false);
    public readonly saveSuccess = signal(false);
    public readonly saveError = signal(false);

    // Profile picture upload
    private static readonly MAX_PICTURE_BYTES = 1_000_000; // 1 MB - keeps the base64 data URI a reasonable size to store/transfer.
    private static readonly ALLOWED_PICTURE_TYPES = ['image/png', 'image/jpeg', 'image/gif', 'image/webp'];
    public readonly pictureUploading = signal(false);
    /** Holds a translation key (or null) - resolved via the translate pipe in the template. */
    public readonly pictureError = signal<string | null>(null);

    public readonly licenseTypes = ['PPL(A)', 'PPL(B)', 'CPL(A)', 'ATPL', 'SPL', 'Schüler'];
    // label holds a translation key, resolved via the translate pipe in the template.
    // These are flight-log-derived stats, not part of the Pilot profile itself -
    // there's no aggregate-stats endpoint yet, so this card stays illustrative.
    public readonly stats = [
        { label: 'DASHBOARD.STAT_TOTAL_FLIGHTS', value: '42' },
        { label: 'PROFILE.STAT_TOTAL_HOURS', value: '36h 20m' },
        { label: 'DASHBOARD.STAT_TOWS', value: '28' },
        { label: 'PROFILE.STAT_MEMBER_SINCE', value: '2023' },
    ];

    // Password change
    public currentPw = ''; public newPw = ''; public confirmPw = '';
    // Mirrors the backend's @Pattern on ChangePasswordRequest.newPassword (see
    // there) - checked client-side too so a pilot finds out immediately
    // instead of after a round trip to the server.
    private static readonly PASSWORD_COMPLEXITY = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).+$/;
    public readonly passwordSaving = signal(false);
    public readonly passwordSuccess = signal(false);
    /** Holds a translation key (or null) - resolved via the translate pipe in the template. */
    public readonly passwordError = signal<string | null>(null);

    // Delete account
    public readonly showDeleteModal = signal(false);
    public deletePassword = '';
    public readonly deleteLoading = signal(false);
    /** Holds a translation key (or null) - resolved via the translate pipe in the template. */
    public readonly deleteError = signal<string | null>(null);

    public ngOnInit(): void {
        this.pilotService.getMyProfile().subscribe({
            next: (profile) => this.applyProfile(profile),
            error: () => {
                this.isLoading.set(false);
                this.loadError.set(true);
            },
        });
    }

    private applyProfile(profile: PilotProfile): void {
        this.username = profile.username;
        this.firstName = profile.firstName ?? '';
        this.lastName = profile.lastName ?? '';
        this.email = profile.email ?? '';
        this.phone = profile.phone ?? '';
        this.licenseType = profile.licenseType ?? '';
        this.licenseNumber = profile.licenseNumber ?? '';
        this.homeAirfield = profile.homeAirfield ?? '';
        this.isLoading.set(false);
        this.loadError.set(false);
        // savedProfile (above) updates itself - it's a computed() over
        // PilotService.currentProfile, which every PilotService call that
        // returns a profile (including the one that got us `profile` here)
        // already writes to as a side effect.
    }

    /** Reads the chosen file, validates it client-side, then uploads it right away (not gated behind "Save changes"). */
    public onPictureSelected(event: Event): void {
        const input = event.target as HTMLInputElement;
        const file = input.files?.[0];
        input.value = ''; // so picking the same file again later still fires a change event

        if (!file) {
            return;
        }

        this.pictureError.set(null);

        if (!ProfileComponent.ALLOWED_PICTURE_TYPES.includes(file.type)) {
            this.pictureError.set('PROFILE.PICTURE_INVALID_TYPE');
            return;
        }
        if (file.size > ProfileComponent.MAX_PICTURE_BYTES) {
            this.pictureError.set('PROFILE.PICTURE_TOO_LARGE');
            return;
        }

        const reader = new FileReader();
        reader.onload = () => this.uploadPicture(reader.result as string);
        reader.onerror = () => this.pictureError.set('PROFILE.PICTURE_UPLOAD_FAILED');
        reader.readAsDataURL(file);
    }

    private uploadPicture(dataUrl: string): void {
        this.pictureUploading.set(true);

        this.pilotService.updateProfilePicture({ profilePicture: dataUrl }).subscribe({
            next: (profile) => {
                this.applyProfile(profile);
                this.pictureUploading.set(false);
            },
            error: () => {
                this.pictureUploading.set(false);
                this.pictureError.set('PROFILE.PICTURE_UPLOAD_FAILED');
            },
        });
    }

    public save(): void {
        this.isSaving.set(true);
        this.saveSuccess.set(false);
        this.saveError.set(false);

        this.pilotService.updateMyProfile({
            firstName: this.firstName,
            lastName: this.lastName,
            email: this.email,
            phone: this.phone,
            licenseType: this.licenseType,
            licenseNumber: this.licenseNumber,
            homeAirfield: this.homeAirfield,
        }).subscribe({
            next: (profile) => {
                this.applyProfile(profile);
                this.isSaving.set(false);
                this.saveSuccess.set(true);
                setTimeout(() => this.saveSuccess.set(false), 3000);
            },
            error: () => {
                this.isSaving.set(false);
                this.saveError.set(true);
            },
        });
    }

    public changePassword(): void {
        this.passwordError.set(null);
        this.passwordSuccess.set(false);

        if (!this.currentPw || !this.newPw) {
            this.passwordError.set('PROFILE.PASSWORD_REQUIRED');
            return;
        }
        if (this.newPw !== this.confirmPw) {
            this.passwordError.set('REGISTER.PASSWORD_MISMATCH');
            return;
        }
        // Checked client-side first (cheap, instant) even though the backend
        // enforces both rules too (see ChangePasswordRequest / PilotService) -
        // that server-side check is what actually matters for security, this
        // one is purely so a pilot isn't stuck waiting on a round trip to find
        // out they made an obvious mistake.
        if (this.newPw === this.currentPw) {
            this.passwordError.set('PROFILE.PASSWORD_SAME_AS_CURRENT');
            return;
        }
        if (!ProfileComponent.PASSWORD_COMPLEXITY.test(this.newPw)) {
            this.passwordError.set('PROFILE.PASSWORD_TOO_SIMPLE');
            return;
        }

        this.passwordSaving.set(true);

        this.pilotService.changePassword({
            currentPassword: this.currentPw,
            newPassword: this.newPw,
        }).subscribe({
            next: () => {
                this.passwordSaving.set(false);
                this.passwordSuccess.set(true);
                this.currentPw = ''; this.newPw = ''; this.confirmPw = '';
                setTimeout(() => this.passwordSuccess.set(false), 3000);
            },
            // Distinct statuses for distinct failure reasons (see PilotService):
            // 400 = wrong current password, 422 = new password same as current,
            // 429 = too many attempts (see RateLimitingFilter). None of these are
            // 401, so the auth interceptor never treats this as an expired
            // session - we just show the matching inline error below instead.
            error: (error: HttpErrorResponse) => {
                this.passwordSaving.set(false);
                if (error.status === 400) {
                    this.passwordError.set('PROFILE.CURRENT_PASSWORD_WRONG');
                } else if (error.status === 422) {
                    this.passwordError.set('PROFILE.PASSWORD_SAME_AS_CURRENT');
                } else if (error.status === 429) {
                    this.passwordError.set('COMMON.RATE_LIMITED');
                } else {
                    this.passwordError.set('PROFILE.PASSWORD_UPDATE_FAILED');
                }
            },
        });
    }

    public openDeleteModal(): void {
        this.deletePassword = '';
        this.deleteError.set(null);
        this.showDeleteModal.set(true);
    }

    public closeDeleteModal(): void {
        this.showDeleteModal.set(false);
    }

    public confirmDeleteAccount(): void {
        this.deleteError.set(null);

        if (!this.deletePassword) {
            this.deleteError.set('PROFILE.PASSWORD_REQUIRED');
            return;
        }

        this.deleteLoading.set(true);

        this.pilotService.deleteMyAccount({ password: this.deletePassword }).subscribe({
            next: () => {
                // Account is gone server-side - the JWT is now meaningless, so
                // clear it and send the pilot back to /login, same as a normal logout.
                this.authService.logout();
                this.router.navigate(['/login']);
            },
            error: (error: HttpErrorResponse) => {
                this.deleteLoading.set(false);
                this.deleteError.set(error.status === 400
                    ? 'PROFILE.DELETE_PASSWORD_WRONG'
                    : 'PROFILE.DELETE_FAILED');
            },
        });
    }
}
