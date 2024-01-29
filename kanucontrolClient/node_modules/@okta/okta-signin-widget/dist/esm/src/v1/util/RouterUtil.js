import _Handlebars2 from '../../../packages/@okta/courage-dist/esm/lib/handlebars/dist/cjs/handlebars.runtime.js';
import { loc } from '../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/handle-url.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-base64.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-i18n.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-img.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-markdown.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-xsrfTokenInput.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/framework/Model.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/vendor/lib/backbone.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/jquery-wrapper.js';
import oktaUnderscore from '../../../packages/@okta/courage-dist/esm/src/courage/util/underscore-wrapper.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/models/Model.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/models/BaseModel.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/framework/View.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/views/Backbone.ListView.js';
import fn$1 from '../../util/BrowserFeatures.js';
import Enums from '../../util/Enums.js';
import ErrorCodes from '../../util/ErrorCodes.js';
import { UnsupportedBrowserError, ConfigError } from '../../util/Errors.js';
import util from '../../util/OAuth2Util.js';
import Util from '../../util/Util.js';

const fn = {};

const verifyUrlTpl = _Handlebars2.template({
  "compiler": [8, ">= 4.3.0"],
  "main": function (container, depth0, helpers, partials, data) {
    var helper,
        alias1 = depth0 != null ? depth0 : container.nullContext || {},
        alias2 = container.hooks.helperMissing,
        alias3 = "function",
        alias4 = container.escapeExpression,
        lookupProperty = container.lookupProperty || function (parent, propertyName) {
      if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
        return parent[propertyName];
      }

      return undefined;
    };

    return "signin/verify/" + alias4((helper = (helper = lookupProperty(helpers, "provider") || (depth0 != null ? lookupProperty(depth0, "provider") : depth0)) != null ? helper : alias2, typeof helper === alias3 ? helper.call(alias1, {
      "name": "provider",
      "hash": {},
      "data": data,
      "loc": {
        "start": {
          "line": 1,
          "column": 14
        },
        "end": {
          "line": 1,
          "column": 26
        }
      }
    }) : helper)) + "/" + alias4((helper = (helper = lookupProperty(helpers, "factorType") || (depth0 != null ? lookupProperty(depth0, "factorType") : depth0)) != null ? helper : alias2, typeof helper === alias3 ? helper.call(alias1, {
      "name": "factorType",
      "hash": {},
      "data": data,
      "loc": {
        "start": {
          "line": 1,
          "column": 27
        },
        "end": {
          "line": 1,
          "column": 41
        }
      }
    }) : helper));
  },
  "useData": true
});

const verifyUrlMultipleTpl = _Handlebars2.template({
  "compiler": [8, ">= 4.3.0"],
  "main": function (container, depth0, helpers, partials, data) {
    var helper,
        alias1 = depth0 != null ? depth0 : container.nullContext || {},
        alias2 = container.hooks.helperMissing,
        alias3 = "function",
        alias4 = container.escapeExpression,
        lookupProperty = container.lookupProperty || function (parent, propertyName) {
      if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
        return parent[propertyName];
      }

      return undefined;
    };

    return "signin/verify/" + alias4((helper = (helper = lookupProperty(helpers, "provider") || (depth0 != null ? lookupProperty(depth0, "provider") : depth0)) != null ? helper : alias2, typeof helper === alias3 ? helper.call(alias1, {
      "name": "provider",
      "hash": {},
      "data": data,
      "loc": {
        "start": {
          "line": 1,
          "column": 14
        },
        "end": {
          "line": 1,
          "column": 26
        }
      }
    }) : helper)) + "/" + alias4((helper = (helper = lookupProperty(helpers, "factorType") || (depth0 != null ? lookupProperty(depth0, "factorType") : depth0)) != null ? helper : alias2, typeof helper === alias3 ? helper.call(alias1, {
      "name": "factorType",
      "hash": {},
      "data": data,
      "loc": {
        "start": {
          "line": 1,
          "column": 27
        },
        "end": {
          "line": 1,
          "column": 41
        }
      }
    }) : helper)) + "/" + alias4((helper = (helper = lookupProperty(helpers, "factorIndex") || (depth0 != null ? lookupProperty(depth0, "factorIndex") : depth0)) != null ? helper : alias2, typeof helper === alias3 ? helper.call(alias1, {
      "name": "factorIndex",
      "hash": {},
      "data": data,
      "loc": {
        "start": {
          "line": 1,
          "column": 42
        },
        "end": {
          "line": 1,
          "column": 57
        }
      }
    }) : helper));
  },
  "useData": true
});

const verifyUrlNoProviderTpl = _Handlebars2.template({
  "compiler": [8, ">= 4.3.0"],
  "main": function (container, depth0, helpers, partials, data) {
    var helper,
        lookupProperty = container.lookupProperty || function (parent, propertyName) {
      if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
        return parent[propertyName];
      }

      return undefined;
    };

    return "signin/verify/" + container.escapeExpression((helper = (helper = lookupProperty(helpers, "factorType") || (depth0 != null ? lookupProperty(depth0, "factorType") : depth0)) != null ? helper : container.hooks.helperMissing, typeof helper === "function" ? helper.call(depth0 != null ? depth0 : container.nullContext || {}, {
      "name": "factorType",
      "hash": {},
      "data": data,
      "loc": {
        "start": {
          "line": 1,
          "column": 14
        },
        "end": {
          "line": 1,
          "column": 28
        }
      }
    }) : helper));
  },
  "useData": true
});

const enrollFactorUrlTpl = _Handlebars2.template({
  "compiler": [8, ">= 4.3.0"],
  "main": function (container, depth0, helpers, partials, data) {
    var helper,
        alias1 = depth0 != null ? depth0 : container.nullContext || {},
        alias2 = container.hooks.helperMissing,
        alias3 = "function",
        alias4 = container.escapeExpression,
        lookupProperty = container.lookupProperty || function (parent, propertyName) {
      if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
        return parent[propertyName];
      }

      return undefined;
    };

    return "signin/enroll/" + alias4((helper = (helper = lookupProperty(helpers, "provider") || (depth0 != null ? lookupProperty(depth0, "provider") : depth0)) != null ? helper : alias2, typeof helper === alias3 ? helper.call(alias1, {
      "name": "provider",
      "hash": {},
      "data": data,
      "loc": {
        "start": {
          "line": 1,
          "column": 14
        },
        "end": {
          "line": 1,
          "column": 26
        }
      }
    }) : helper)) + "/" + alias4((helper = (helper = lookupProperty(helpers, "factorType") || (depth0 != null ? lookupProperty(depth0, "factorType") : depth0)) != null ? helper : alias2, typeof helper === alias3 ? helper.call(alias1, {
      "name": "factorType",
      "hash": {},
      "data": data,
      "loc": {
        "start": {
          "line": 1,
          "column": 27
        },
        "end": {
          "line": 1,
          "column": 41
        }
      }
    }) : helper));
  },
  "useData": true
});

const activateFactorUrlTpl = _Handlebars2.template({
  "1": function (container, depth0, helpers, partials, data) {
    var helper,
        lookupProperty = container.lookupProperty || function (parent, propertyName) {
      if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
        return parent[propertyName];
      }

      return undefined;
    };

    return "/" + container.escapeExpression((helper = (helper = lookupProperty(helpers, "step") || (depth0 != null ? lookupProperty(depth0, "step") : depth0)) != null ? helper : container.hooks.helperMissing, typeof helper === "function" ? helper.call(depth0 != null ? depth0 : container.nullContext || {}, {
      "name": "step",
      "hash": {},
      "data": data,
      "loc": {
        "start": {
          "line": 1,
          "column": 63
        },
        "end": {
          "line": 1,
          "column": 71
        }
      }
    }) : helper));
  },
  "compiler": [8, ">= 4.3.0"],
  "main": function (container, depth0, helpers, partials, data) {
    var stack1,
        helper,
        alias1 = depth0 != null ? depth0 : container.nullContext || {},
        alias2 = container.hooks.helperMissing,
        alias3 = "function",
        alias4 = container.escapeExpression,
        lookupProperty = container.lookupProperty || function (parent, propertyName) {
      if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
        return parent[propertyName];
      }

      return undefined;
    };

    return "signin/enroll-activate/" + alias4((helper = (helper = lookupProperty(helpers, "provider") || (depth0 != null ? lookupProperty(depth0, "provider") : depth0)) != null ? helper : alias2, typeof helper === alias3 ? helper.call(alias1, {
      "name": "provider",
      "hash": {},
      "data": data,
      "loc": {
        "start": {
          "line": 1,
          "column": 23
        },
        "end": {
          "line": 1,
          "column": 35
        }
      }
    }) : helper)) + "/" + alias4((helper = (helper = lookupProperty(helpers, "factorType") || (depth0 != null ? lookupProperty(depth0, "factorType") : depth0)) != null ? helper : alias2, typeof helper === alias3 ? helper.call(alias1, {
      "name": "factorType",
      "hash": {},
      "data": data,
      "loc": {
        "start": {
          "line": 1,
          "column": 36
        },
        "end": {
          "line": 1,
          "column": 50
        }
      }
    }) : helper)) + ((stack1 = lookupProperty(helpers, "if").call(alias1, depth0 != null ? lookupProperty(depth0, "step") : depth0, {
      "name": "if",
      "hash": {},
      "fn": container.program(1, data, 0),
      "inverse": container.noop,
      "data": data,
      "loc": {
        "start": {
          "line": 1,
          "column": 50
        },
        "end": {
          "line": 1,
          "column": 78
        }
      }
    })) != null ? stack1 : "");
  },
  "useData": true
});

const recoveryUrlTpl = _Handlebars2.template({
  "compiler": [8, ">= 4.3.0"],
  "main": function (container, depth0, helpers, partials, data) {
    var helper,
        lookupProperty = container.lookupProperty || function (parent, propertyName) {
      if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
        return parent[propertyName];
      }

      return undefined;
    };

    return "signin/recovery/" + container.escapeExpression((helper = (helper = lookupProperty(helpers, "recoveryToken") || (depth0 != null ? lookupProperty(depth0, "recoveryToken") : depth0)) != null ? helper : container.hooks.helperMissing, typeof helper === "function" ? helper.call(depth0 != null ? depth0 : container.nullContext || {}, {
      "name": "recoveryToken",
      "hash": {},
      "data": data,
      "loc": {
        "start": {
          "line": 1,
          "column": 16
        },
        "end": {
          "line": 1,
          "column": 33
        }
      }
    }) : helper));
  },
  "useData": true
});

const refreshUrlTpl = _Handlebars2.template({
  "1": function (container, depth0, helpers, partials, data) {
    var helper,
        lookupProperty = container.lookupProperty || function (parent, propertyName) {
      if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
        return parent[propertyName];
      }

      return undefined;
    };

    return "/" + container.escapeExpression((helper = (helper = lookupProperty(helpers, "token") || (depth0 != null ? lookupProperty(depth0, "token") : depth0)) != null ? helper : container.hooks.helperMissing, typeof helper === "function" ? helper.call(depth0 != null ? depth0 : container.nullContext || {}, {
      "name": "token",
      "hash": {},
      "data": data,
      "loc": {
        "start": {
          "line": 1,
          "column": 39
        },
        "end": {
          "line": 1,
          "column": 48
        }
      }
    }) : helper));
  },
  "compiler": [8, ">= 4.3.0"],
  "main": function (container, depth0, helpers, partials, data) {
    var stack1,
        lookupProperty = container.lookupProperty || function (parent, propertyName) {
      if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
        return parent[propertyName];
      }

      return undefined;
    };

    return "signin/refresh-auth-state" + ((stack1 = lookupProperty(helpers, "if").call(depth0 != null ? depth0 : container.nullContext || {}, depth0 != null ? lookupProperty(depth0, "token") : depth0, {
      "name": "if",
      "hash": {},
      "fn": container.program(1, data, 0),
      "inverse": container.noop,
      "data": data,
      "loc": {
        "start": {
          "line": 1,
          "column": 25
        },
        "end": {
          "line": 1,
          "column": 55
        }
      }
    })) != null ? stack1 : "");
  },
  "useData": true
});

const sessionCookieRedirectTpl = _Handlebars2.template({
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

    return container.escapeExpression((helper = (helper = lookupProperty(helpers, "baseUrl") || (depth0 != null ? lookupProperty(depth0, "baseUrl") : depth0)) != null ? helper : alias2, typeof helper === alias3 ? helper.call(alias1, {
      "name": "baseUrl",
      "hash": {},
      "data": data,
      "loc": {
        "start": {
          "line": 1,
          "column": 0
        },
        "end": {
          "line": 1,
          "column": 11
        }
      }
    }) : helper)) + "/login/sessionCookieRedirect?checkAccountSetupComplete=true&token=" + ((stack1 = (helper = (helper = lookupProperty(helpers, "token") || (depth0 != null ? lookupProperty(depth0, "token") : depth0)) != null ? helper : alias2, typeof helper === alias3 ? helper.call(alias1, {
      "name": "token",
      "hash": {},
      "data": data,
      "loc": {
        "start": {
          "line": 1,
          "column": 77
        },
        "end": {
          "line": 1,
          "column": 88
        }
      }
    }) : helper)) != null ? stack1 : "") + "&redirectUrl=" + ((stack1 = (helper = (helper = lookupProperty(helpers, "redirectUrl") || (depth0 != null ? lookupProperty(depth0, "redirectUrl") : depth0)) != null ? helper : alias2, typeof helper === alias3 ? helper.call(alias1, {
      "name": "redirectUrl",
      "hash": {},
      "data": data,
      "loc": {
        "start": {
          "line": 1,
          "column": 101
        },
        "end": {
          "line": 1,
          "column": 118
        }
      }
    }) : helper)) != null ? stack1 : "");
  },
  "useData": true
});

const deviceActivationStatuses = ['DEVICE_ACTIVATED', 'DEVICE_NOT_ACTIVATED_CONSENT_DENIED', 'DEVICE_NOT_ACTIVATED'];

fn.isHostBackgroundChromeTab = function () {
  // Checks if the SIW is loaded in a chrome webview and
  // it is in an app that is in background.
  if (navigator.userAgent.match(/Android/) && navigator.userAgent.match(/Chrome/) && document.hidden) {
    return true;
  } else {
    return false;
  }
};

fn.isDocumentVisible = function () {
  return document.visibilityState === 'visible';
};

fn.createVerifyUrl = function (provider, factorType, factorIndex) {
  if (provider && factorIndex) {
    return verifyUrlMultipleTpl({
      provider: encodeURIComponent(provider.toLowerCase()),
      factorType: encodeURIComponent(factorType),
      factorIndex: encodeURIComponent(factorIndex)
    });
  } else if (provider) {
    return verifyUrlTpl({
      provider: encodeURIComponent(provider.toLowerCase()),
      factorType: encodeURIComponent(factorType)
    });
  } else {
    return verifyUrlNoProviderTpl({
      factorType: encodeURIComponent(factorType)
    });
  }
};

fn.createEnrollFactorUrl = function (provider, factorType) {
  return enrollFactorUrlTpl({
    provider: encodeURIComponent(provider.toLowerCase()),
    factorType: encodeURIComponent(factorType)
  });
};

fn.createActivateFactorUrl = function (provider, factorType, step) {
  return activateFactorUrlTpl({
    provider: encodeURIComponent(provider.toLowerCase()),
    factorType: encodeURIComponent(factorType),
    step: step ? encodeURIComponent(step) : false
  });
};

fn.createRecoveryUrl = function (recoveryToken) {
  return recoveryUrlTpl({
    recoveryToken: encodeURIComponent(recoveryToken)
  });
};

fn.createRefreshUrl = function (stateToken) {
  const token = stateToken ? encodeURIComponent(stateToken) : null;
  return refreshUrlTpl({
    token: token
  });
};

fn.routeAfterAuthStatusChangeError = function (router, err) {
  if (!err) {
    return;
  } // Global error handling for CORS enabled errors


  if (err.xhr && fn$1.corsIsNotEnabled(err.xhr)) {
    router.settings.callGlobalError(new UnsupportedBrowserError(loc('error.enabled.cors')));
    return;
  } // Token has expired - no longer valid. Navigate back to primary auth.


  if (err.errorCode === ErrorCodes.INVALID_TOKEN_EXCEPTION) {
    router.appState.set('flashError', err);
    router.controller.state.set('navigateDir', Enums.DIRECTION_BACK);

    if (router.settings.get('features.mfaOnlyFlow')) {
      router.navigate('signin/error', {
        trigger: true
      });
    } else {
      router.navigate('', {
        trigger: true
      });
    }

    return;
  }

  Util.triggerAfterError(router.controller, err);
};

fn.routeAfterAuthStatusChange = function (router, res) {
  // Other errors are handled by the function making the authClient request
  if (!res || !res.status) {
    router.appState.clearLastAuthResponse();
    return;
  }

  router.appState.setAuthResponse(res);

  if (router.controller && router.controller.trapAuthResponse(res)) {
    return;
  }

  fn.handleResponseStatus(router, res);
};

fn.handleResponseStatus = function (router, res) {
  switch (res.status) {
    case 'SUCCESS':
      {
        handleSuccessResponseStatus(router, res);
        return;
      }

    case 'ADMIN_CONSENT_REQUIRED':
      router.navigate('signin/admin-consent', {
        trigger: true
      });
      return;

    case 'CONSENT_REQUIRED':
      router.navigate('signin/consent', {
        trigger: true
      });
      return;

    case 'DEVICE_ACTIVATE':
      router.navigate('signin/device-activate', {
        trigger: true
      });
      return;
    // We want the same view for FACTOR_REQUIRED & FACTOR_CHALLENGE
    // In the new idx pipeline FACTOR_CHALLENGE API response does not contain a prev link

    case 'FACTOR_REQUIRED':
    case 'FACTOR_CHALLENGE':
    case 'MFA_REQUIRED':
      {
        const lastFailedChallengeFactorData = router.appState.get('lastFailedChallengeFactorData'); // When the widget is bootstrapped with an error in MFA_CHALLENGE state, we pass the
        // lastFailedChallengeFactorData to MFA_REQUIRED, in order to show the error message
        // on the correct factor view

        if (lastFailedChallengeFactorData && lastFailedChallengeFactorData.factor) {
          router.appState.get('factors').lastUsedFactor = lastFailedChallengeFactorData.factor;
        }

        const factor = router.appState.get('factors').getDefaultFactor();
        const url = fn.createVerifyUrl(factor.get('provider'), factor.get('factorType'));
        router.navigate(url, {
          trigger: true
        });
        router.appState.clearLastFailedChallengeFactorData();
        return;
      }

    case 'POLL':
      {
        const pollUrl = 'signin/poll';
        router.navigate(pollUrl, {
          trigger: true
        });
        return;
      }

    case 'MFA_CHALLENGE':
      // Since we normally trap MFA_CHALLENGE, this will only get called on a
      // page refresh or when an error is returned on verification with an IdP.
      // We need to return to MFA_REQUIRED to initialize the
      // page correctly (i.e. factors dropdown, etc)
      if (router.appState.get('isFactorResultFailed')) {
        router.appState.setLastFailedChallengeFactorData();
      }

      router.appState.get('transaction').prev().then(function (trans) {
        router.appState.set('transaction', trans);
      }); // TODO: catch/handle error here?

      return;

    case 'MFA_ENROLL':
    case 'FACTOR_ENROLL':
      router.navigate('signin/enroll', {
        trigger: true
      });
      return;

    case 'MFA_ENROLL_ACTIVATE':
    case 'FACTOR_ENROLL_ACTIVATE':
      {
        const activateUrl = fn.createActivateFactorUrl(router.appState.get('activatedFactorProvider'), router.appState.get('activatedFactorType'));
        router.navigate(activateUrl, {
          trigger: true
        });
        return;
      }

    case 'PASSWORD_WARN':
    case 'PASSWORD_EXPIRED':
      if (router.settings.get('features.customExpiredPassword') && !router.appState.get('isPwdManagedByOkta')) {
        router.navigate('signin/custom-password-expired', {
          trigger: true
        });
      } else {
        router.navigate('signin/password-expired', {
          trigger: true
        });
      }

      return;

    case 'RECOVERY_CHALLENGE':
      {
        const fromEmail = res.factorType.toLowerCase() === Enums.RECOVERY_FACTOR_TYPE_EMAIL.toLowerCase();
        const isForgotPassword = res.recoveryType === Enums.RECOVERY_TYPE_PASSWORD;
        const isUnlock = res.recoveryType === Enums.RECOVERY_TYPE_UNLOCK; // Will use this workaround (lowercasing response) until OKTA-69083 is resolved

        if (isForgotPassword && fromEmail) {
          router.navigate('signin/recovery-emailed', {
            trigger: true
          });
        } else if (isUnlock && fromEmail) {
          router.navigate('signin/unlock-emailed', {
            trigger: true
          });
        } else {
          router.navigate('signin/recovery-challenge', {
            trigger: true
          });
        }

        return;
      }

    case 'RECOVERY':
      router.navigate('signin/recovery-question', {
        trigger: true
      });
      return;

    case 'PASSWORD_RESET':
      router.navigate('signin/password-reset', {
        trigger: true
      });
      return;

    case 'LOCKED_OUT':
      {
        if (router.settings.get('features.selfServiceUnlock')) {
          router.navigate('signin/unlock', {
            trigger: true
          });
        } else {
          const errorMessage = loc('error.auth.lockedOut', 'login');
          const resError = {
            responseJSON: {
              errorCauses: [],
              errorSummary: errorMessage,
              errorCode: 'E0000119'
            }
          };
          const err = {
            name: 'AuthApiError',
            message: errorMessage,
            xhr: resError
          };
          router.controller.model.appState.trigger('removeLoading');
          router.controller.model.trigger('error', router.controller.model, resError);
          Util.triggerAfterError(router.controller, err);
        }

        return;
      }

    case 'PROFILE_REQUIRED':
      router.navigate('signin/enroll-user', {
        trigger: true
      });
      return;

    case 'UNAUTHENTICATED':
      // Either we have factors and we are in passwordlessAuth mode
      if (router.appState.get('promptForFactorInUnauthenticated')) {
        const defaultFactor = router.appState.get('factors').getDefaultFactor();
        const factorURL = fn.createVerifyUrl(defaultFactor.get('provider'), defaultFactor.get('factorType'));
        router.navigate(factorURL, {
          trigger: true
        });
        return;
      } // Or we're in device flow and we need to force idp discovery check


      if (router.appState.get('usingDeviceFlow')) {
        router.navigate('signin/idp-discovery-check', {
          trigger: true
        });
        return;
      } // Or we don't have anything and we need to show the login page


      router.navigate('', {
        trigger: true
      });
      return;

    default:
      throw new Error('Unknown status: ' + res.status);
  }
};

function handleSuccessResponseStatus(router, res) {
  var _res$_embedded, _res$_links, _res$_links$original, _res$_links2, _res$_links2$next;

  if (res.recoveryType === Enums.RECOVERY_TYPE_UNLOCK) {
    router.navigate('signin/account-unlocked', {
      trigger: true
    });
    return;
  }

  if (oktaUnderscore.contains(deviceActivationStatuses, (_res$_embedded = res._embedded) === null || _res$_embedded === void 0 ? void 0 : _res$_embedded.deviceActivationStatus)) {
    router.navigate('signin/device-activate-complete', {
      trigger: true
    });
    return;
  } // If the desired end result object needs to have idToken (and not sessionToken),
  // get the id token from session token before calling the global success function.


  if (router.settings.get('oauth2Enabled')) {
    util.getTokens(router.settings, {
      sessionToken: res.sessionToken
    }, router.controller);
    return;
  }

  const successData = {
    user: res._embedded.user,
    type: res.type || Enums.SESSION_SSO
  };

  if (res.relayState) {
    successData.relayState = res.relayState;
  }

  const redirectFn = router.settings.get('redirectUtilFn');
  const nextUrl = (res === null || res === void 0 ? void 0 : (_res$_links = res._links) === null || _res$_links === void 0 ? void 0 : (_res$_links$original = _res$_links.original) === null || _res$_links$original === void 0 ? void 0 : _res$_links$original.href) || (res === null || res === void 0 ? void 0 : (_res$_links2 = res._links) === null || _res$_links2 === void 0 ? void 0 : (_res$_links2$next = _res$_links2.next) === null || _res$_links2$next === void 0 ? void 0 : _res$_links2$next.href);

  if (res.type === Enums.SESSION_STEP_UP) {
    var _res$_links3, _res$_links3$next;

    const targetUrl = res === null || res === void 0 ? void 0 : (_res$_links3 = res._links) === null || _res$_links3 === void 0 ? void 0 : (_res$_links3$next = _res$_links3.next) === null || _res$_links3$next === void 0 ? void 0 : _res$_links3$next.href;
    successData.stepUp = {
      url: targetUrl,
      finish: function () {
        redirectFn(targetUrl);
      }
    };
  } else {
    if (nextUrl) {
      successData.next = function () {
        redirectFn(nextUrl);
      };
    } else {
      // Add the type for now until the API returns it.
      successData.type = Enums.SESSION_SSO;
    }

    if (res.sessionToken) {
      successData.session = {
        token: res.sessionToken,
        setCookieAndRedirect: function (redirectUri) {
          if (redirectUri) {
            Util.debugMessage(`
              Passing a "redirectUri" to "setCookieAndRedirect" is strongly discouraged.
              It is recommended to set a "redirectUri" option in the config object passed to the widget constructor.
            `);
          }

          redirectUri = redirectUri || router.settings.get('redirectUri');

          if (!redirectUri) {
            throw new ConfigError('"redirectUri" is required');
          }

          redirectFn(sessionCookieRedirectTpl({
            baseUrl: router.settings.get('baseUrl'),
            token: encodeURIComponent(res.sessionToken),
            redirectUrl: encodeURIComponent(redirectUri)
          }));
        }
      };
    }
  } // Check if we need to wait for redirect based on host.


  if (fn.isHostBackgroundChromeTab()) {
    document.addEventListener('visibilitychange', function checkVisibilityAndCallSuccess() {
      if (fn.isDocumentVisible()) {
        document.removeEventListener('visibilitychange', checkVisibilityAndCallSuccess);
        router.settings.callGlobalSuccess(Enums.SUCCESS, successData);
      }
    });
  } else {
    router.settings.callGlobalSuccess(Enums.SUCCESS, successData);
  }
}

export { fn as default };
//# sourceMappingURL=RouterUtil.js.map
