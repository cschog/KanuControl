import _Handlebars2 from '../../../../../packages/@okta/courage-dist/esm/lib/handlebars/dist/cjs/handlebars.runtime.js';
import { loc, View } from '../../../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
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
import BaseView from '../../internals/BaseView.js';
import { addCustomButton } from '../../internals/FormInputFactory.js';
import BaseAuthenticatorView from '../../components/BaseAuthenticatorView.js';
import EnrollGoogleAuthenticatorBarcodeView from './EnrollGoogleAuthenticatorBarcodeView.js';
import EnrollAuthenticatorManualSetupView from './EnrollAuthenticatorManualSetupView.js';

const VIEW_TO_DISPLAY = 'viewToDisplay';
const viewToDisplayState = {
  BARCODE: 'barcode',
  MANUAL: 'manual',
  ENTER_CODE: 'enterCode'
};
const Body = BaseForm.extend({
  title: function () {
    return loc('oie.enroll.google_authenticator.setup.title', 'login');
  },
  noButtonBar: true,
  className: 'oie-enroll-google-authenticator',
  enterCodeSubtitle: View.extend({
    template: _Handlebars2.template({
      "compiler": [8, ">= 4.3.0"],
      "main": function (container, depth0, helpers, partials, data) {
        var lookupProperty = container.lookupProperty || function (parent, propertyName) {
          if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
            return parent[propertyName];
          }

          return undefined;
        };

        return "<div class=\"google-authenticator-setup-info-title enter-code-title\">" + container.escapeExpression((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || container.hooks.helperMissing).call(depth0 != null ? depth0 : container.nullContext || {}, {
          "name": "i18n",
          "hash": {
            "bundle": "login",
            "code": "oie.enroll.google_authenticator.enterCode.title"
          },
          "data": data,
          "loc": {
            "start": {
              "line": 1,
              "column": 68
            },
            "end": {
              "line": 1,
              "column": 146
            }
          }
        })) + "</div>";
      },
      "useData": true
    })
  }),
  getUISchema: function () {
    const schema = BaseForm.prototype.getUISchema.apply(this, arguments);
    const nextButton = addCustomButton({
      className: 'google-authenticator-next',
      title: loc('oform.next', 'login'),
      attributes: {
        style: 'display: block'
      },
      click: () => {
        this.model.set(VIEW_TO_DISPLAY, viewToDisplayState.ENTER_CODE);
      }
    });
    const verifyButton = addCustomButton({
      className: 'google-authenticator-verify',
      title: loc('oform.verify', 'login'),
      click: () => {
        this.$el.submit();
      }
    });
    schema[0].showWhen = {
      viewToDisplay: viewToDisplayState.ENTER_CODE
    }; // Add Enter Code Subtitle

    schema.unshift({
      View: this.enterCodeSubtitle,
      selector: '.o-form-fieldset-container',
      showWhen: {
        viewToDisplay: viewToDisplayState.ENTER_CODE
      }
    });
    schema.push({
      View: EnrollGoogleAuthenticatorBarcodeView,
      selector: '.o-form-fieldset-container',
      showWhen: {
        viewToDisplay: viewToDisplayState.BARCODE
      }
    }, {
      View: EnrollAuthenticatorManualSetupView,
      selector: '.o-form-fieldset-container',
      showWhen: {
        viewToDisplay: viewToDisplayState.MANUAL
      }
    }, {
      label: false,
      className: 'shared-secret',
      type: 'text',
      placeholder: this.options.appState.get('currentAuthenticator').contextualData.sharedSecret,
      disabled: true,
      showWhen: {
        viewToDisplay: viewToDisplayState.MANUAL
      }
    }, {
      View: nextButton,
      showWhen: {
        viewToDisplay: val => val === viewToDisplayState.BARCODE || val === viewToDisplayState.MANUAL
      }
    }, {
      View: verifyButton,
      showWhen: {
        viewToDisplay: val => val === viewToDisplayState.ENTER_CODE
      }
    });
    return schema;
  }
});
var EnrollAuthenticatorGoogleAuthenticatorView = BaseAuthenticatorView.extend({
  Body: Body,
  createModelClass: function () {
    const ModelClass = BaseView.prototype.createModelClass.apply(this, arguments);
    const local = Object.assign({
      viewToDisplay: {
        value: 'barcode',
        type: 'string',
        required: true,
        values: [viewToDisplayState.BARCODE, viewToDisplayState.MANUAL, viewToDisplayState.ENTER_CODE]
      }
    }, ModelClass.prototype.local);
    return ModelClass.extend({
      local: local
    });
  }
});

export { EnrollAuthenticatorGoogleAuthenticatorView as default };
//# sourceMappingURL=EnrollAuthenticatorGoogleAuthenticatorView.js.map
