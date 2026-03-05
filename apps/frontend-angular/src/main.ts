import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { App } from './app/app';
import { environment } from './environments/environment';

console.log('DEBUG STARTUP - API_BASE_URL:', environment.API_BASE_URL);

bootstrapApplication(App, appConfig)
  .catch((err) => console.error(err));