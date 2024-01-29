import _Handlebars2 from '../../../../../packages/@okta/courage-dist/esm/lib/handlebars/dist/cjs/handlebars.runtime.js';
import { View, loc } from '../../../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
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
import BaseForm from '../../internals/BaseForm.js';
import BaseAuthenticatorView from '../../components/BaseAuthenticatorView.js';

const ExampleView = View.extend({
  template: _Handlebars2.template({
    "compiler": [8, ">= 4.3.0"],
    "main": function (container, depth0, helpers, partials, data) {
      return "<div aria-hidden=\"true\" class=\"yubikey-demo\"></div>";
    },
    "useData": true
  })
});
const Body = BaseForm.extend({
  title: function () {
    return this.options.appState.isAuthenticatorChallenge() ? loc('oie.yubikey.challenge.title', 'login') : loc('oie.yubikey.enroll.title', 'login');
  },
  subtitle: function () {
    return loc('oie.yubikey.description', 'login');
  },
  save: function () {
    return loc('mfa.challenge.verify', 'login');
  },
  getUISchema: function () {
    const schema = BaseForm.prototype.getUISchema.apply(this, arguments);
    schema.unshift({
      View: ExampleView,
      selector: '.o-form-fieldset-container'
    });
    return schema;
  }
});
var AuthenticatorYubiKeyView = BaseAuthenticatorView.extend({
  Body: Body
});

export { AuthenticatorYubiKeyView as default };
//# sourceMappingURL=AuthenticatorYubiKeyView.js.map
