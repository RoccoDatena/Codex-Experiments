import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { ApiService } from '../core/api.service';
import { ApplicationItem } from '../core/models';
import { AuthService } from '../core/auth.service';

@Component({
  selector: 'app-applications',
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './applications.component.html'
})
export class ApplicationsComponent implements OnInit {
  items: ApplicationItem[] = [];
  search = '';
  page = 0;
  size = 10;
  totalPages = 0;
  sort = 'name,asc';
  tenantFilter: number | undefined;
  editingId: number | null = null;
  showForm = false;

  private readonly fb = inject(FormBuilder);

  readonly form = this.fb.nonNullable.group({
    tenantId: [0, [Validators.required, Validators.min(1)]],
    name: ['', [Validators.required]],
    logoUrl: [''],
    baseColor: ['#102844', [Validators.required]],
    environment: ['DEV'],
    p12Password: ['', [Validators.required]],
    p12FileRef: [''],
    retentionMinutes: [60, [Validators.required, Validators.min(1)]],
    publicKeyPem: ['', [Validators.required]],
    allocatedLicenses: [0, [Validators.required, Validators.min(0)]]
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
    this.api
      .getApplications(this.page, this.size, this.search, this.sort, this.tenantFilter)
      .subscribe((response) => {
        this.items = response.content;
        this.totalPages = response.totalPages;
      });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const payload = this.form.getRawValue();
    const request$ = this.editingId
      ? this.api.updateApplication(this.editingId, payload)
      : this.api.createApplication(payload);

    request$.subscribe(() => {
      this.closeForm();
      this.load();
    });
  }

  openCreate(): void {
    this.editingId = null;
    this.form.reset({
      tenantId: this.tenantFilter ?? 0,
      name: '',
      logoUrl: '',
      baseColor: '#102844',
      environment: 'DEV',
      p12Password: '',
      p12FileRef: '',
      retentionMinutes: 60,
      publicKeyPem: '',
      allocatedLicenses: 0
    });
    this.showForm = true;
  }

  edit(item: ApplicationItem): void {
    this.editingId = item.id;
    this.form.patchValue({
      tenantId: item.tenantId,
      name: item.name,
      logoUrl: item.logoUrl,
      baseColor: item.baseColor,
      environment: item.environment,
      retentionMinutes: item.retentionMinutes,
      publicKeyPem: item.publicKeyPem,
      allocatedLicenses: item.allocatedLicenses
    });
    this.showForm = true;
  }

  closeForm(): void {
    this.editingId = null;
    this.form.reset({
      tenantId: this.tenantFilter ?? 0,
      name: '',
      logoUrl: '',
      baseColor: '#102844',
      environment: 'DEV',
      p12Password: '',
      p12FileRef: '',
      retentionMinutes: 60,
      publicKeyPem: '',
      allocatedLicenses: 0
    });
    this.showForm = false;
  }

  disable(item: ApplicationItem): void {
    this.api.disableApplication(item.id).subscribe(() => this.load());
  }

  enable(item: ApplicationItem): void {
    this.api.enableApplication(item.id).subscribe(() => this.load());
  }
}
