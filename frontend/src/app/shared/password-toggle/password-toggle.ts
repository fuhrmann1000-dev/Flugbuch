import { Component, model } from '@angular/core';
import { TranslatePipe } from '@ngx-translate/core';

/**
 * The classic "eye" button that toggles a neighboring password input between
 * masked and plain text. Drop it inside a `position: relative` wrapper right
 * after the `<input>`; it doesn't touch the input itself, it only flips the
 * two-way bound `visible` flag - the caller is responsible for binding that
 * flag to the input's `[type]`.
 */
@Component({
    selector: 'app-password-toggle',
    standalone: true,
    imports: [TranslatePipe],
    templateUrl: './password-toggle.html',
    styleUrl: './password-toggle.scss',
})
export class PasswordToggleComponent {
    public readonly visible = model(false);

    public toggle(): void {
        this.visible.set(!this.visible());
    }
}
