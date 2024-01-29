import { loc, internal } from '../../../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
import '../../../../../packages/@okta/courage-dist/esm/lib/handlebars/dist/cjs/handlebars.runtime.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/handle-url.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-base64.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-i18n.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-img.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-markdown.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-xsrfTokenInput.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/framework/Model.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/vendor/lib/backbone.js';
import oktaJQueryStatic from '../../../../../packages/@okta/courage-dist/esm/src/courage/util/jquery-wrapper.js';
import oktaUnderscore from '../../../../../packages/@okta/courage-dist/esm/src/courage/util/underscore-wrapper.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/models/Model.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/models/BaseModel.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/framework/View.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/views/Backbone.ListView.js';
import fn from '../../../../util/FactorUtil.js';
import Factor from '../../../models/Factor.js';
import FactorsDropDownOptions from './FactorsDropDownOptions.js';

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
const {
  BaseDropDown: BaseDropDown
} = internal.views.components;
oktaJQueryStatic(document).click(function (e) {
  const $target = oktaJQueryStatic(e.target);
  const isDropdown = $target.closest('.option-selected').length > 0 && $target.closest('.dropdown').length > 0;

  if (!isDropdown) {
    oktaJQueryStatic('.dropdown .options').hide();
    oktaJQueryStatic('.dropdown a.option-selected').attr('aria-expanded', false);
  }
});
var FactorsDropDown = BaseDropDown.extend({
  className: 'bg-helper icon-button',
  screenReaderText: function () {
    const factors = this.options.appState.get('factors');
    let factor;
    let factorLabel;

    if (factors) {
      factor = fn.findFactorInFactorsArray(factors, this.options.provider, this.options.factorType);
    } else {
      factor = new Factor.Model(this.options.appState.get('factor'), this.toJSON());
    }

    factorLabel = factor.get('factorLabel');
    return loc('mfa.factors.dropdown.sr.text', 'login', [factorLabel]);
  },
  events: {
    'click a.option-selected': function (e) {
      e.preventDefault();

      if (oktaUnderscore.result(this, 'disabled')) {
        e.stopPropagation();
      } else {
        const expanded = this.$('.options').toggle().is(':visible');
        this.$('a.option-selected').attr('aria-expanded', expanded);

        if (expanded) {
          this.$('#okta-dropdown-options').find('li.factor-option:first a').focus();
        }
      }
    },
    'click .dropdown-disabled': function (e) {
      e.preventDefault();
      e.stopPropagation();
    }
  },
  initialize: function () {
    this.addOption(FactorsDropDownOptions.getDropdownOption('TITLE'));
    const factorsList = this.options.appState.get('factors');
    const multiplePushFactors = factorsList.hasMultipleFactorsOfSameType('push');
    factorsList.each(function (factor) {
      // Do not add okta totp if there are multiple okta push (each push will have an inline totp)
      if (!(factor.get('factorType') === 'token:software:totp' && multiplePushFactors)) {
        this.addOption(FactorsDropDownOptions.getDropdownOption(factor.get('factorName')), {
          model: factor
        });
        this.listenTo(this.last(), 'options:toggle', function () {
          this.$('.options').hide();
          this.$('a.option-selected').attr('aria-expanded', false);
        });
      }
    }, this);
  }
});

export { FactorsDropDown as default };
//# sourceMappingURL=FactorsDropDown.js.map
