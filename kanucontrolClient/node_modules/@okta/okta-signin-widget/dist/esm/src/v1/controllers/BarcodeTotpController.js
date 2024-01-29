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
import fn from '../../util/FactorUtil.js';
import FormController from '../util/FormController.js';
import FormType from '../util/FormType.js';
import fn$1 from '../util/RouterUtil.js';
import BarcodeView from '../views/enroll-factors/BarcodeView.js';
import Footer from '../views/enroll-factors/Footer.js';

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
var BarcodeTotpController = FormController.extend({
  className: 'barcode-totp',
  Model: function () {
    return {
      local: {
        __factorType__: ['string', false, this.options.factorType],
        __provider__: ['string', false, this.options.provider]
      }
    };
  },
  Form: {
    title: function () {
      const factorName = fn.getFactorLabel(this.model.get('__provider__'), this.model.get('__factorType__'));
      return loc('enroll.totp.title', 'login', [factorName]);
    },
    save: oktaUnderscore.partial(loc, 'oform.next', 'login'),
    noCancelButton: true,
    attributes: {
      'data-se': 'step-scan'
    },
    className: 'barcode-scan',
    formChildren: [FormType.View({
      View: BarcodeView
    })]
  },
  Footer: Footer,
  initialize: function () {
    this.listenTo(this.form, 'save', function () {
      const url = fn$1.createActivateFactorUrl(this.model.get('__provider__'), this.model.get('__factorType__'), 'activate');
      this.options.appState.trigger('navigate', url);
    });
  }
});

export { BarcodeTotpController as default };
//# sourceMappingURL=BarcodeTotpController.js.map
