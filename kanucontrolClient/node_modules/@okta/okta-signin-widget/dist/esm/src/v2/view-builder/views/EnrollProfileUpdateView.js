import _Handlebars2 from '../../../../packages/@okta/courage-dist/esm/lib/handlebars/dist/cjs/handlebars.runtime.js';
import { loc, View } from '../../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/handle-url.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-base64.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-i18n.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-img.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-markdown.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-xsrfTokenInput.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/framework/Model.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/vendor/lib/backbone.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/jquery-wrapper.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/underscore-wrapper.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/models/Model.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/models/BaseModel.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/framework/View.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/views/Backbone.ListView.js';
import '../internals/BaseHeader.js';
import BaseFooter from '../internals/BaseFooter.js';
import BaseForm from '../internals/BaseForm.js';
import '../internals/BaseFormWithPolling.js';
import '../internals/BaseOktaVerifyChallengeView.js';
import BaseView from '../internals/BaseView.js';
import '../components/AuthenticatorEnrollOptions.js';
import '../components/AuthenticatorVerifyOptions.js';
import '../../../util/FactorUtil.js';
import '../../../v1/views/admin-consent/ScopeList.js';
import '../../../v1/views/consent/ScopeList.js';
import './captcha/CaptchaView.js';
import { getSkipSetupLink } from '../utils/LinksUtil.js';

const Body = BaseForm.extend({
  className: 'profile-update',
  title: function () {
    return loc('oie.profile.additional.title', 'login');
  },
  save: function () {
    return loc('enroll.choices.submit.finish', 'login');
  },
  getUISchema: function () {
    const uiSchemas = BaseForm.prototype.getUISchema.apply(this, arguments);
    uiSchemas.forEach(input => {
      if (input.name === 'userProfile.secondEmail') {
        input.explain = View.extend({
          template: _Handlebars2.template({
            "compiler": [8, ">= 4.3.0"],
            "main": function (container, depth0, helpers, partials, data) {
              var stack1,
                  lookupProperty = container.lookupProperty || function (parent, propertyName) {
                if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
                  return parent[propertyName];
                }

                return undefined;
              };

              return (stack1 = (lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || container.hooks.helperMissing).call(depth0 != null ? depth0 : container.nullContext || {}, {
                "name": "i18n",
                "hash": {
                  "$1": "<span class='strong'>$1</span>",
                  "bundle": "login",
                  "code": "oie.profile.additional.secondemail.subtitle"
                },
                "data": data,
                "loc": {
                  "start": {
                    "line": 1,
                    "column": 0
                  },
                  "end": {
                    "line": 1,
                    "column": 112
                  }
                }
              })) != null ? stack1 : "";
            },
            "useData": true
          })
        });
      }
    });
    return uiSchemas;
  },
  initialize: function () {
    BaseForm.prototype.initialize.apply(this, arguments);
  }
});
const Footer = BaseFooter.extend({
  className: 'auth-footer side-by-side-links',
  links: function () {
    const {
      uiSchema: uiSchema
    } = this.options.currentViewState;
    const optionalParams = uiSchema.filter(item => item.required === false);

    if (uiSchema.length === optionalParams.length) {
      return getSkipSetupLink(this.options.appState, loc('oie.enroll.skip.profile', 'login'));
    } else {
      this.$el.removeClass('.side-by-side-links');
    }
  }
});
var EnrollProfileUpdateView = BaseView.extend({
  Body: Body,
  Footer: Footer,
  postRender: function () {
    BaseView.prototype.postRender.apply(this, arguments);
    /**
     * As per requirement of this flow set secondEmail default to empty string, if exists in remediation
     * ideally server should have passed default string in remediation
     */

    if (this.options.appState.getSchemaByName('userProfile.secondEmail')) {
      this.model.set('userProfile.secondEmail', '');
    }
  }
});

export { EnrollProfileUpdateView as default };
//# sourceMappingURL=EnrollProfileUpdateView.js.map
