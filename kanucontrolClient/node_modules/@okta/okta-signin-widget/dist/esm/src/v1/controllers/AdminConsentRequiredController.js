import { View, loc } from '../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
import '../../../packages/@okta/courage-dist/esm/lib/handlebars/dist/cjs/handlebars.runtime.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/handle-url.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-base64.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-i18n.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-img.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-markdown.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-xsrfTokenInput.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/framework/Model.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/vendor/lib/backbone.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/jquery-wrapper.js';
import oktaUnderscore from '../../../packages/@okta/courage-dist/esm/src/courage/util/underscore-wrapper.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/models/Model.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/models/BaseModel.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/framework/View.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/views/Backbone.ListView.js';
import FormController from '../util/FormController.js';
import FormType from '../util/FormType.js';
import AdminScopeList from '../views/admin-consent/ScopeList.js';
import consentLogoHeaderTemplate from '../views/shared/templates/consentLogoHeaderTemplate.js';

/*!
 * Copyright (c) 2020, Okta, Inc. and/or its affiliates. All rights reserved.
 * The Okta software accompanied by this notice is provided pursuant to the Apache License, Version 2.0 (the "License.")
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0.
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under the License.
 */
const ConsentModel = {
  props: {
    scopes: ['array', true]
  },
  save: function () {
    return this.doTransaction(function (transaction) {
      return transaction.consent({
        consent: {
          scopes: oktaUnderscore.pluck(this.get('scopes'), 'name')
        }
      });
    });
  },
  cancel: function () {
    var self = this;
    return this.doTransaction(function (transaction) {
      return transaction.cancel();
    }).then(function () {
      var consentCancelFn = self.settings.get('consent.cancel');

      if (oktaUnderscore.isFunction(consentCancelFn)) {
        consentCancelFn();
      }
    });
  }
};
const ConsentHeader = View.extend({
  className: 'consent-title detail-row',
  template: consentLogoHeaderTemplate,
  getTemplateData: function () {
    var appState = this.options.appState;
    return {
      appName: appState.escape('targetLabel'),
      customLogo: appState.get('targetLogo') && appState.get('targetLogo').href,
      defaultLogo: appState.get('defaultAppLogo'),
      clientURI: appState.get('targetClientURI') && appState.get('targetClientURI').href,
      issuer: appState.get('issuer')
    };
  }
});
const ConsentForm = {
  noCancelButton: false,
  buttonOrder: ['cancel', 'save'],
  autoSave: true,
  save: oktaUnderscore.partial(loc, 'consent.required.consentButton', 'login'),
  cancel: oktaUnderscore.partial(loc, 'consent.required.cancelButton', 'login'),
  formChildren: [FormType.View({
    View: ConsentHeader
  }), FormType.View({
    View: AdminScopeList
  })]
};
const AdminConsentRequiredController = FormController.extend({
  Model: ConsentModel,
  Form: ConsentForm,
  className: 'admin-consent-required',
  initialize: function () {
    this.model.set('scopes', this.options.appState.get('scopes'));
    this.listenTo(this.form, 'cancel', () => {
      this.model.cancel();
    });
  }
});

export { AdminConsentRequiredController as default };
//# sourceMappingURL=AdminConsentRequiredController.js.map
