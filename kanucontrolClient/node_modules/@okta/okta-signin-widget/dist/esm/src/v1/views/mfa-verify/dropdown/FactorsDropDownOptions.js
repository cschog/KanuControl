import _Handlebars2 from '../../../../../packages/@okta/courage-dist/esm/lib/handlebars/dist/cjs/handlebars.runtime.js';
import { loc } from '../../../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/handle-url.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-base64.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-i18n.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-img.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-markdown.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-xsrfTokenInput.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/framework/Model.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/vendor/lib/backbone.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/util/jquery-wrapper.js';
import oktaUnderscore from '../../../../../packages/@okta/courage-dist/esm/src/courage/util/underscore-wrapper.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/models/Model.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/models/BaseModel.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/framework/View.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/views/Backbone.ListView.js';
import fn from '../../../util/RouterUtil.js';

const pushTitleTpl = _Handlebars2.template({
  "compiler": [8, ">= 4.3.0"],
  "main": function (container, depth0, helpers, partials, data) {
    var stack1,
        helper,
        alias1 = depth0 != null ? depth0 : container.nullContext || {},
        alias2 = container.hooks.helperMissing,
        alias3 = "function",
        lookupProperty = container.lookupProperty || function (parent, propertyName) {
      if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
        return parent[propertyName];
      }

      return undefined;
    };

    return container.escapeExpression((helper = (helper = lookupProperty(helpers, "factorName") || (depth0 != null ? lookupProperty(depth0, "factorName") : depth0)) != null ? helper : alias2, typeof helper === alias3 ? helper.call(alias1, {
      "name": "factorName",
      "hash": {},
      "data": data,
      "loc": {
        "start": {
          "line": 1,
          "column": 0
        },
        "end": {
          "line": 1,
          "column": 14
        }
      }
    }) : helper)) + " (" + ((stack1 = (helper = (helper = lookupProperty(helpers, "deviceName") || (depth0 != null ? lookupProperty(depth0, "deviceName") : depth0)) != null ? helper : alias2, typeof helper === alias3 ? helper.call(alias1, {
      "name": "deviceName",
      "hash": {},
      "data": data,
      "loc": {
        "start": {
          "line": 1,
          "column": 16
        },
        "end": {
          "line": 1,
          "column": 32
        }
      }
    }) : helper)) != null ? stack1 : "") + ")";
  },
  "useData": true
}); // deviceName is escaped on BaseForm (see BaseForm's template)


const action = function (model) {
  let factorIndex;
  const factorType = model.get('factorType');
  const factorsList = this.options.appState.get('factors');

  if (factorsList.hasMultipleFactorsOfSameType(factorType)) {
    factorIndex = factorsList.getFactorIndex(factorType, model.get('id'));
  }

  const url = fn.createVerifyUrl(model.get('provider'), factorType, factorIndex);
  const self = this;
  this.options.appState.trigger('factorSwitched');
  this.model.manageTransaction(function (transaction, setTransaction) {
    // FACTOR_CHALLENGE does not have a prev link
    if (transaction.status === 'FACTOR_CHALLENGE') {
      this.options.appState.set('trapMfaRequiredResponse', true);
    }

    if (transaction.status === 'MFA_CHALLENGE' && transaction.prev) {
      this.options.appState.set('trapMfaRequiredResponse', true);
      return transaction.prev().then(function (trans) {
        self.trigger('options:toggle');
        setTransaction(trans);
        self.options.appState.trigger('navigate', url);
      });
    } else {
      self.trigger('options:toggle');
      self.options.appState.trigger('navigate', url);
    }
  });
};

const dropdownOptions = {
  TITLE: {
    title: oktaUnderscore.partial(loc, 'mfa.factors.dropdown.title', 'login'),
    className: 'dropdown-list-title',
    disabled: true
  },
  OKTA_VERIFY: {
    icon: 'factor-icon mfa-okta-verify-30',
    className: 'factor-option',
    title: function () {
      return this.model.get('factorLabel');
    },
    action: function () {
      action.call(this, this.model);
    }
  },
  OKTA_VERIFY_PUSH: {
    icon: 'factor-icon mfa-okta-verify-30',
    className: 'factor-option',
    title: function () {
      return pushTitleTpl({
        factorName: this.model.get('factorLabel'),
        deviceName: this.model.get('deviceName')
      });
    },
    action: function () {
      action.call(this, this.model);
    }
  },
  GOOGLE_AUTH: {
    icon: 'factor-icon mfa-google-auth-30',
    className: 'factor-option',
    title: function () {
      return this.model.get('factorLabel');
    },
    action: function () {
      action.call(this, this.model);
    }
  },
  CUSTOM_HOTP: {
    icon: 'factor-icon mfa-hotp-30',
    className: 'factor-option',
    title: function () {
      return this.model.get('factorLabel');
    },
    action: function () {
      action.call(this, this.model);
    }
  },
  SYMANTEC_VIP: {
    icon: 'factor-icon mfa-symantec-30',
    className: 'factor-option',
    title: function () {
      return this.model.get('factorLabel');
    },
    action: function () {
      action.call(this, this.model);
    }
  },
  RSA_SECURID: {
    icon: 'factor-icon mfa-rsa-30',
    className: 'factor-option',
    title: oktaUnderscore.partial(loc, 'factor.totpHard.rsaSecurId', 'login'),
    action: function () {
      action.call(this, this.model);
    }
  },
  ON_PREM: {
    icon: 'factor-icon mfa-onprem-30',
    className: 'factor-option',
    title: function () {
      return this.model.get('factorLabel');
    },
    action: function () {
      action.call(this, this.model);
    }
  },
  DUO: {
    icon: 'factor-icon mfa-duo-30',
    className: 'factor-option',
    title: function () {
      return this.model.get('factorLabel');
    },
    action: function () {
      action.call(this, this.model);
    }
  },
  DUO_PUSH: {
    icon: 'duo-push-16',
    className: 'suboption factor-option',
    // TODO: add phone number here
    title: oktaUnderscore.partial(loc, 'mfa.duoSecurity.push', 'login', ['XXX-XXX-7890'])
  },
  DUO_SMS: {
    icon: 'duo-sms-16',
    className: 'suboption factor-option',
    // TODO: add phone number here
    title: oktaUnderscore.partial(loc, 'mfa.duoSecurity.sms', 'login', ['XXX-XXX-7890'])
  },
  DUO_CALL: {
    icon: 'duo-call-16',
    className: 'suboption factor-option',
    // TODO: add phone number here
    title: oktaUnderscore.partial(loc, 'mfa.duoSecurity.call', 'login', ['XXX-XXX-7890'])
  },
  YUBIKEY: {
    icon: 'factor-icon mfa-yubikey-30',
    className: 'factor-option',
    title: function () {
      return this.model.get('factorLabel');
    },
    action: function () {
      action.call(this, this.model);
    }
  },
  SMS: {
    icon: 'factor-icon mfa-sms-30',
    className: 'factor-option',
    title: function () {
      return this.model.get('factorLabel');
    },
    action: function () {
      action.call(this, this.model);
    }
  },
  CALL: {
    icon: 'factor-icon mfa-call-30',
    className: 'factor-option',
    title: function () {
      return this.model.get('factorLabel');
    },
    action: function () {
      action.call(this, this.model);
    }
  },
  QUESTION: {
    icon: 'factor-icon mfa-question-30',
    className: 'factor-option',
    title: function () {
      return this.model.get('factorLabel');
    },
    action: function () {
      action.call(this, this.model);
    }
  },
  PASSWORD: {
    icon: 'factor-icon mfa-password-30',
    className: 'factor-option',
    title: function () {
      return this.model.get('factorLabel');
    },
    action: function () {
      action.call(this, this.model);
    }
  },
  WINDOWS_HELLO: {
    icon: 'factor-icon mfa-windows-hello-30',
    className: 'factor-option',
    title: function () {
      return this.model.get('factorLabel');
    },
    action: function () {
      action.call(this, this.model);
    }
  },
  U2F: {
    icon: 'factor-icon mfa-u2f-30',
    className: 'factor-option',
    title: function () {
      return this.model.get('factorLabel');
    },
    action: function () {
      action.call(this, this.model);
    }
  },
  WEBAUTHN: {
    icon: 'factor-icon mfa-webauthn-30',
    className: 'factor-option',
    title: function () {
      return this.model.get('factorLabel');
    },
    action: function () {
      action.call(this, this.model);
    }
  },
  EMAIL: {
    icon: 'factor-icon mfa-email-30',
    className: 'factor-option',
    title: function () {
      return this.model.get('factorLabel');
    },
    action: function () {
      action.call(this, this.model);
    }
  },
  GENERIC_SAML: {
    icon: 'factor-icon mfa-custom-factor-30',
    className: 'factor-option',
    title: function () {
      return this.model.get('factorLabel');
    },
    action: function () {
      action.call(this, this.model);
    }
  },
  GENERIC_OIDC: {
    icon: 'factor-icon mfa-custom-factor-30',
    className: 'factor-option',
    title: function () {
      return this.model.get('factorLabel');
    },
    action: function () {
      action.call(this, this.model);
    }
  },
  CUSTOM_CLAIMS: {
    icon: 'factor-icon mfa-custom-factor-30',
    className: 'factor-option',
    title: function () {
      return this.model.get('factorLabel');
    },
    action: function () {
      action.call(this, this.model);
    }
  }
};
var FactorsDropDownOptions = {
  getDropdownOption: function (factorName) {
    return dropdownOptions[factorName];
  }
};

export { FactorsDropDownOptions as default };
//# sourceMappingURL=FactorsDropDownOptions.js.map
