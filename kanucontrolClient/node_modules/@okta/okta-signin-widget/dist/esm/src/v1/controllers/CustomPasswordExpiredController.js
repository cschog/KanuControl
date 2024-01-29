import { loc, internal } from '../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
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
import Footer from '../views/expired-password/Footer.js';

/*!
 * Copyright (c) 2017, Okta, Inc. and/or its affiliates. All rights reserved.
 * The Okta software accompanied by this notice is provided pursuant to the Apache License, Version 2.0 (the "License.")
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0.
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under the License.
 */
const {
  Util: Util
} = internal.util;
var CustomPasswordExpiredController = FormController.extend({
  className: 'custom-password-expired',
  Model: {},
  Form: {
    noButtonBar: true,
    title: function () {
      const expiringSoon = this.options.appState.get('isPwdExpiringSoon');
      const numDays = this.options.appState.get('passwordExpireDays');

      if (expiringSoon && numDays > 0) {
        return loc('password.expiring.title', 'login', [numDays]);
      } else if (expiringSoon && numDays === 0) {
        return loc('password.expiring.today', 'login');
      } else if (expiringSoon) {
        return loc('password.expiring.soon', 'login');
      } else {
        return this.settings.get('brandName') ? loc('password.expired.title.specific', 'login', [this.settings.get('brandName')]) : loc('password.expired.title.generic', 'login');
      }
    },
    subtitle: function () {
      if (this.options.appState.get('isPwdExpiringSoon')) {
        const subtitle = this.settings.get('brandName') ? loc('password.expiring.subtitle.specific', 'login', [this.settings.get('brandName')]) : loc('password.expiring.subtitle.generic', 'login');
        return subtitle + ' ' + loc('password.expired.custom.subtitle', 'login');
      }

      return loc('password.expired.custom.subtitle', 'login');
    },
    formChildren: function () {
      return [FormType.Button({
        title: oktaUnderscore.partial(loc, 'password.expired.custom.submit', 'login', [this.options.appState.get('passwordExpiredWebsiteName')]),
        className: 'button button-primary button-wide',
        attributes: {
          'data-se': 'custom-button'
        },
        click: function () {
          Util.redirect(this.options.appState.get('passwordExpiredLinkUrl'));
        }
      })];
    }
  },
  Footer: Footer
});

export { CustomPasswordExpiredController as default };
//# sourceMappingURL=CustomPasswordExpiredController.js.map
