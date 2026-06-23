import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { FormBuilder, Validators } from '@angular/forms';
import { ApiService } from '../core/api.service';
import { AuthService } from '../core/auth.service';
import { UserItem, UserRole } from '../core/models';

@Component({
  selector: 'app-users',
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './users.component.html'
})
export class UsersComponent implements OnInit {
  items: UserItem[] = [];
  search = '';
  page = 0;
  size = 10;
  totalPages = 0;
  sort = 'username,asc';
  tenantFilter: number | undefined;
  showForm = false;

  readonly roles: UserRole[] = ['SUPER_ADMIN', 'TENANT_ADMIN', 'LICENSE_ADMIN', 'LICENSE_VIEWER'];
  private readonly fb = inject(FormBuilder);

  readonly form = this.fb.nonNullable.group({
    username: ['', [Validators.required]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
    role: ['LICENSE_VIEWER' as UserRole],
    tenantId: [0],
    extraPermissionsRaw: ['']
  });

  constructor(private readonly api: ApiService, private readonly auth: AuthService) {}

  ngOnInit(): void {
    const user = this.auth.getCurrentUser();
    if (user?.tenantId) {
      this.tenantFilter = user.tenantId;
      this.form.patchValue({ tenantId: user.tenantId });
    }
    this.load();
  }

  load(): void {
    this.api.getUsers(this.page, this.size, this.search, this.sort, this.tenantFilter).subscribe((response) => {
      this.items = response.content;
      this.totalPages = response.totalPages;
    });
  }

  openCreate(): void {
    this.form.patchValue({
      username: '',
      email: '',
      password: '',
      role: 'LICENSE_VIEWER',
      tenantId: this.tenantFilter ?? 0,
      extraPermissionsRaw: ''
    });
    this.showForm = true;
  }

  closeForm(): void {
    this.form.patchValue({ username: '', email: '', password: '', extraPermissionsRaw: '' });
    this.showForm = false;
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const raw = this.form.getRawValue();
    const payload = {
      username: raw.username,
      email: raw.email,
      password: raw.password,
      role: raw.role,
      tenantId: raw.tenantId || null,
      extraPermissions: raw.extraPermissionsRaw
        .split(',')
        .map((value) => value.trim())
        .filter((value) => value.length > 0)
    };

    this.api.createUser(payload).subscribe(() => {
      this.closeForm();
      this.load();
    });
  }
}
