import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../core/auth.service';

@Component({
  selector: 'app-shell',
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './shell.component.html',
  styleUrl: './shell.component.css'
})
export class ShellComponent {
  constructor(private readonly auth: AuthService, private readonly router: Router) {}

  get userLabel(): string {
    const user = this.auth.getCurrentUser();
    return user ? `${user.username} (${user.role})` : 'anonymous';
  }

  logout(): void {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}
