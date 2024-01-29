import '../../../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
import '../../../../../packages/@okta/courage-dist/esm/lib/handlebars/dist/cjs/handlebars.runtime.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/handle-url.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-base64.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-i18n.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-img.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-markdown.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-xsrfTokenInput.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/framework/Model.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/vendor/lib/backbone.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/util/jquery-wrapper.js';
import oktaUnderscore from '../../../../../packages/@okta/courage-dist/esm/src/courage/util/underscore-wrapper.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/models/Model.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/models/BaseModel.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/framework/View.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/views/Backbone.ListView.js';
import { MS_PER_SEC } from '../../utils/Constants.js';

var polling = {
  startPolling: function (newRefreshInterval) {
    this.fixedPollingInterval = this.options.currentViewState.refresh;
    this.dynamicPollingInterval = newRefreshInterval;
    this.countDownCounterValue = Math.ceil(this.pollingInterval / MS_PER_SEC); // Poll is present in remediation form

    if (this.fixedPollingInterval) {
      this._startRemediationPolling();
    } else {
      // Poll is present in authenticator/ authenticator Enrollment obj.
      // Authenticator won't co-exists hence it's safe to trigger both.
      this._startAuthenticatorPolling();
    }
  },
  _startAuthenticatorPolling: function () {
    // Authenticator won't co-exists hence it's safe to trigger both.
    ['currentAuthenticator', 'currentAuthenticatorEnrollment'].some(responseKey => {
      if (this.options.appState.has(responseKey)) {
        var _authenticator$poll;

        const authenticator = this.options.appState.get(responseKey);
        const authenticatorPollAction = `${responseKey}-poll`;
        const pollInterval = this.dynamicPollingInterval || (authenticator === null || authenticator === void 0 ? void 0 : (_authenticator$poll = authenticator.poll) === null || _authenticator$poll === void 0 ? void 0 : _authenticator$poll.refresh);

        if (oktaUnderscore.isNumber(pollInterval)) {
          this.polling = setTimeout(() => {
            this.options.appState.trigger('invokeAction', authenticatorPollAction);
          }, pollInterval);
        }

        return true;
      } else {
        return false;
      }
    });
  },
  _startRemediationPolling: function () {
    const pollInterval = this.dynamicPollingInterval || this.fixedPollingInterval;

    if (oktaUnderscore.isNumber(pollInterval)) {
      this.polling = setTimeout(() => {
        this.options.appState.trigger('saveForm', this.model);
      }, pollInterval);
    }
  },
  stopPolling: function () {
    if (this.polling) {
      clearTimeout(this.polling);
      this.polling = null;
    }
  }
};

export { polling as default };
//# sourceMappingURL=polling.js.map
