import { Collection } from '../../../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
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
import '../../internals/BaseHeader.js';
import '../../internals/BaseFooter.js';
import BaseForm from '../../internals/BaseForm.js';
import '../../internals/BaseFormWithPolling.js';
import '../../internals/BaseOktaVerifyChallengeView.js';
import '../../internals/BaseView.js';
import '../../components/AuthenticatorEnrollOptions.js';
import AuthenticatorVerifyOptions from '../../components/AuthenticatorVerifyOptions.js';
import { getAuthenticatorDataForVerification } from '../../utils/AuthenticatorUtil.js';
import { AUTHENTICATOR_KEY } from '../../../ion/RemediationConstants.js';
import '../../../../v1/views/admin-consent/ScopeList.js';
import '../../../../v1/views/consent/ScopeList.js';
import '../captcha/CaptchaView.js';
import { Body as Body$1 } from '../SelectAuthenticatorVerifyView.js';

// Common view for OV push and custom app.
const Body = Body$1.extend({
  getUISchema: function () {
    // Change the UI schema to not display radios here.
    const uiSchemas = BaseForm.prototype.getUISchema.apply(this, arguments);
    const methodsSchema = uiSchemas.find(schema => schema.name === 'authenticator.methodType');

    this._sortMethodOptionsIfDeviceKnown(methodsSchema.options);

    const methodOptions = methodsSchema.options.map(option => {
      return Object.assign({}, option, getAuthenticatorDataForVerification({
        authenticatorKey: this.isOV() ? AUTHENTICATOR_KEY.OV : AUTHENTICATOR_KEY.CUSTOM_APP
      }));
    });
    return [{
      View: AuthenticatorVerifyOptions,
      options: {
        name: methodsSchema.name,
        collection: new Collection(methodOptions)
      }
    }];
  },
  // If the `deviceKnown` attribute is true, we should put the signed_nonce method to the top of authenticator list.
  // This is in sync with v2/ion/ui-schema/ion-object-handler.js - createOVOptions
  _sortMethodOptionsIfDeviceKnown: function (methodOptions) {
    var _this$options, _this$options$current, _this$options$current2, _this$options$current3;

    // Check if the `deviceKnown` attribute is true
    const deviceKnown = (_this$options = this.options) === null || _this$options === void 0 ? void 0 : (_this$options$current = _this$options.currentViewState) === null || _this$options$current === void 0 ? void 0 : (_this$options$current2 = _this$options$current.relatesTo) === null || _this$options$current2 === void 0 ? void 0 : (_this$options$current3 = _this$options$current2.value) === null || _this$options$current3 === void 0 ? void 0 : _this$options$current3.deviceKnown;

    if (deviceKnown) {
      const signedNonceIndex = methodOptions.findIndex(e => e.value === 'signed_nonce');

      if (signedNonceIndex > 0) {
        const signedNonceModel = methodOptions[signedNonceIndex]; // Put the 'signed_nonce' option to the top of the list

        methodOptions.splice(signedNonceIndex, 1);
        methodOptions.unshift(signedNonceModel);
      }
    }
  },
  isOV: function () {
    return this.options.appState.get('authenticatorKey') === AUTHENTICATOR_KEY.OV;
  }
});

export { Body as default };
//# sourceMappingURL=ChallengeOktaVerifyCustomAppFormView.js.map
