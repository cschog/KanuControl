import { View } from '../../../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
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
import { SHOW_RESEND_TIMEOUT } from '../../utils/Constants.js';
import sessionStorageHelper from '../../../client/sessionStorageHelper.js';

var BaseResendView = View.extend({
  postRender: function () {
    this.showCalloutAfterTimeout();
  },
  showCalloutAfterTimeout: function () {
    const timeStamp = sessionStorageHelper.getResendTimestamp();

    if (!timeStamp) {
      sessionStorageHelper.setResendTimestamp(Date.now());
    } // We keep track of a 'global' timestamp in sessionStorage because if the SIW does a re-render,
    // we don't want to force the user to wait another 30s again to see the resend link. With this
    // the user will wait AT MOST 30s until they see the resend link.


    this.showMeInterval = setInterval(() => {
      const start = sessionStorageHelper.getResendTimestamp();
      const now = Date.now();

      if (now - start >= SHOW_RESEND_TIMEOUT) {
        this.$el.removeClass('hide');
        clearInterval(this.showMeInterval);
        sessionStorageHelper.removeResendTimestamp();
      }
    }, 250);
  },
  remove: function () {
    var _this$options$appStat, _this$options$appStat2;

    View.prototype.remove.apply(this, arguments);
    clearInterval(this.showMeInterval);
    const formName = this.options.appState.get('currentFormName');
    const resendContext = ((_this$options$appStat = this.options.appState.get('currentAuthenticator')) === null || _this$options$appStat === void 0 ? void 0 : _this$options$appStat.resend) || ((_this$options$appStat2 = this.options.appState.get('currentAuthenticatorEnrollment')) === null || _this$options$appStat2 === void 0 ? void 0 : _this$options$appStat2.resend);
    const didFormNameChange = this.options.model.get('formName') !== formName; // Clear resend timeStamp whenever we change views (this means we're navigating away from the resend view)

    if (sessionStorageHelper.getResendTimestamp() && (!resendContext || didFormNameChange)) {
      sessionStorageHelper.removeResendTimestamp();
    }
  }
});

export { BaseResendView as default };
//# sourceMappingURL=BaseResendView.js.map
