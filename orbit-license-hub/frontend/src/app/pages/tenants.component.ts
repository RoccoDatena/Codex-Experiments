import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { ApiService } from '../core/api.service';
import { Tenant } from '../core/models';

@Component({
  selector: 'app-tenants',
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './tenants.component.html'
})
export class TenantsComponent implements OnInit {
  tenants: Tenant[] = [];
  search = '';
  page = 0;
  size = 10;
  totalPages = 0;
  totalElements = 0;
  sort = 'name,asc';
  editingId: number | null = null;
  showForm = false;

  private readonly fb = inject(FormBuilder);

  readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.minLength(2)]],
    purchasedLicenses: [0, [Validators.required, Validators.min(0)]],
    status: ['ACTIVE' as 'ACTIVE' | 'DISABLED']
  });

  constructor(private readonly api: ApiService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.api.getTenants(this.page, this.size, this.search, this.sort).subscribe((response) => {
      this.tenants = response.content;
      this.totalPages = response.totalPages;
      this.totalElements = response.totalElements;
    });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.getRawValue();
    const request$ = this.editingId
      ? this.api.updateTenant(this.editingId, value)
      : this.api.createTenant(value);

    request$.subscribe(() => {
      this.closeForm();
      this.load();
    });
  }

  openCreate(): void {
    this.editingId = null;
    this.form.reset({ name: '', purchasedLicenses: 0, status: 'ACTIVE' });
    this.showForm = true;
  }

  edit(item: Tenant): void {
    this.editingId = item.id;
    this.form.patchValue({
      name: item.name,
      purchasedLicenses: item.purchasedLicenses,
      status: item.status
    });
    this.showForm = true;
  }

  closeForm(): void {
    this.editingId = null;
    this.form.reset({ name: '', purchasedLicenses: 0, status: 'ACTIVE' });
    this.showForm = false;
  }

  nextPage(): void {
    if (this.page + 1 >= this.totalPages) {
      return;
    }
    this.page += 1;
    this.load();
  }

  prevPage(): void {
    if (this.page === 0) {
      return;
    }
    this.page -= 1;
    this.load();
  }
}
