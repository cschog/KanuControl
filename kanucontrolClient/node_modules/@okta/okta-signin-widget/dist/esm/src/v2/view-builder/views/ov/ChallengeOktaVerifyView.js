import BaseAuthenticatorView from '../../components/BaseAuthenticatorView.js';
import { Body as Body$1 } from '../shared/ChallengePushView.js';
import Body$2 from './ChallengeOktaVerifyFastPassView.js';
import Body from './NumberChallengePushView.js';
import { AUTHENTICATOR_METHODS } from '../../../ion/RemediationConstants.js';

var ChallengeOktaVerifyView = BaseAuthenticatorView.extend({
  initialize: function () {
    var _this$options, _this$options$appStat, _currentAuthenticator;

    BaseAuthenticatorView.prototype.initialize.apply(this, arguments);
    const currentAuthenticator = (_this$options = this.options) === null || _this$options === void 0 ? void 0 : (_this$options$appStat = _this$options.appState) === null || _this$options$appStat === void 0 ? void 0 : _this$options$appStat.get('currentAuthenticator');
    const selectedMethod = currentAuthenticator === null || currentAuthenticator === void 0 ? void 0 : currentAuthenticator.methods[0];

    if ((selectedMethod === null || selectedMethod === void 0 ? void 0 : selectedMethod.type) === AUTHENTICATOR_METHODS.PUSH && currentAuthenticator !== null && currentAuthenticator !== void 0 && (_currentAuthenticator = currentAuthenticator.contextualData) !== null && _currentAuthenticator !== void 0 && _currentAuthenticator.correctAnswer) {
      this.Body = Body;
    } else if ((selectedMethod === null || selectedMethod === void 0 ? void 0 : selectedMethod.type) === AUTHENTICATOR_METHODS.PUSH) {
      this.Body = Body$1;
    } else {
      this.Body = Body$2;
    }
  }
});

export { ChallengeOktaVerifyView as default };
//# sourceMappingURL=ChallengeOktaVerifyView.js.map
