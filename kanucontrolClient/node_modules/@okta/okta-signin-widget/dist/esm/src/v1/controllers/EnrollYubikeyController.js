import { loc } from '../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
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
import Footer from '../views/enroll-factors/Footer.js';
import TextBox from '../views/shared/TextBox.js';

/*!
 * Copyright (c) 2015-2016, Okta, Inc. and/or its affiliates. All rights reserved.
 * The Okta software accompanied by this notice is provided pursuant to the Apache License, Version 2.0 (the "License.")
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0.
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under the License.
 */
var EnrollYubikeyController = FormController.extend({
  className: 'enroll-yubikey',
  Model: {
    props: {
      passCode: ['string', true],
      factorId: 'string'
    },
    save: function () {
      return this.doTransaction(function (transaction) {
        const factor = oktaUnderscore.findWhere(transaction.factors, {
          factorType: 'token:hardware',
          provider: 'YUBICO'
        });

        return factor.enroll({
          passCode: this.get('passCode')
        });
      });
    }
  },
  Form: {
    title: oktaUnderscore.partial(loc, 'enroll.yubikey.title', 'login'),
    subtitle: oktaUnderscore.partial(loc, 'enroll.yubikey.subtitle', 'login'),
    noCancelButton: true,
    save: oktaUnderscore.partial(loc, 'mfa.challenge.verify', 'login'),
    autoSave: true,
    className: 'enroll-yubikey',
    formChildren: [FormType.View({
      View: '<div class="yubikey-demo" data-type="yubikey-example"></div>'
    }), FormType.Input({
      name: 'passCode',
      input: TextBox,
      type: 'password'
    })]
  },
  Footer: Footer
});

export { EnrollYubikeyController as default };
//# sourceMappingURL=EnrollYubikeyController.js.map
