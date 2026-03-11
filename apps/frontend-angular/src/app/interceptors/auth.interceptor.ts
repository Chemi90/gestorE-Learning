import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.getToken();

  console.log(`Interceptor - URL: ${req.url}, HasToken: ${!!token}`);

  if (!token) {
    console.warn('Interceptor - No token found, sending request without Authorization header');
    return next(req);
  }

  console.log('Interceptor - Adding Authorization header');
  return next(
    req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    })
  );
};
