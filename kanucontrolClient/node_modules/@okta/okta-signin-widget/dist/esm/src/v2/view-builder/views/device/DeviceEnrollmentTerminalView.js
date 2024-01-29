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
import '../../internals/BaseHeader.js';
import '../../internals/BaseFooter.js';
import BaseForm from '../../internals/BaseForm.js';
import '../../internals/BaseFormWithPolling.js';
import '../../internals/BaseOktaVerifyChallengeView.js';
import BaseView from '../../internals/BaseView.js';
import '../../components/AuthenticatorEnrollOptions.js';
import '../../components/AuthenticatorVerifyOptions.js';
import '../../../../util/FactorUtil.js';
import Enums from '../../../../util/Enums.js';
import '../../../../v1/views/admin-consent/ScopeList.js';
import '../../../../v1/views/consent/ScopeList.js';
import '../captcha/CaptchaView.js';
import { getDeviceEnrollmentContext, AndroidAppLinkWithAccountOdaTerminalView, AndroidAppLinkWithoutAccountOdaTerminalView, IosAndAndroidLoopbackOdaTerminalView } from '../../components/OdaOktaVerifyTerminalView.js';
import MdmOktaVerifyTerminalView from '../../components/MdmOktaVerifyTerminalView.js';
import OktaVerifyAuthenticatorHeader from '../../components/OktaVerifyAuthenticatorHeader.js';
import Link from '../../components/Link.js';

const BaseDeviceEnrollTerminalForm = BaseForm.extend({
  noButtonBar: true,
  className: 'device-enrollment-terminal'
});
const AndroidAppLinkPreselectForm = BaseForm.extend({
  attributes: {
    'data-se': 'android-app-link-setup-options-terminal'
  },
  title: function () {
    return loc('enroll.title.oda.with.account', 'login');
  },
  initialize: function () {
    BaseForm.prototype.initialize.apply(this, arguments);
    const deviceEnrollmentContext = getDeviceEnrollmentContext(this.options.appState.get('deviceEnrollment'));
    this.model.set('hasOVAccount', 'no');
    this.addInput({
      label: () => loc('enroll.subtitle.fastpass', 'login', [deviceEnrollmentContext.orgName]),
      'label-top': true,
      options: {
        'no': loc('enroll.option.noaccount.fastpass', 'login'),
        'yes': loc('enroll.option.account.fastpass', 'login')
      },
      name: 'hasOVAccount',
      type: 'radio'
    });
  },
  saveForm: function () {
    //remove any existing warnings or messages before saving form
    this.$el.find('.o-form-error-container').empty();
    this.options.appState.trigger('updateDeviceEnrollmentView', this.model.get('hasOVAccount') === 'yes');
  }
});
const AndroidAppLinkWithAccountOdaTerminalForm = BaseDeviceEnrollTerminalForm.extend({
  attributes: {
    'data-se': 'android-oda-app-link-with-ov-account-terminal'
  },
  title: function () {
    return loc('enroll.title.oda.with.account', 'login');
  },
  initialize: function () {
    BaseDeviceEnrollTerminalForm.prototype.initialize.apply(this, arguments);
    this.add(AndroidAppLinkWithAccountOdaTerminalView);
  }
});
const AndroidAppLinkWithoutAccountOdaTerminalForm = BaseDeviceEnrollTerminalForm.extend({
  attributes: {
    'data-se': 'android-oda-app-link-without-ov-account-terminal'
  },
  title: function () {
    return loc('enroll.title.oda.without.account', 'login');
  },
  initialize: function () {
    BaseDeviceEnrollTerminalForm.prototype.initialize.apply(this, arguments);
    this.add(AndroidAppLinkWithoutAccountOdaTerminalView);
  }
});
const IosAndAndroidLoopbackOdaTerminalForm = BaseDeviceEnrollTerminalForm.extend({
  attributes: {
    'data-se': 'loopback-terminal'
  },
  title: function () {
    return loc('enroll.title.oda', 'login');
  },
  initialize: function () {
    BaseDeviceEnrollTerminalForm.prototype.initialize.apply(this, arguments);
    this.add(IosAndAndroidLoopbackOdaTerminalView);
  }
});
const MdmTerminalForm = BaseDeviceEnrollTerminalForm.extend({
  attributes: {
    'data-se': 'mdm-terminal'
  },
  title: function () {
    return loc('enroll.title.mdm', 'login');
  },
  initialize: function () {
    BaseDeviceEnrollTerminalForm.prototype.initialize.apply(this, arguments);
    this.add(MdmOktaVerifyTerminalView);
  }
});
const AndroidAppLinkTerminalViewFooter = Link.extend({
  postRender: function () {
    this.$el.click(event => {
      event.preventDefault();
      this.options.appState.trigger('switchBackToPreselect');
    });
  }
});
var DeviceEnrollmentTerminalView = BaseView.extend({
  initialize: function () {
    BaseView.prototype.initialize.apply(this, arguments);
    this.listenTo(this.options.appState, 'updateDeviceEnrollmentView', this.handleUpdateDeviceEnrollmentView);
    this.listenTo(this.options.appState, 'switchBackToPreselect', this.handleSwitchBackToPreselect);
    const deviceEnrollmentContext = getDeviceEnrollmentContext(this.options.appState.get('deviceEnrollment'));
    this.enrollmentType = (deviceEnrollmentContext.enrollmentType || '').toLowerCase(); // oda/mdm

    switch (this.enrollmentType) {
      case Enums.ODA:
        this.Header = OktaVerifyAuthenticatorHeader;
        this.Body = deviceEnrollmentContext.isAndroidAppLink ? AndroidAppLinkPreselectForm : IosAndAndroidLoopbackOdaTerminalForm;
        break;

      case Enums.MDM:
        this.Body = MdmTerminalForm;
        break;
    }
  },
  handleUpdateDeviceEnrollmentView: function (withAccount) {
    this.Body = withAccount ? AndroidAppLinkWithAccountOdaTerminalForm : AndroidAppLinkWithoutAccountOdaTerminalForm;
    this.backLink = this.add(AndroidAppLinkTerminalViewFooter, {
      options: {
        name: 'back-to-preselect',
        label: loc('oform.back', 'login')
      }
    }).last();
    this.renderForm();
  },
  handleSwitchBackToPreselect: function () {
    this.Body = AndroidAppLinkPreselectForm;
    this.backLink && this.backLink.remove();
    this.render();
  }
});

export { DeviceEnrollmentTerminalView as default };
//# sourceMappingURL=DeviceEnrollmentTerminalView.js.map
