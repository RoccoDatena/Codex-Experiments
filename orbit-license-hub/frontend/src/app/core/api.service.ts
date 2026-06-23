import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApplicationItem, LicenseRecord, PagedResponse, Tenant, UserItem } from './models';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly apiBase = 'http://localhost:8080/api';

  constructor(private readonly http: HttpClient) {}

  getTenants(page = 0, size = 10, search = '', sort = 'name,asc'): Observable<PagedResponse<Tenant>> {
    const params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('search', search)
      .set('sort', sort);
    return this.http.get<PagedResponse<Tenant>>(`${this.apiBase}/tenants`, { params });
  }

  createTenant(payload: Partial<Tenant>): Observable<Tenant> {
    return this.http.post<Tenant>(`${this.apiBase}/tenants`, payload);
  }

  updateTenant(id: number, payload: Partial<Tenant>): Observable<Tenant> {
    return this.http.put<Tenant>(`${this.apiBase}/tenants/${id}`, payload);
  }

  getApplications(
    page = 0,
    size = 10,
    search = '',
    sort = 'name,asc',
    tenantId?: number
  ): Observable<PagedResponse<ApplicationItem>> {
    let params = new HttpParams().set('page', page).set('size', size).set('search', search).set('sort', sort);
    if (tenantId !== undefined) {
      params = params.set('tenantId', tenantId);
    }
    return this.http.get<PagedResponse<ApplicationItem>>(`${this.apiBase}/applications`, { params });
  }

  createApplication(payload: Record<string, unknown>): Observable<ApplicationItem> {
    return this.http.post<ApplicationItem>(`${this.apiBase}/applications`, payload);
  }

  updateApplication(id: number, payload: Record<string, unknown>): Observable<ApplicationItem> {
    return this.http.put<ApplicationItem>(`${this.apiBase}/applications/${id}`, payload);
  }

  disableApplication(id: number): Observable<void> {
    return this.http.post<void>(`${this.apiBase}/applications/${id}/disable`, {});
  }

  enableApplication(id: number): Observable<void> {
    return this.http.post<void>(`${this.apiBase}/applications/${id}/enable`, {});
  }

  getLicenses(
    page = 0,
    size = 10,
    search = '',
    sort = 'id,desc',
    tenantId?: number
  ): Observable<PagedResponse<LicenseRecord>> {
    let params = new HttpParams().set('page', page).set('size', size).set('search', search).set('sort', sort);
    if (tenantId !== undefined) {
      params = params.set('tenantId', tenantId);
    }
    return this.http.get<PagedResponse<LicenseRecord>>(`${this.apiBase}/licenses`, { params });
  }

  assignLicense(payload: Record<string, unknown>): Observable<LicenseRecord> {
    return this.http.post<LicenseRecord>(`${this.apiBase}/licenses/assign`, payload);
  }

  deassignLicense(id: number): Observable<void> {
    return this.http.post<void>(`${this.apiBase}/licenses/${id}/deassign`, {});
  }

  releaseLicense(id: number): Observable<void> {
    return this.http.post<void>(`${this.apiBase}/licenses/${id}/release`, {});
  }

  getUsers(page = 0, size = 10, search = '', sort = 'username,asc', tenantId?: number): Observable<PagedResponse<UserItem>> {
    let params = new HttpParams().set('page', page).set('size', size).set('search', search).set('sort', sort);
    if (tenantId !== undefined) {
      params = params.set('tenantId', tenantId);
    }
    return this.http.get<PagedResponse<UserItem>>(`${this.apiBase}/users`, { params });
  }

  createUser(payload: Record<string, unknown>): Observable<UserItem> {
    return this.http.post<UserItem>(`${this.apiBase}/users`, payload);
  }
}
