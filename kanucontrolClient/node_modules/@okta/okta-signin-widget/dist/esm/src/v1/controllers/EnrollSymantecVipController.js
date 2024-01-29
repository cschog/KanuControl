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
import Util from '../../util/Util.js';
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
var EnrollSymantecVipController = FormController.extend({
  className: 'enroll-symantec',
  Model: {
    props: {
      credentialId: ['string', true],
      passCode: ['string', true],
      nextPassCode: ['string', true],
      factorId: 'string'
    },
    save: function () {
      return this.doTransaction(function (transaction) {
        const factor = oktaUnderscore.findWhere(transaction.factors, {
          factorType: 'token',
          provider: 'SYMANTEC'
        });

        return factor.enroll({
          passCode: this.get('passCode'),
          nextPassCode: this.get('nextPassCode'),
          profile: {
            credentialId: this.get('credentialId')
          }
        });
      });
    }
  },
  Form: {
    title: oktaUnderscore.partial(loc, 'factor.totpHard.symantecVip', 'login'),
    subtitle: oktaUnderscore.partial(loc, 'enroll.symantecVip.subtitle', 'login'),
    noButtonBar: true,
    autoSave: true,
    className: 'enroll-symantec',
    formChildren: function () {
      return [FormType.Input({
        label: loc('enroll.symantecVip.credentialId.placeholder', 'login'),
        'label-top': true,
        explain: Util.createInputExplain('enroll.symantecVip.credentialId.tooltip', 'enroll.symantecVip.credentialId.placeholder', 'login'),
        'explain-top': true,
        name: 'credentialId',
        input: TextBox,
        type: 'text'
      }), FormType.Input({
        label: loc('enroll.symantecVip.passcode1.placeholder', 'login'),
        'label-top': true,
        explain: Util.createInputExplain('enroll.symantecVip.passcode1.tooltip', 'enroll.symantecVip.passcode1.placeholder', 'login'),
        'explain-top': true,
        name: 'passCode',
        input: TextBox,
        type: 'text'
      }), FormType.Input({
        label: loc('enroll.symantecVip.passcode2.placeholder', 'login'),
        'label-top': true,
        explain: Util.createInputExplain('enroll.symantecVip.passcode2.tooltip', 'enroll.symantecVip.passcode2.placeholder', 'login'),
        'explain-top': true,
        name: 'nextPassCode',
        input: TextBox,
        type: 'text'
      }), FormType.Toolbar({
        noCancelButton: true,
        save: loc('mfa.challenge.verify', 'login')
      })];
    }
  },
  Footer: Footer
});

export { EnrollSymantecVipController as default };
//# sourceMappingURL=EnrollSymantecVipController.js.map
