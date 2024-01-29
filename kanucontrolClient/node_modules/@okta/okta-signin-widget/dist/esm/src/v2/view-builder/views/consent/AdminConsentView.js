import '../../internals/BaseHeader.js';
import '../../internals/BaseFooter.js';
import '../../internals/BaseForm.js';
import '../../internals/BaseFormWithPolling.js';
import '../../internals/BaseOktaVerifyChallengeView.js';
import { loc } from '../../../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
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
import '../../../../../packages/@okta/courage-dist/esm/src/courage/util/underscore-wrapper.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/models/Model.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/models/BaseModel.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/framework/View.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/views/Backbone.ListView.js';
import BaseView from '../../internals/BaseView.js';
import '../../components/AuthenticatorEnrollOptions.js';
import '../../components/AuthenticatorVerifyOptions.js';
import '../../../../util/FactorUtil.js';
import '../../../../v1/views/admin-consent/ScopeList.js';
import '../../../../v1/views/consent/ScopeList.js';
import '../captcha/CaptchaView.js';
import AdminConsentViewHeader from './AdminConsentViewHeader.js';
import ConsentViewForm from './ConsentViewForm.js';
import { doesI18NKeyExist } from '../../../ion/i18nTransformer.js';

var AdminConsentView = BaseView.extend({
  Header: AdminConsentViewHeader,
  Body: ConsentViewForm,
  createModelClass: function (currentViewState) {
    const ModelClass = BaseView.prototype.createModelClass.apply(this, arguments);
    const {
      uiSchema: uiSchema
    } = currentViewState;
    const {
      scopes: scopes
    } = uiSchema[0];
    const i18nKeyPrefix = 'consent.scopes';
    const localizedScopes = scopes.map(({
      name: name,
      displayName: displayName,
      description: description
    }) => {
      const scopeKey = `${i18nKeyPrefix}.${name}`;
      const labelKey = `${scopeKey}.label`;
      const descKey = `${scopeKey}.desc`;
      const doesLabelExist = doesI18NKeyExist(labelKey);
      const doesDescExist = doesI18NKeyExist(descKey);
      const i18nDisplayName = doesLabelExist ? loc(labelKey, 'login') : displayName;
      const i18nDescription = doesDescExist ? loc(descKey, 'login') : description;
      return {
        name: name,
        displayName: i18nDisplayName,
        description: i18nDescription,
        isCustomized: !doesLabelExist
      };
    });
    return ModelClass.extend({
      props: {
        scopes: {
          type: 'array',
          value: localizedScopes
        }
      },
      local: {
        consent: {
          type: 'boolean',
          value: false
        }
      },
      toJSON: function () {
        return {
          consent: this.get('consent')
        };
      }
    });
  }
});

export { AdminConsentView as default };
//# sourceMappingURL=AdminConsentView.js.map
