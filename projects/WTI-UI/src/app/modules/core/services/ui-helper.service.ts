import { Injectable } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { NewClarificationAlertComponent } from '../../clarifications/components/new-clarification-alert/new-clarification-alert.component';
import { NewClarificationAnnoucementAlertComponent } from '../../clarifications/components/new-announcement-clarification-alert/new-announcement-alert.component';
import { NewRunAlertComponent } from '../../runs/components/new-run-alert/new-run-alert.component';
import { MatSnackBar } from '@angular/material/snack-bar';

@Injectable()
export class UiHelperService {
  enableClarificationNotifications = true;
  enableRunsNotifications = true;

  constructor(private _dialogService: MatDialog,
              private _matSnackBar: MatSnackBar) { }

  incomingClarification(id: string): void {
	console.log('clarification incoming : Alert will be initiated if alert is enabled.')
    if (this.enableClarificationNotifications) {
      this._dialogService.open(NewClarificationAlertComponent, {
        data: { id }
      });
    }
  }
  
  incomingClarificationAnnouncement(id: string): void {
	console.log('Announcement incoming : Alert will be initiated if alert is enabled.')
    if (this.enableClarificationNotifications) {
      this._dialogService.open(NewClarificationAnnoucementAlertComponent, {
        data: { id }
      });
    }
  }

  incomingRun(id: string): void {
    if (this.enableRunsNotifications) {
      this._dialogService.open(NewRunAlertComponent, {
        data: { id }
      });
    }
  }

  alertOk(message: string): void {
    this._matSnackBar.open(message, 'Close', {
      duration: undefined,   //no automatic dismissal; user must close
	  panelClass: 'green-snackbar'
    });
  }

  alertError(message: string): void {
    this._matSnackBar.open(message, 'Close', {
      duration: undefined,   //no automatic dismissal; user must close
	  panelClass: 'red-snackbar'
    });
  }

  indefinitelyDisplayedAlert(message: string): void {
	this._matSnackBar.open(message, 'Close', {
	  duration: undefined
	});
  }
}
