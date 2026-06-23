import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { LoginRequest, LoginResponse, UserRole } from './models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly apiBase = 'http://localhost:8080/api';

  constructor(private readonly http: HttpClient) {}

  login(payload: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiBase}/auth/login`, payload).pipe(
      tap((response) => {
        localStorage.setItem('orbit_token', response.accessToken);
        localStorage.setItem('orbit_user', JSON.stringify(response));
      })
    );
  }

  logout(): void {
    localStorage.removeItem('orbit_token');
    localStorage.removeItem('orbit_user');
  }

  isLoggedIn(): boolean {
    return localStorage.getItem('orbit_token') !== null;
  }

  getCurrentUser(): LoginResponse | null {
    const raw = localStorage.getItem('orbit_user');
    if (!raw) {
      return null;
    }

    try {
      return JSON.parse(raw) as LoginResponse;
    } catch {
      return null;
    }
  }

  hasAnyRole(roles: UserRole[]): boolean {
    const user = this.getCurrentUser();
    return user ? roles.includes(user.role) : false;
  }
}
