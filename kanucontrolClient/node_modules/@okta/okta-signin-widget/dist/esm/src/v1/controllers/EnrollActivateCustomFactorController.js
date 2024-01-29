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
import '../../../packages/@okta/courage-dist/esm/src/courage/util/underscore-wrapper.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/models/Model.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/models/BaseModel.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/framework/View.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/views/Backbone.ListView.js';
import EnrollCustomFactorController from './EnrollCustomFactorController.js';
import Factor from '../models/Factor.js';
import FormType from '../util/FormType.js';
import HtmlErrorMessageView from '../views/mfa-verify/HtmlErrorMessageView.js';

/*!
 * Copyright (c) 2019-2020, Okta, Inc. and/or its affiliates. All rights reserved.
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
var EnrollActivateCustomFactorController = EnrollCustomFactorController.extend({
  Model: {
    local: {
      provider: 'string',
      factorType: 'string'
    },
    save: function () {
      return this.manageTransaction(() => {
        const url = this.appState.get('enrollCustomFactorRedirectUrl');

        if (url !== null) {
          Util.redirect(url);
        }
      });
    }
  },
  Form: function () {
    const factor = new Factor.Model(this.options.appState.get('factor'), this.toJSON());
    const vendorName = factor.get('vendorName');
    const subtitle = loc('enroll.customFactor.subtitle', 'login', [vendorName]);
    const saveText = loc('enroll.customFactor.save', 'login');
    return {
      autoSave: true,
      title: vendorName,
      subtitle: subtitle,
      save: saveText,
      formChildren: function () {
        const result = [];

        if (this.options.appState.get('isFactorResultFailed')) {
          result.push(FormType.View({
            View: new HtmlErrorMessageView({
              message: this.options.appState.get('factorResultErrorMessage')
            })
          }, {
            selector: '.o-form-error-container'
          }));
        }

        return result;
      }
    };
  }
});

export { EnrollActivateCustomFactorController as default };
//# sourceMappingURL=EnrollActivateCustomFactorController.js.map
