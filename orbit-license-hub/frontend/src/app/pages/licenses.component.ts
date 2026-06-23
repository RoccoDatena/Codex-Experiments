import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { FormBuilder, Validators } from '@angular/forms';
import { ApiService } from '../core/api.service';
import { AuthService } from '../core/auth.service';
import { LicenseRecord } from '../core/models';

@Component({
  selector: 'app-licenses',
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './licenses.component.html'
})
export class LicensesComponent implements OnInit {
  items: LicenseRecord[] = [];
  search = '';
  page = 0;
  size = 10;
  totalPages = 0;
  sort = 'id,desc';
  tenantFilter: number | undefined;
  showForm = false;

  private readonly fb = inject(FormBuilder);

  readonly form = this.fb.nonNullable.group({
    tenantId: [0, [Validators.required, Validators.min(1)]],
    applicationId: [0, [Validators.required, Validators.min(1)]],
    deviceCode: ['', [Validators.required, Validators.minLength(2)]]
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
    this.api.getLicenses(this.page, this.size, this.search, this.sort, this.tenantFilter).subscribe((response) => {
      this.items = response.content;
      this.totalPages = response.totalPages;
    });
  }

  openCreate(): void {
    this.form.patchValue({
      tenantId: this.tenantFilter ?? 0,
      applicationId: 0,
      deviceCode: ''
    });
    this.showForm = true;
  }

  closeForm(): void {
    this.form.patchValue({ deviceCode: '' });
    this.showForm = false;
  }

  assign(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.api.assignLicense(this.form.getRawValue()).subscribe(() => {
      this.closeForm();
      this.load();
    });
  }

  deassign(item: LicenseRecord): void {
    this.api.deassignLicense(item.id).subscribe(() => this.load());
  }

  release(item: LicenseRecord): void {
    this.api.releaseLicense(item.id).subscribe(() => this.load());
  }
}
