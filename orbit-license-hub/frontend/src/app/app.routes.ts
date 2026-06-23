import { Routes } from '@angular/router';
import { LoginComponent } from './pages/login.component';
import { ShellComponent } from './layout/shell.component';
import { authGuard } from './core/auth.guard';
import { roleGuard } from './core/role.guard';

export const appRoutes: Routes = [
  { path: 'login', component: LoginComponent },
  {
    path: '',
    component: ShellComponent,
    canActivate: [authGuard],
    children: [
      {
        path: 'tenants',
        canActivate: [roleGuard(['SUPER_ADMIN'])],
        loadComponent: () => import('./pages/tenants.component').then((m) => m.TenantsComponent)
      },
      {
        path: 'applications',
        canActivate: [roleGuard(['SUPER_ADMIN', 'TENANT_ADMIN', 'LICENSE_ADMIN', 'LICENSE_VIEWER'])],
        loadComponent: () =>
          import('./pages/applications.component').then((m) => m.ApplicationsComponent)
      },
      {
        path: 'licenses',
        canActivate: [roleGuard(['SUPER_ADMIN', 'TENANT_ADMIN', 'LICENSE_ADMIN', 'LICENSE_VIEWER'])],
        loadComponent: () => import('./pages/licenses.component').then((m) => m.LicensesComponent)
      },
      {
        path: 'users',
        canActivate: [roleGuard(['SUPER_ADMIN', 'TENANT_ADMIN'])],
        loadComponent: () => import('./pages/users.component').then((m) => m.UsersComponent)
      },
      { path: '', pathMatch: 'full', redirectTo: 'applications' }
    ]
  },
  { path: '**', redirectTo: '' }
];
